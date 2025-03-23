package sicxesimulator.software.linker;

import sicxesimulator.data.ObjectFile;
import sicxesimulator.data.Symbol;
import sicxesimulator.data.SymbolTable;
import sicxesimulator.data.records.RelocationRecord;
import sicxesimulator.utils.Constants;
import sicxesimulator.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static sicxesimulator.data.ObjectFile.ObjectFileOrigin.LINKED_MODULES;

/**
 * Linker que suporta:
 *   - finalRelocation=true  => Aplica a realocação no próprio array de bytes
 *   - finalRelocation=false => Apenas concatena e ajusta offsets dos relocations, deixando para o Loader
 */
public class Linker {

    /**
     * Linka uma lista de módulos em um único ObjectFile final.
     *
     * @param modules         módulos a serem linkados
     * @param finalRelocation se true, o arquivo final já terá os endereços corrigidos (sem relocation records)
     * @param loadAddress     endereço de carga base (se finalRelocation=true)
     * @param outputFileName  nome do arquivo final (sem extensão)
     * @return ObjectFile resultante
     */
    public ObjectFile linkModules(
            List<ObjectFile> modules,
            boolean finalRelocation,
            int loadAddress,
            String outputFileName
    ) {
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("Nenhum módulo para linkar.");
        }

        // 1) Atribuir "base" para cada módulo e criar tabela global de símbolos
        LinkerContext context = assignBasesAndGlobalSymbols(modules, finalRelocation, loadAddress);

        // 2) Verificar símbolos importados
        verifyImportedSymbols(modules, context.globalSymbols);

        // 3) Concatena e (se finalRelocation) aplica a relocação já no array de bytes
        FinalCodeData finalData = buildFinalCodeAndSymbols(modules, context, finalRelocation);

        // 4) Determina endereço inicial e combina o código-fonte
        int finalStart = finalRelocation ? loadAddress : modules.getFirst().getStartAddress();
        List<String> combinedSource = combineModuleSources(modules);

        // 5) Monta o objeto final
        ObjectFile finalObj = new ObjectFile(
                finalStart,
                finalData.code,
                finalData.symbolTable,
                outputFileName,
                combinedSource,
                // sem importedSymbols: Collections.emptySet()
                Collections.emptySet(),
                // Se foi realocado de forma "final", não há relocations no objeto final
                finalRelocation ? Collections.emptyList() : finalData.relocationRecords
        );
        finalObj.setOrigin(LINKED_MODULES);
        finalObj.setFullyRelocated(finalRelocation);

        // 6) Gera arquivos de saída
        String objFileName = outputFileName + ".obj";
        try {
            writeLinkedObjectFile(finalObj, objFileName, finalRelocation);
        } catch (IOException e) {
            System.err.println("Falha ao gravar .obj final textual: " + e.getMessage());
        }
        File metaFile = new File(Constants.SAVE_DIR, outputFileName + ".meta");
        finalObj.saveToFile(metaFile);

        return finalObj;
    }

    // ============================================================================
    // Etapa 1: Constrói a tabela de símbolos globais e mapeia cada módulo -> base
    // ============================================================================

    private LinkerContext assignBasesAndGlobalSymbols(
            List<ObjectFile> modules,
            boolean finalRelocation,
            int loadAddress
    ) {
        int currentBase = loadAddress;
        Map<ObjectFile, Integer> baseMap = new HashMap<>();
        Map<String, Integer> globalSymbols = new HashMap<>();
        Set<String> exportedSymbols = new HashSet<>();
        int totalSize = 0;

        for (ObjectFile mod : modules) {
            baseMap.put(mod, currentBase);
            // Copia símbolos exportados para a "globalSymbols"
            for (Map.Entry<String, Symbol> e : mod.getSymbolTable().getAllSymbols().entrySet()) {
                String symName = e.getKey();
                Symbol sym = e.getValue();
                if (sym.isPublic) {
                    // Se finalRelocation, o símbolo global é (base + sym.address)
                    // Se não, ainda não fixamos endereços => "endereço local" é mantido
                    int globalAddr = finalRelocation ? currentBase + sym.address : sym.address;

                    if (exportedSymbols.contains(symName)) {
                        throw new IllegalStateException("Símbolo " + symName + " exportado por mais de um módulo.");
                    }
                    exportedSymbols.add(symName);
                    globalSymbols.put(symName, globalAddr);
                }
            }
            totalSize += mod.getProgramLength();
            currentBase += mod.getProgramLength();
        }

        return new LinkerContext(baseMap, globalSymbols, totalSize);
    }

    private void verifyImportedSymbols(List<ObjectFile> modules, Map<String, Integer> globalSymbols) {
        for (ObjectFile mod : modules) {
            for (String sym : mod.getImportedSymbols()) {
                if (!globalSymbols.containsKey(sym)) {
                    throw new IllegalArgumentException("Símbolo importado [" + sym + "] não foi definido em nenhum módulo.");
                }
            }
        }
    }

    // ============================================================================
    // Etapa 3: Concatena os blocos de código + ajusta/gera relocations
    // ============================================================================

    private FinalCodeData buildFinalCodeAndSymbols(
            List<ObjectFile> modules,
            LinkerContext ctx,
            boolean finalRelocation
    ) {
        byte[] finalCode = new byte[ctx.totalSize];
        SymbolTable finalSymTab = new SymbolTable();
        List<RelocationRecord> finalRecs = new ArrayList<>();

        int globalOffset = 0;

        for (ObjectFile mod : modules) {
            byte[] code = mod.getObjectCode();
            int base = ctx.baseMap.get(mod);

            // Se finalRelocation, ajusta cada M record "na hora"
            if (finalRelocation) {
                for (RelocationRecord rec : mod.getRelocationRecords()) {
                    fixAddressInCode(code, rec, mod, base, ctx.globalSymbols);
                }
            } else {
                // Caso relocável => apenas gera novos relocations com offset global
                for (RelocationRecord rec : mod.getRelocationRecords()) {
                    int newOffset = rec.offset() + globalOffset;
                    // Replicamos o PC-relativo, length, symbol etc.
                    RelocationRecord newRec = new RelocationRecord(
                            newOffset,
                            rec.symbol(),
                            rec.length(),
                            rec.pcRelative()
                    );
                    finalRecs.add(newRec);
                }
            }

            // Copia o código (possivelmente já realocado) para finalCode
            System.arraycopy(code, 0, finalCode, globalOffset, code.length);

            // Atualiza symbol table final
            for (Map.Entry<String, Symbol> e : mod.getSymbolTable().getAllSymbols().entrySet()) {
                String symName = e.getKey();
                Symbol sym = e.getValue();
                // Se finalRelocation => (base + localAddress)
                // Se relocável => (globalOffset + localAddress)
                int finalAddr = finalRelocation
                        ? (base + sym.address)
                        : (globalOffset + sym.address);
                finalSymTab.addSymbol(symName, finalAddr, sym.isPublic);
            }

            globalOffset += code.length;
        }

        return new FinalCodeData(finalCode, finalSymTab, finalRecs);
    }

    /**
     * Aplica a correção no array de bytes para cada registro de relocação (caso finalRelocation == true).
     * Se for PC-relativo, subtrai 3. (Adapte para Formato 4 se precisar subtrair 4.)
     */
    private void fixAddressInCode(
            byte[] code,
            RelocationRecord rec,
            ObjectFile module,
            int moduleBase,
            Map<String, Integer> globalSymbols
    ) {
        int offset = rec.offset();
        int length = rec.length();
        if (offset + length > code.length) {
            throw new IllegalArgumentException("RelocationRecord fora do array do módulo " + module.getProgramName());
        }

        // Lê valor atual
        int oldVal = 0;
        for (int i = 0; i < length; i++) {
            oldVal = (oldVal << 8) | (code[offset + i] & 0xFF);
        }

        // Resolvem o endereço do símbolo
        Integer resolved = globalSymbols.get(rec.symbol());
        if (resolved == null) {
            // Tenta ver se é um símbolo local do próprio módulo
            Integer local = module.getSymbolTable().getSymbolAddress(rec.symbol());
            if (local == null) {
                throw new IllegalStateException("Símbolo não encontrado: " + rec.symbol());
            }
            resolved = moduleBase + local;
        }

        // Ajusta valor
        int newVal = oldVal + resolved;
        if (rec.pcRelative()) {
            newVal -= 3; // Em Formato 3
        }

        // Escreve de volta
        for (int i = length - 1; i >= 0; i--) {
            code[offset + i] = (byte) (newVal & 0xFF);
            newVal >>>= 8;
        }
    }

    // ============================================================================
    // Etapa 4: Combina os fontes
    // ============================================================================
    private List<String> combineModuleSources(List<ObjectFile> modules) {
        List<String> combined = new ArrayList<>();
        for (ObjectFile mod : modules) {
            List<String> src = mod.getRawSourceCode();
            if (src != null) {
                combined.addAll(src);
            }
            combined.add(";---- Fim do Módulo " + mod.getProgramName() + " ----");
        }
        return combined;
    }

    // ============================================================================
    // Gera o arquivo .obj final com H, D, T, M, E
    // ============================================================================
    private void writeLinkedObjectFile(
            ObjectFile finalObj,
            String outputFileName,
            boolean finalRelocation
    ) throws IOException {
        int startAddr = finalObj.getStartAddress();
        byte[] code = finalObj.getObjectCode();
        int progLen = code.length;
        String progName = finalObj.getProgramName();
        SymbolTable finalSym = finalObj.getSymbolTable();

        StringBuilder content = new StringBuilder();
        // H record
        content.append(String.format("H^%-6s^%06X^%06X\n", fitProgramName(progName), startAddr, progLen));

        // D record (símbolos exportados)
        List<Symbol> exported = new ArrayList<>();
        for (Symbol s : finalSym.getAllSymbols().values()) {
            if (s.isPublic) exported.add(s);
        }
        if (!exported.isEmpty()) {
            StringBuilder dRec = new StringBuilder("D");
            for (Symbol es : exported) {
                dRec.append("^").append(es.name).append("^").append(String.format("%06X", es.address));
            }
            content.append(dRec).append("\n");
        }

        // T records
        List<TBlock> tblocks = buildTextRecords(startAddr, code);
        for (TBlock b : tblocks) {
            StringBuilder hex = new StringBuilder();
            for (byte bb : b.data) {
                hex.append(String.format("%02X", bb & 0xFF));
            }
            int sizeBlock = b.data.size();
            content.append(String.format("T^%06X^%02X^%s\n", b.startAddress, sizeBlock, hex));
        }

        // Se finalRelocation == false => M records
        if (!finalRelocation) {
            for (RelocationRecord rec : finalObj.getRelocationRecords()) {
                int address = startAddr + rec.offset();
                int halfBytes = rec.length() * 2;
                // Notação de +SÍMBOLO
                String mLine = String.format("M^%06X^%02X^+%s", address, halfBytes, rec.symbol());
                content.append(mLine).append("\n");
            }
        }

        // E record
        content.append(String.format("E^%06X\n", startAddr));

        // Escreve em disco
        FileUtils.writeFileInDir(Constants.SAVE_DIR, outputFileName, content.toString());
    }

    private String fitProgramName(String name) {
        if (name == null) return "NONAME";
        return (name.length() > 6) ? name.substring(0, 6) : name;
    }

    private List<TBlock> buildTextRecords(int start, byte[] code) {
        List<TBlock> blocks = new ArrayList<>();
        final int MAX_BYTES = 30;
        int idx = 0;
        while (idx < code.length) {
            int len = Math.min(MAX_BYTES, code.length - idx);
            TBlock tb = new TBlock(start + idx);
            for (int i = 0; i < len; i++) {
                tb.data.add(code[idx + i]);
            }
            blocks.add(tb);
            idx += len;
        }
        return blocks;
    }

    // ============================================================================
    // Classes internas auxiliares
    // ============================================================================
    private static class LinkerContext {
        Map<ObjectFile, Integer> baseMap;
        Map<String, Integer> globalSymbols;
        int totalSize;

        LinkerContext(Map<ObjectFile, Integer> baseMap, Map<String, Integer> globalSymbols, int totalSize) {
            this.baseMap = baseMap;
            this.globalSymbols = globalSymbols;
            this.totalSize = totalSize;
        }
    }

    private static class FinalCodeData {
        byte[] code;
        SymbolTable symbolTable;
        List<RelocationRecord> relocationRecords;

        FinalCodeData(byte[] code, SymbolTable symbolTable, List<RelocationRecord> relocationRecords) {
            this.code = code;
            this.symbolTable = symbolTable;
            this.relocationRecords = relocationRecords;
        }
    }

    public static class TBlock {
        int startAddress;
        List<Byte> data = new ArrayList<>();

        public TBlock(int start) {
            this.startAddress = start;
        }
    }
}
