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
 * Linker que suporta dois modos:
 *   - finalRelocation=true  => Aplica a realocação final (corrige o array de bytes).
 *                             O arquivo resultante fica totalmente resolvido.
 *   - finalRelocation=false => Gera um arquivo parcial, deixando relocations pendentes
 *                              para serem aplicados pelo Loader em tempo de carga.
 */
public class Linker {

    /**
     * Linka uma lista de módulos em um único ObjectFile final.
     *
     * @param modules         Lista de módulos a serem linkados.
     * @param finalRelocation true se o Linker já deve realizar toda a realocação
     *                        diretamente no array de bytes. false para gerar relocations
     *                        que serão aplicados depois pelo Loader.
     * @param loadAddress     Endereço base de carga a ser usado se finalRelocation=true.
     *                        Se finalRelocation=false, este valor não afeta a realocação final
     *                        mas pode ser usado na “costura” interna.
     * @param outputFileName  Nome do arquivo .obj resultante (sem extensão).
     * @return Objeto final linkado (ObjectFile).
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

        // Passo 1: Atribuir base para cada módulo e montar tabela global de símbolos
        LinkerContext context = assignBasesAndGlobalSymbols(modules, finalRelocation, loadAddress);

        // Passo 2: Validar símbolos importados (ver se todos existem em "globalSymbols")
        verifyImportedSymbols(modules, context.globalSymbols);

        // Passo 3: Concatena os bytes de cada módulo. Se finalRelocation=true,
        // já aplica a realocação nos bytes; senão, apenas gera relocationRecords novos.
        FinalCodeData finalData = buildFinalCodeAndSymbols(modules, context, finalRelocation);

        // Passo 4: Determina endereço inicial no objeto final
        // Se já realocou tudo, começa no loadAddress. Caso contrário,
        // poderíamos reaproveitar o do primeiro módulo. Aqui fica a critério do design.
        int finalStart = finalRelocation
                ? loadAddress
                : modules.get(0).getStartAddress();

        // Combina os fontes de todos módulos (opcional, serve para fins de debug)
        List<String> combinedSource = combineModuleSources(modules);

        // Passo 5: Cria o ObjectFile resultante
        // - Se finalRelocation=true => sem relocationRecords;
        //   do contrário, com a lista gerada (finalData.relocationRecords).
        ObjectFile finalObj = new ObjectFile(
                finalStart,
                finalData.code,
                finalData.symbolTable,
                outputFileName,
                combinedSource,
                Collections.emptySet(), // ImportedSymbols não são mais relevantes no objeto final
                finalRelocation ? Collections.emptyList() : finalData.relocationRecords
        );
        finalObj.setOrigin(LINKED_MODULES);
        finalObj.setFullyRelocated(finalRelocation);

        // Passo 6: sgerar saída textual H-D-T-M-E e gravar no disco
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

    /**
     * Atribui endereços-base para cada módulo, constrói a tabela global de símbolos
     * e calcula tamanho total do programa.
     */
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

            // Copia símbolos "public" (exportados) do módulo para a tabela global
            for (Map.Entry<String, Symbol> e : mod.getSymbolTable().getAllSymbols().entrySet()) {
                String symName = e.getKey();
                Symbol sym = e.getValue();
                if (sym.isPublic) {
                    // Se finalRelocation=true, definimos o endereço global como (base+local)
                    // Se false, mantemos local. (No final, no buildFinalCodeAndSymbols,
                    //  somaremos "globalOffset" para unificar.)
                    int globalAddr = finalRelocation ? (currentBase + sym.address) : sym.address;

                    if (exportedSymbols.contains(symName)) {
                        throw new IllegalStateException(
                                "Símbolo " + symName + " exportado por mais de um módulo."
                        );
                    }
                    exportedSymbols.add(symName);
                    globalSymbols.put(symName, globalAddr);
                }
            }

            // Tamanho do programa final: soma dos tamanhos de cada módulo
            totalSize += mod.getProgramLength();
            currentBase += mod.getProgramLength();
        }

        return new LinkerContext(baseMap, globalSymbols, totalSize);
    }

    /**
     * Verifica se cada símbolo importado de cada módulo existe em globalSymbols.
     */
    private void verifyImportedSymbols(List<ObjectFile> modules, Map<String, Integer> globalSymbols) {
        for (ObjectFile mod : modules) {
            for (String sym : mod.getImportedSymbols()) {
                if (!globalSymbols.containsKey(sym)) {
                    throw new IllegalArgumentException(
                            "Símbolo importado [" + sym + "] não foi definido em nenhum módulo."
                    );
                }
            }
        }
    }

    /**
     * Concatena e constrói o código final.
     * Se finalRelocation=true, já aplica reloc no array de bytes.
     * Caso contrário, realoca apenas o offset e gera novos RelocationRecords agregados.
     */
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

            // Se finalRelocation=true, corrigimos cada RelocationRecord no array do módulo
            if (finalRelocation) {
                for (RelocationRecord rec : mod.getRelocationRecords()) {
                    fixAddressInCode(code, rec, mod, base, ctx.globalSymbols);
                }
            } else {
                // Gera novos relocation records com offset somado
                for (RelocationRecord rec : mod.getRelocationRecords()) {
                    int newOffset = rec.offset() + globalOffset;
                    RelocationRecord newRec = new RelocationRecord(
                            newOffset,
                            rec.symbol(),
                            rec.length(),
                            rec.pcRelative()
                    );
                    finalRecs.add(newRec);
                }
            }

            // Copia o código deste módulo (já realocado ou não) para a posição final
            System.arraycopy(code, 0, finalCode, globalOffset, code.length);

            // Ajusta a SymbolTable final
            //   Se finalRelocation=true => address = base + sym.address
            //   Se false => address = globalOffset + sym.address
            for (Map.Entry<String, Symbol> e : mod.getSymbolTable().getAllSymbols().entrySet()) {
                String symName = e.getKey();
                Symbol sym = e.getValue();
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
     * Realocação "final" diretamente no array de bytes.
     * Consulta o endereço do símbolo (resolvido em globalSymbols) e soma.
     * Se pcRelative, subtrai 3 (ou 4, conforme formato).
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
            throw new IllegalArgumentException(
                    "RelocationRecord fora do array do módulo " + module.getProgramName()
            );
        }

        // Lê valor atual no array
        int oldVal = 0;
        for (int i = 0; i < length; i++) {
            oldVal = (oldVal << 8) | (code[offset + i] & 0xFF);
        }

        // Descobre o endereço do símbolo
        Integer resolved = globalSymbols.get(rec.symbol());
        if (resolved == null) {
            // Tenta ver se é símbolo local do próprio módulo
            Integer localAddr = module.getSymbolTable().getSymbolAddress(rec.symbol());
            if (localAddr == null) {
                throw new IllegalStateException("Símbolo não encontrado: " + rec.symbol());
            }
            resolved = moduleBase + localAddr;
        }

        // Soma
        int newVal = oldVal + resolved;
        // Se pcRelative, subtrai 3 (Formato 3)
        if (rec.pcRelative()) {
            newVal -= 3;
        }

        // Escreve o valor de volta
        for (int i = length - 1; i >= 0; i--) {
            code[offset + i] = (byte) (newVal & 0xFF);
            newVal >>>= 8;
        }
    }

    /**
     * Combina as linhas de código-fonte de todos módulos. (Opcional, para depuração)
     */
    private List<String> combineModuleSources(List<ObjectFile> modules) {
        List<String> combined = new ArrayList<>();
        for (ObjectFile mod : modules) {
            if (mod.getRawSourceCode() != null) {
                combined.addAll(mod.getRawSourceCode());
            }
            combined.add(";---- Fim do Módulo " + mod.getProgramName() + " ----");
        }
        return combined;
    }

    /**
     * Gera o arquivo .obj textual (formato H/D/T/M/E) do objeto final.
     * Se finalRelocation=false, inclui M records para que o Loader possa realocar.
     */
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
        // H record (Header)
        content.append(
                String.format("H^%-6s^%06X^%06X\n", fitProgramName(progName), startAddr, progLen)
        );

        // D record (Define) - símbolos exportados
        List<Symbol> exported = new ArrayList<>();
        for (Symbol s : finalSym.getAllSymbols().values()) {
            if (s.isPublic) {
                exported.add(s);
            }
        }
        if (!exported.isEmpty()) {
            StringBuilder dRec = new StringBuilder("D");
            for (Symbol es : exported) {
                dRec.append("^")
                        .append(es.name)
                        .append("^")
                        .append(String.format("%06X", es.address));
            }
            content.append(dRec).append("\n");
        }

        // T records (Text)
        List<TBlock> tblocks = buildTextRecords(startAddr, code);
        for (TBlock b : tblocks) {
            StringBuilder hex = new StringBuilder();
            for (byte bb : b.data) {
                hex.append(String.format("%02X", bb & 0xFF));
            }
            int sizeBlock = b.data.size();
            content.append(
                    String.format("T^%06X^%02X^%s\n", b.startAddress, sizeBlock, hex)
            );
        }

        // M records (Modification) - só se finalRelocation=false
        if (!finalRelocation) {
            for (RelocationRecord rec : finalObj.getRelocationRecords()) {
                int address = startAddr + rec.offset();
                int halfBytes = rec.length() * 2;
                // +SÍMBOLO
                String mLine = String.format(
                        "M^%06X^%02X^+%s", address, halfBytes, rec.symbol()
                );
                content.append(mLine).append("\n");
            }
        }

        // E record (End)
        content.append(String.format("E^%06X\n", startAddr));

        // Grava no disco
        FileUtils.writeFileInDir(Constants.SAVE_DIR, outputFileName, content.toString());
    }

    private String fitProgramName(String name) {
        if (name == null) return "NONAME";
        return (name.length() > 6) ? name.substring(0, 6) : name;
    }

    /**
     * Divide o array de bytes em blocos (máx 30) para gerar T records.
     */
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

    // ---------------------------------------------------------------
    // Classes auxiliares internas
    // ---------------------------------------------------------------
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
