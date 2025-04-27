package sicxesimulator.software.linker;

import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.Symbol;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.software.data.RelocationRecord;
import sicxesimulator.software.loader.Loader;
import sicxesimulator.common.utils.Constants;
import sicxesimulator.common.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static sicxesimulator.software.data.ObjectFile.ObjectFileOrigin.LINKED_MODULES;

/**
 * Responsável por combinar múltiplos módulos objeto em um único {@link ObjectFile} final.
 *
 * <p>Suporta dois modos de operação:</p>
 * <ul>
 *   <li><b>finalRelocation=true:</b> Aplica realocação final diretamente no array de bytes.
 *       Gera um objeto totalmente resolvido, pronto para execução.</li>
 *   <li><b>finalRelocation=false:</b> Gera um objeto com realocações pendentes,
 *       que serão aplicadas posteriormente pelo {@link Loader}.</li>
 * </ul>
 *
 * <p>Durante o processo de linkedição, os seguintes passos são realizados:</p>
 * <ol>
 *   <li>Atribuição de endereços base a cada módulo</li>
 *   <li>Validação de símbolos importados</li>
 *   <li>Geração do array de byte final (com ou sem relocação final)</li>
 *   <li>Construção da tabela global de símbolos</li>
 *   <li>Geração de registros H/D/T/M/E no formato textual</li>
 *   <li>Escrita dos arquivos .obj e .meta no disco</li>
 * </ol>
 */
public class Linker {

    /**
     * Executa o processo de linkedição de uma lista de módulos em um único objeto final.
     *
     * @param modules Lista de módulos {@link ObjectFile} a serem linkados.
     * @param finalRelocation Indica se a realocação final deve ser aplicada diretamente.
     * @param loadAddress Endereço base para carga, usado apenas se finalRelocation=true.
     * @param outputFileName Nome base do arquivo .obj resultante (sem extensão).
     * @return Objeto final {@link ObjectFile} resultante do processo.
     */
    public ObjectFile linkModules(List<ObjectFile> modules, boolean finalRelocation, int loadAddress, String outputFileName) {
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("Nenhum modulo para linkar.");
        }

        // Passo 1: Atribuir base para cada módulo e montar tabela global de símbolos
        LinkerContext context = assignBasesAndGlobalSymbols(modules, finalRelocation);

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
                : modules.getFirst().getStartAddress();

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
     * Atribui endereços base para os módulos e constrói a tabela global de símbolos exportados.
     *
     * <p>O comportamento varia conforme o modo de realocação:</p>
     * <ul>
     *   <li><b>finalRelocation=true:</b> Cada módulo usa seu próprio START. Os símbolos exportados
     *   são ajustados com base nesse START.</li>
     *   <li><b>finalRelocation=false:</b> Módulos são empilhados sequencialmente, e o endereço de cada símbolo
     *   exportado é ajustado com o offset global.</li>
     * </ul>
     *
     * @return {@link LinkerContext} contendo as informações globais necessárias para a montagem do objeto final.
     */
    private LinkerContext assignBasesAndGlobalSymbols(List<ObjectFile> modules, boolean finalRelocation) {
        Map<ObjectFile, Integer> baseMap = new HashMap<>();
        Map<String, Integer> globalSymbols = new HashMap<>();
        Set<String> exportedSymbols = new HashSet<>();

        int totalSize;
        if (finalRelocation) {
            // Modo ABSOLUTO: cada módulo usa seu próprio START (interpretado como decimal ou hexadecimal conforme Parser)
            // TotalSize = (maior (start + tamanho)) - (menor start)
            int minStart = Integer.MAX_VALUE;
            int maxEnd = 0;
            for (ObjectFile mod : modules) {
                int start = mod.getStartAddress();
                int end = start + mod.getProgramLength();
                if (start < minStart) {
                    minStart = start;
                }
                if (end > maxEnd) {
                    maxEnd = end;
                }
                // A base de cada módulo é o próprio seu START
                baseMap.put(mod, start);

                // Copia símbolos exportados
                for (var e : mod.getSymbolTable().getAllSymbols().entrySet()) {
                    String symName = e.getKey();
                    Symbol sym = e.getValue();
                    if (sym.isPublic) {
                        int globalAddr = start + sym.address;
                        if (!exportedSymbols.add(symName)) {
                            throw new IllegalStateException("Símbolo " + symName + " exportado por mais de um módulo.");
                        }
                        globalSymbols.put(symName, globalAddr);
                    }
                }
            }
            totalSize = maxEnd - minStart;
        } else {
            // Modo RELOCÁVEL: os módulos são empilhados
            int maxAddressUsed = 0;
            for (ObjectFile mod : modules) {
                int base = maxAddressUsed; // coloca o módulo na sequência
                baseMap.put(mod, base);

                // Copia símbolos exportados (ajustando com o offset global)
                for (var e : mod.getSymbolTable().getAllSymbols().entrySet()) {
                    String symName = e.getKey();
                    Symbol sym = e.getValue();
                    if (sym.isPublic) {
                        int globalAddr = maxAddressUsed + sym.address;
                        if (!exportedSymbols.add(symName)) {
                            throw new IllegalStateException("Símbolo duplicado: " + symName);
                        }
                        globalSymbols.put(symName, globalAddr);
                    }
                }
                maxAddressUsed += mod.getProgramLength();
            }
            totalSize = maxAddressUsed;
        }

        return new LinkerContext(baseMap, globalSymbols, totalSize);
    }

    /**
     * Valida se todos os símbolos importados por cada módulo existem na tabela global.
     *
     * @throws IllegalArgumentException se algum símbolo importado não for resolvido.
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
     * Constrói o array de bytes final e a tabela de símbolos global.
     *
     * <p>Aplica a realocação se finalRelocation=true, ou gera {@link RelocationRecord} com offsets ajustados.</p>
     *
     * @return {@link FinalCodeData} com os dados de saída do processo de linkagem.
     */
    private FinalCodeData buildFinalCodeAndSymbols(List<ObjectFile> modules, LinkerContext context, boolean finalRelocation) {
        byte[] finalCode = new byte[context.totalSize];
        SymbolTable finalSymTab = new SymbolTable();
        List<RelocationRecord> finalRecs = new ArrayList<>();

        int globalOffset = 0;

        for (ObjectFile mod : modules) {
            byte[] code = mod.getObjectCode();
            int base = context.baseMap.get(mod);

            // Se finalRelocation=true, corrigimos cada RelocationRecord no array do módulo
            if (finalRelocation) {
                for (RelocationRecord rec : mod.getRelocationRecords()) {
                    fixAddressInCode(code, rec, mod, base, context.globalSymbols);
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
     * Aplica a realocação diretamente no array de bytes do módulo.
     *
     * <p>Resolve o símbolo, soma seu valor ao campo indicado, e grava o valor realocado
     * de volta no array. Se o campo for relativo ao PC, aplica o ajuste (-3 ou -4).</p>
     *
     * @throws IllegalArgumentException se o offset da relocação estiver fora do array.
     */
    private void fixAddressInCode(byte[] code, RelocationRecord rec, ObjectFile module, int moduleBase, Map<String, Integer> globalSymbols) {
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
     * Combina as linhas de código-fonte de todos os módulos em uma única lista.
     *
     * <p>Este métodO é opcional e utilizado principalmente para fins de depuração,
     * permitindo rastrear a origem do código montado a partir dos diferentes módulos.</p>
     *
     * <p>Para cada módulo com código-fonte original disponível ({@code getRawSourceCode() != null}),
     * suas linhas são adicionadas à lista, seguidas por um comentário delimitador com o nome do programa.</p>
     *
     * @param modules Lista de módulos {@link ObjectFile} contendo os códigos-fonte originais.
     * @return Lista combinada de todas as linhas de código-fonte, separadas por marcações de fim de módulo.
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
     * Gera o conteúdo textual do arquivo .obj no formato H/D/T/M/E e grava no disco.
     *
     * @param finalObj Objeto final {@link ObjectFile} contendo os dados a serem salvos.
     * @param outputFileName Nome base do arquivo (sem extensão).
     * @param finalRelocation Indica se relocations devem ser incluídos.
     * @throws IOException Em caso de falha ao escrever no disco.
     */
    private void writeLinkedObjectFile(ObjectFile finalObj, String outputFileName, boolean finalRelocation) throws IOException {
        int startAddress = finalObj.getStartAddress();
        byte[] code = finalObj.getObjectCode();
        int progLen = code.length;
        String progName = finalObj.getProgramName();
        SymbolTable finalSym = finalObj.getSymbolTable();

        StringBuilder content = new StringBuilder();
        // H record (Header)
        content.append(
                String.format("H^%-6s^%06X^%06X\n", fitProgramName(progName), startAddress, progLen)
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
        List<TBlock> tblocks = buildTextRecords(startAddress, code);
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
                int address = startAddress + rec.offset();
                int halfBytes = rec.length() * 2;
                // +SÍMBOLO
                String mLine = String.format(
                        "M^%06X^%02X^+%s", address, halfBytes, rec.symbol()
                );
                content.append(mLine).append("\n");
            }
        }

        // E record (End)
        content.append(String.format("E^%06X\n", startAddress));

        // Grava no disco
        FileUtils.writeFileInDir(Constants.SAVE_DIR, outputFileName, content.toString());
    }

    /**
     * Garante que o nome do programa tenha no máximo 6 caracteres.
     *
     * <p>Este métodO é usado para gerar o cabeçalho do arquivo objeto (registro H),
     * onde o nome do programa é limitado a 6 posições.</p>
     *
     * <p>Se o nome for {@code null}, retorna o identificador "NONAME".
     * Caso contrário, <b>trunca o nome para 6 caracteres</b> ou o mantém como está.</p>
     *
     * @param name Nome original do programa.
     * @return Nome ajustado com no máximo 6 caracteres.
     */
    private String fitProgramName(String name) {
        if (name == null) return "NONAME";
        return (name.length() > 6) ? name.substring(0, 6) : name;
    }

    /**
     * Constrói os registros de texto (T) para o arquivo objeto.
     *
     * <p>Divide o array de bytes em blocos de até 30 bytes cada, como especificado pelo
     * formato dos registros de texto no SIC/XE.</p>
     *
     * <p>Cada bloco resultante é armazenado em um {@link TBlock}, contendo o endereço inicial
     * e a sequência de bytes correspondente.</p>
     *
     * @param start Endereço base onde o código será carregado.
     * @param code Array de bytes do código final.
     * @return Lista de blocos T prontos para serem gravados no .obj.
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

    /**
     * Armazena o contexto intermediário da linkedição.
     *
     * <p>Contém informações como:</p>
     * <ul>
     *   <li>Mapa de endereços base para cada módulo</li>
     *   <li>Tabela global de símbolos exportados</li>
     *   <li>Tamanho total acumulado do código</li>
     * </ul>
     */
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

    /**
     * Estrutura de saída do processo de linkagem.
     *
     * <p>Contém o código final montado, a tabela de símbolos consolidada e
     * os registros de relocação (se finalRelocation=false).</p>
     */
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

    /**
     * Representa um bloco de texto (registro T) do arquivo objeto SIC/XE.
     *
     * <p>Armazena o endereço inicial do bloco e os dados em formato de lista de bytes.
     * Cada bloco corresponde a no máximo 30 bytes, conforme especificação do montador.</p>
     */
    public static class TBlock {
        int startAddress;
        List<Byte> data = new ArrayList<>();

        public TBlock(int start) {
            this.startAddress = start;
        }
    }
}
