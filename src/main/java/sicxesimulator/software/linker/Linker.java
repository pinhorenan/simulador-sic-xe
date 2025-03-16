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
 * Responsável por linkar uma lista de módulos (ObjectFile) em um único arquivo final.
 * Essa implementação descentraliza as etapas de linking em métodos auxiliares.
 */
public class Linker {

    /**
     * Linka uma lista de módulos em um único ObjectFile final.
     *
     * @param modules         Lista de módulos a serem linkados.
     * @param finalRelocation Se true, o arquivo final já terá os endereços corrigidos (sem relocation records).
     * @param loadAddress     Endereço de carga do programa final.
     * @param outputFileName  Nome do arquivo final (sem extensão).
     * @return ObjectFile final gerado.
     */
    public ObjectFile linkModules(List<ObjectFile> modules, boolean finalRelocation, int loadAddress, String outputFileName) {
        if (modules.isEmpty()) {
            throw new IllegalArgumentException("Nenhum módulo para linkar.");
        }

        // Etapa 1: Atribuir bases aos módulos e construir a tabela de símbolos globais
        LinkerContext context = assignBasesAndGlobalSymbols(modules, finalRelocation, loadAddress);

        // Etapa 2: Verificar que todos os símbolos importados foram exportados em algum módulo
        verifyImportedSymbols(modules, context.globalSymbols);

        // Etapa 3: Gerar o código final concatenado, relocando (ou reagrupando) relocation records conforme o caso
        FinalCodeData finalData = buildFinalCodeAndSymbols(modules, context, finalRelocation);

        // Etapa 4: Determinar o endereço inicial final e combinar os códigos fonte (apenas para fins de exibição)
        int finalStart = finalRelocation ? loadAddress : modules.getFirst().getStartAddress();
        List<String> combinedSource = combineModuleSources(modules);

        // Etapa 5: Criar o ObjectFile final
        ObjectFile finalObject = new ObjectFile(
                finalStart,
                finalData.code,
                finalData.symbolTable,
                outputFileName,
                combinedSource,
                Collections.emptySet(),
                finalRelocation ? Collections.emptyList() : finalData.relocationRecords
        );
        finalObject.setOrigin(LINKED_MODULES);
        finalObject.setFullyRelocated(finalRelocation);

        // Etapa 6: Gerar os arquivos de saída (.obj e .meta)
        String objFileName = outputFileName + ".obj";
        try {
            writeLinkedObjectFile(finalObject, objFileName, finalRelocation);
        } catch (IOException e) {
            System.err.println("Falha ao gravar .obj final textual: " + e.getMessage());
        }
        File metaFile = new File(Constants.SAVE_DIR, outputFileName + ".meta");
        finalObject.saveToFile(metaFile);

        return finalObject;
    }

    /**
     * Etapa 1: Atribui base a cada módulo e monta a tabela global de símbolos.
     *
     * @param modules         Lista de módulos.
     * @param finalRelocation Se true, os endereços serão absolutos.
     * @param loadAddress     Endereço de carga inicial.
     * @return Contexto com o mapa de bases, tabela global e tamanho total.
     */
    private LinkerContext assignBasesAndGlobalSymbols(List<ObjectFile> modules, boolean finalRelocation, int loadAddress) {
        int currentBase = loadAddress;
        Map<ObjectFile, Integer> baseMap = new HashMap<>();
        Map<String, Integer> globalSymbols = new HashMap<>();
        Set<String> alreadyExported = new HashSet<>();
        int totalSize = 0;

        for (ObjectFile module : modules) {
            baseMap.put(module, currentBase);
            for (var entry : module.getSymbolTable().getAllSymbols().entrySet()) {
                String symbolName = entry.getKey();
                Symbol symbol = entry.getValue();
                if (symbol.isPublic) {
                    int globalAddress = finalRelocation ? (currentBase + symbol.address) : symbol.address;
                    if (alreadyExported.contains(symbolName)) {
                        throw new IllegalStateException("Símbolo " + symbolName + " exportado por mais de um módulo.");
                    }
                    alreadyExported.add(symbolName);
                    globalSymbols.put(symbolName, globalAddress);
                }
            }
            totalSize += module.getProgramLength();
            currentBase += module.getProgramLength();
        }
        return new LinkerContext(baseMap, globalSymbols, totalSize);
    }

    /**
     * Etapa 2: Verifica que todos os símbolos importados foram definidos em algum módulo.
     * @param modules       Lista de módulos.
     * @param globalSymbols Tabela de símbolos globais.
     */
    private void verifyImportedSymbols(List<ObjectFile> modules, Map<String, Integer> globalSymbols) {
        for (ObjectFile module : modules) {
            for (String symbol : module.getImportedSymbols()) {
                if (!globalSymbols.containsKey(symbol)) {
                    throw new IllegalArgumentException("Símbolo importado [" + symbol + "] não foi definido em nenhum módulo.");
                }
            }
        }
    }

    /**
     * Etapa 3: Concatena os códigos dos módulos, corrige os endereços e agrupa os relocation records se necessário.
     *
     * @param modules         Lista de módulos.
     * @param context         Contexto com bases e totalSize.
     * @param finalRelocation Se true, corrige os endereços no código; caso contrário, agrupa os relocation records.
     * @return Dados finais contendo o código concatenado, relocation records e a symbolTable final.
     */
    private FinalCodeData buildFinalCodeAndSymbols(List<ObjectFile> modules, LinkerContext context, boolean finalRelocation) {
        byte[] finalCode = new byte[context.totalSize];
        SymbolTable finalSymbolTable = new SymbolTable();
        List<RelocationRecord> finalRelocs = new ArrayList<>();
        int offsetGlobal = 0;

        for (ObjectFile module : modules) {
            byte[] code = module.getObjectCode();
            int base = context.baseMap.get(module);

            if (finalRelocation) {
                for (RelocationRecord rec : module.getRelocationRecords()) {
                    fixAddressInCode(code, rec, module, base, context.globalSymbols);
                }
            } else {
                for (RelocationRecord rec : module.getRelocationRecords()) {
                    int newOffset = rec.offset() + offsetGlobal;
                    RelocationRecord newRec = new RelocationRecord(newOffset, rec.symbol(), rec.length(), rec.pcRelative());
                    finalRelocs.add(newRec);
                }
            }

            System.arraycopy(code, 0, finalCode, offsetGlobal, code.length);
            for (var entry : module.getSymbolTable().getAllSymbols().entrySet()) {
                String symbolName = entry.getKey();
                Symbol symbol = entry.getValue();
                int finalAddress = finalRelocation ? (base + symbol.address) : (offsetGlobal + symbol.address);
                finalSymbolTable.addSymbol(symbolName, finalAddress, symbol.isPublic);
            }
            offsetGlobal += code.length;
        }
        return new FinalCodeData(finalCode, finalSymbolTable, finalRelocs);
    }

    /**
     * Etapa 4: Combina os códigos-fonte de cada módulo para fins de exibição.
     * @param modules Lista de módulos.
     * @return Lista de linhas de código-fonte concatenadas.
     */
    private List<String> combineModuleSources(List<ObjectFile> modules) {
        List<String> combined = new ArrayList<>();
        for (ObjectFile module : modules) {
            if (module.getRawSourceCode() != null) {
                combined.addAll(module.getRawSourceCode());
            }
            combined.add(";---- Fim do Módulo " + module.getProgramName() + " ----");
        }
        return combined;
    }

    /**
     * Corrige um endereço no código de um módulo, com base em um RelocationRecord.
     * @param code Código do módulo.
     * @param rec Registro de relocação.
     * @param module Módulo atual.
     * @param moduleBase Base do módulo.
     * @param globalSymbols Tabela de símbolos globais.
     */
    private void fixAddressInCode(byte[] code, RelocationRecord rec, ObjectFile module, int moduleBase, Map<String, Integer> globalSymbols) {
        int offset = rec.offset();
        int length = rec.length();
        if (offset + length > code.length) {
            throw new IllegalArgumentException("RelocationRecord fora dos limites do código do módulo " + module.getProgramName());
        }
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 8) | (code[offset + i] & 0xFF);
        }
        String symbol = rec.symbol();
        Integer resolvedAddress = globalSymbols.get(symbol);
        if (resolvedAddress == null) {
            Integer localAddr = module.getSymbolTable().getSymbolAddress(symbol);
            if (localAddr != null) {
                resolvedAddress = moduleBase + localAddr;
            } else {
                throw new IllegalArgumentException("Símbolo [" + symbol + "] não encontrado nem em globalSymbols nem local no módulo " + module.getProgramName());
            }
        }
        int newValue = value + resolvedAddress;
        if (rec.pcRelative()) {
            newValue -= 3; // Ajuste para PC-relative
        }
        int temp = newValue;
        for (int i = length - 1; i >= 0; i--) {
            code[offset + i] = (byte) (temp & 0xFF);
            temp >>>= 8;
        }
    }

    /**
     * Escreve o arquivo .obj final com os registros H, D, T, M e E.
     * @param finalObj Objeto final.
     * @param outputFileName Nome do arquivo de saída.
     * @param finalRelocation Se true, não inclui registros de relocação.
     * @throws IOException Em caso de erro ao gravar o arquivo.
     */
    private void writeLinkedObjectFile(ObjectFile finalObj, String outputFileName, boolean finalRelocation) throws IOException {
        int startAddr = finalObj.getStartAddress();
        byte[] code = finalObj.getObjectCode();
        int programLength = code.length;
        String progName = finalObj.getProgramName();
        SymbolTable finalSymTab = finalObj.getSymbolTable();

        StringBuilder content = new StringBuilder();
        content.append(String.format("H^%-6s^%06X^%06X\n", fitProgramName(progName), startAddr, programLength));

        List<Symbol> exportedSymbols = new ArrayList<>();
        for (Symbol symbol : finalSymTab.getAllSymbols().values()) {
            if (symbol.isPublic) {
                exportedSymbols.add(symbol);
            }
        }
        if (!exportedSymbols.isEmpty()) {
            StringBuilder dRec = new StringBuilder("D");
            for (Symbol symbol : exportedSymbols) {
                dRec.append("^").append(symbol.name)
                        .append("^").append(String.format("%06X", symbol.address));
            }
            content.append(dRec).append("\n");
        }

        List<TBlock> tblocks = buildTextRecords(startAddr, code);
        for (TBlock block : tblocks) {
            StringBuilder hex = new StringBuilder();
            for (byte b : block.data) {
                hex.append(String.format("%02X", b & 0xFF));
            }
            int lengthBlock = block.data.size();
            String textRec = String.format("T^%06X^%02X^%s", block.startAddress, lengthBlock, hex);
            content.append(textRec).append("\n");
        }

        if (!finalRelocation) {
            List<RelocationRecord> finalRecs = finalObj.getRelocationRecords();
            for (RelocationRecord rec : finalRecs) {
                int address = startAddr + rec.offset();
                int halfBytes = rec.length() * 2;
                String mLine = String.format("M^%06X^%02X^+%s", address, halfBytes, rec.symbol());
                content.append(mLine).append("\n");
            }
        }
        content.append(String.format("E^%06X\n", startAddr));
        FileUtils.writeFileInDir(Constants.SAVE_DIR, outputFileName, content.toString());
    }

    /**
     * Ajusta o nome do programa para ter no máximo 6 caracteres.
     * @param name Nome do programa.
     * @return Nome ajustado.
     */
    private String fitProgramName(String name) {
        if (name == null) return "NONAME";
        return (name.length() > 6) ? name.substring(0, 6) : name;
    }


    private List<TBlock> buildTextRecords(int start, byte[] code) {
        List<TBlock> blocks = new ArrayList<>();
        final int MAX_BYTES = 30;
        int index = 0;
        while (index < code.length) {
            int blockLen = Math.min(MAX_BYTES, code.length - index);
            TBlock block = new TBlock(start + index);
            for (int i = 0; i < blockLen; i++) {
                block.data.add(code[index + i]);
            }
            blocks.add(block);
            index += blockLen;
        }
        return blocks;
    }

    /**
     * Representa um bloco de texto (T record) do arquivo .obj.
     * Essa classe pode ser extraída para um pacote de registros se necessário.
     */
    public static class TBlock {
        int startAddress;
        List<Byte> data = new ArrayList<>();
        public TBlock(int start) {
            this.startAddress = start;
        }
    }

    /**
     * Classe interna que agrega dados do contexto de linking.
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
     * Classe interna que encapsula os dados finais do código concatenado.
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
}
