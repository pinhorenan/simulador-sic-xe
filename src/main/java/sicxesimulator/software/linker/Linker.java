package sicxesimulator.software.linker;

import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.Symbol;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.software.data.RelocationRecord;
import sicxesimulator.common.utils.Constants;
import sicxesimulator.common.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Combina vários módulos objeto SIC/XE em um único {@link ObjectFile}.
 *
 * <p>Modos de operação:</p>
 * <ul>
 *   <li><b>finalRelocation = true</b> – aplica relocação definitiva nos bytes,
 *       gerando um objeto pronto para execução;</li>
 *   <li><b>false</b> – mantém {@link RelocationRecord}s pendentes
 *       para que o {@code Loader} resolva em tempo de carga.</li>
 * </ul>
 */
public class Linker {

    /**
     * Executa o processo completo de linkedição.
     *
     * @param modules         lista de objetos a linkar
     * @param finalRelocation indica se a relocação deve ser definitiva
     * @param loadAddress     endereço base (usado apenas se {@code finalRelocation=true})
     * @param outputFileName  nome-base dos arquivos gerados (sem extensão)
     * @return {@link ObjectFile} resultante
     */
    public ObjectFile linkModules(List<ObjectFile> modules, boolean finalRelocation, int loadAddress, String outputFileName) {
        Objects.requireNonNull(modules, "modules não pode ser nulo");
        if (modules.isEmpty()) throw new IllegalArgumentException("Nenhum módulo para linkar.");

        /* 1. Bases e símbolos globais */
        LinkerContext ctx = assignBasesAndGlobalSymbols(modules, finalRelocation);

        /* 2. Valida imports */
        verifyImportedSymbols(modules, ctx.globalSymbols);

        /* 3. Monta código final e relocations */
        FinalCodeData data = buildFinalCodeAndSymbols(modules, ctx, finalRelocation);

        /* 4. Endereço inicial */
        int startAddress = finalRelocation ? loadAddress : modules.get(0).getStartAddress();

        /* 5. Fonte combinado (debug) */
        List<String> sources = combineModuleSources(modules);

        /* 6. Cria ObjectFile */
        ObjectFile result = new ObjectFile(
                startAddress,
                data.code,
                data.symbolTable,
                outputFileName,
                sources,
                Collections.emptySet(),
                finalRelocation ? Collections.emptyList() : data.relocationRecords
        );
        result.setOrigin(ObjectFile.ObjectFileOrigin.LINKED_MODULES);
        result.setFullyRelocated(finalRelocation);

        /* 7. Persistência (.obj textual e .meta) */
        try {
            writeLinkedObjectFile(result, outputFileName, !finalRelocation);
        } catch (IOException e) {
            System.err.println("Falha ao gravar .obj textual: " + e.getMessage());
        }
        result.saveToFile(new File(Constants.SAVE_DIR, outputFileName + ".meta"));
        return result;
    }

    /* ------------------------------------------------------------------------------------------ */
    /*  PASSO 1 – Bases e símbolos globais                                                       */
    /* ------------------------------------------------------------------------------------------ */

    private LinkerContext assignBasesAndGlobalSymbols(List<ObjectFile> modules, boolean finalRelocation) {
        Map<ObjectFile, Integer> baseMap = new HashMap<>();
        Map<String, Integer> global = new HashMap<>();
        Set<String> duplicates = new HashSet<>();

        int totalSize;
        if (finalRelocation) {                    // modo absoluto
            int minStart = Integer.MAX_VALUE, maxEnd = 0;
            for (ObjectFile m : modules) {
                int start = m.getStartAddress();
                int end   = start + m.getProgramLength();
                minStart = Math.min(minStart, start);
                maxEnd   = Math.max(maxEnd,   end);
                baseMap.put(m, start);

                for (Symbol s : m.getSymbolTable().getAllSymbols().values()) {
                    if (s.isPublic) {
                        if (!duplicates.add(s.name))
                            throw new IllegalStateException("Símbolo duplicado: " + s.name);
                        global.put(s.name, start + s.address);
                    }
                }
            }
            totalSize = maxEnd - minStart;
        } else {                                 // modo relocável em sequência
            int offset = 0;
            for (ObjectFile m : modules) {
                baseMap.put(m, offset);
                for (Symbol s : m.getSymbolTable().getAllSymbols().values()) {
                    if (s.isPublic) {
                        if (!duplicates.add(s.name))
                            throw new IllegalStateException("Símbolo duplicado: " + s.name);
                        global.put(s.name, offset + s.address);
                    }
                }
                offset += m.getProgramLength();
            }
            totalSize = offset;
        }
        return new LinkerContext(baseMap, global, totalSize);
    }

    /* ------------------------------------------------------------------------------------------ */
    /*  PASSO 2 – Validação de imports                                                           */
    /* ------------------------------------------------------------------------------------------ */

    private void verifyImportedSymbols(List<ObjectFile> modules, Map<String, Integer> global) {
        for (ObjectFile m : modules) {
            for (String imp : m.getImportedSymbols()) {
                if (!global.containsKey(imp))
                    throw new IllegalArgumentException("Import não resolvido: " + imp);
            }
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    /*  PASSO 3 – Código final, símbolos e relocations                                           */
    /* ------------------------------------------------------------------------------------------ */

    private FinalCodeData buildFinalCodeAndSymbols(List<ObjectFile> modules,
                                                   LinkerContext ctx,
                                                   boolean finalRelocation) {
        byte[] finalCode = new byte[ctx.totalSize];
        SymbolTable finalSymTab = new SymbolTable();
        List<RelocationRecord> finalRecs = new ArrayList<>();

        int globalOffset = 0;
        for (ObjectFile mod : modules) {
            byte[] code = mod.getObjectCode();
            int base   = ctx.baseMap.get(mod);

            /* 3.1 – aplica ou registra relocations */
            if (finalRelocation) {
                for (RelocationRecord r : mod.getRelocationRecords()) {
                    relocateInPlace(code, r, mod, base, ctx.globalSymbols);
                }
            } else {
                for (RelocationRecord r : mod.getRelocationRecords()) {
                    finalRecs.add(new RelocationRecord(
                            r.offset() + globalOffset,
                            r.symbol(),
                            r.length(),
                            r.pcRelative()
                    ));
                }
            }

            /* 3.2 – copia bytes */
            System.arraycopy(code, 0, finalCode, globalOffset, code.length);

            /* 3.3 – ajusta símbolos */
            for (Map.Entry<String, Symbol> e : mod.getSymbolTable().getAllSymbols().entrySet()) {
                String name = e.getKey();
                Symbol s    = e.getValue();
                int addr = finalRelocation ? base + s.address : globalOffset + s.address;
                finalSymTab.addSymbol(name, addr, s.isPublic);
            }
            globalOffset += code.length;
        }
        return new FinalCodeData(finalCode, finalSymTab, finalRecs);
    }

    /** Aplica uma única relocação diretamente no buffer do módulo. */
    private void relocateInPlace(byte[] code,
                                 RelocationRecord rec,
                                 ObjectFile module,
                                 int base,
                                 Map<String, Integer> global) {
        int off = rec.offset(), len = rec.length();
        if (off + len > code.length)
            throw new IllegalArgumentException("Relocation fora do buffer do módulo " + module.getProgramName());

        int original = 0;
        for (int i = 0; i < len; i++) original = (original << 8) | (code[off + i] & 0xFF);

        Integer symAddr = global.get(rec.symbol());
        if (symAddr == null) {
            Integer local = module.getSymbolTable().getSymbolAddress(rec.symbol());
            if (local == null) throw new IllegalStateException("Símbolo indefinido: " + rec.symbol());
            symAddr = base + local;
        }

        int value = original + symAddr - (rec.pcRelative() ? (len == 3 ? 3 : 4) : 0);
        for (int i = len - 1; i >= 0; i--) {
            code[off + i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    /*  PASSO 4 – Fonte combinado (debug)                                                        */
    /* ------------------------------------------------------------------------------------------ */

    private List<String> combineModuleSources(List<ObjectFile> modules) {
        List<String> combined = new ArrayList<>();
        for (ObjectFile m : modules) {
            if (m.getRawSourceCode() != null) combined.addAll(m.getRawSourceCode());
            combined.add(";---- Fim de " + m.getProgramName() + " ----");
        }
        return combined;
    }

    /* ------------------------------------------------------------------------------------------ */
    /*  PASSO 5 – Escrita do .obj textual                                                        */
    /* ------------------------------------------------------------------------------------------ */

    private void writeLinkedObjectFile(ObjectFile obj,
                                       String baseName,
                                       boolean includeMRecords) throws IOException {
        int start = obj.getStartAddress();
        byte[] code = obj.getObjectCode();
        SymbolTable symTab = obj.getSymbolTable();

        StringBuilder sb = new StringBuilder();

        /* H */
        sb.append(String.format("H^%-6s^%06X^%06X%n",
                fitName(obj.getProgramName()), start, code.length));

        /* D */
        for (Symbol s : symTab.getAllSymbols().values()) {
            if (s.isPublic) sb.append(String.format("D^%s^%06X%n", s.name, s.address));
        }

        /* T */
        for (TBlock b : buildTextRecords(start, code)) {
            StringBuilder hex = new StringBuilder();
            for (byte v : b.data) hex.append(String.format("%02X", v & 0xFF));
            sb.append(String.format("T^%06X^%02X^%s%n", b.startAddress, b.data.size(), hex));
        }

        /* M */
        if (includeMRecords) {
            for (RelocationRecord r : obj.getRelocationRecords()) {
                int addr = start + r.offset();
                sb.append(String.format("M^%06X^%02X^+%s%n", addr, r.length() * 2, r.symbol()));
            }
        }

        /* E */
        sb.append(String.format("E^%06X%n", start));

        FileUtils.writeFileInDir(Constants.SAVE_DIR, baseName + ".obj", sb.toString());
    }

    /* ------------------------------------------------------------------------------------------ */
    /*  HELPERs                                                                                   */
    /* ------------------------------------------------------------------------------------------ */

    private List<TBlock> buildTextRecords(int start, byte[] code) {
        List<TBlock> list = new ArrayList<>();
        final int MAX = 30;
        for (int i = 0; i < code.length; i += MAX) {
            int len = Math.min(MAX, code.length - i);
            TBlock b = new TBlock(start + i);
            for (int j = 0; j < len; j++) b.data.add(code[i + j]);
            list.add(b);
        }
        return list;
    }

    private String fitName(String n) {
        if (n == null || n.isBlank()) return "NONAME";
        return n.length() > 6 ? n.substring(0, 6) : n;
    }

    /* ------------------------------------------------------------------------------------------ */
    /*  DATA CLASSES                                                                              */
    /* ------------------------------------------------------------------------------------------ */

    /** Contexto intermediário do linker (bases, símbolos globais e tamanho total). */
    public record LinkerContext(Map<ObjectFile, Integer> baseMap,
                                 Map<String, Integer> globalSymbols,
                                 int totalSize) {}

    /** Resultado da linkedição: código final, tabela de símbolos e relocations. */
    public record FinalCodeData(byte[] code,
                                 SymbolTable symbolTable,
                                 List<RelocationRecord> relocationRecords) {}

    /** Bloco de até 30 bytes para gerar um registro T. */
    public static class TBlock {
        /** Endereço do primeiro byte do bloco. */
        public final int startAddress;
        /** Bytes que compõem o bloco. */
        public final List<Byte> data = new ArrayList<>();
        public TBlock(int startAddress) { this.startAddress = startAddress; }
    }
}
