package sicxesimulator.linker;

import sicxesimulator.models.ObjectFile;
import sicxesimulator.models.SymbolTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * O FirstPassLinker é responsável por:
 * 1. Calcular os offsets de relocação de cada módulo, com base no loadAddress e no tamanho dos módulos anteriores.
 * 2. Construir a tabela de símbolos global, ajustando os endereços dos símbolos de cada módulo.
 * 3. Determinar o tamanho total do programa linkado (em bytes) e construir uma identificação do programa.
 * Observação: Os endereços dos ObjectFile são originalmente em palavras (1 palavra = 3 bytes).
 */
class LinkerFirstPass {

    // Mapeia cada módulo ao seu offset de relocação (em bytes)
    private final Map<ObjectFile, Integer> moduleRelocationOffsets;
    // Tabela de símbolos global resultante da ligação
    private final SymbolTable globalSymbolTable;
    // Nome concatenado dos módulos, para identificação do programa
    private String programName;

    public LinkerFirstPass() {
        moduleRelocationOffsets = new HashMap<>();
        globalSymbolTable = new SymbolTable();
        programName = "";
    }

    /**
     * Processa a primeira passagem do linker.
     *
     * @param objectFiles Lista de módulos (ObjectFile) a serem linkados.
     * @param loadAddress Endereço de carga inicial (em bytes) para o programa linkado.
     */
    public void process(List<ObjectFile> objectFiles, int loadAddress) {
        int cumulativeOffset = 0;
        StringBuilder programNameBuilder = new StringBuilder();

        for (ObjectFile obj : objectFiles) {
            // O startAddress do ObjectFile é armazenado em palavras; convertemos para bytes.
            int moduleOriginalStartBytes = obj.getStartAddress() * 3;
            // Endereço de carga do módulo: carga inicial + bytes já utilizados.
            int moduleLoadAddress = loadAddress + cumulativeOffset;
            // Offset de relocação: diferença entre o novo endereço e o endereço original (em bytes).
            int relocationOffset = moduleLoadAddress - moduleOriginalStartBytes;
            moduleRelocationOffsets.put(obj, relocationOffset);

            // Concatena o nome do módulo para identificação.
            programNameBuilder.append(obj.getProgramName()).append("_");

            // Atualiza a tabela de símbolos global:
            // Cada símbolo do módulo tem seu endereço (em palavras) convertido para bytes,
            // ajustado com o offset e convertido novamente para palavras.
            for (Entry<String, Integer> entry : obj.getSymbolTable().getSymbols().entrySet()) {
                String symbol = entry.getKey();
                int originalAddressBytes = entry.getValue() * 3;
                int newAddressBytes = originalAddressBytes + relocationOffset;
                globalSymbolTable.addSymbol(symbol, newAddressBytes / 3);
            }

            // Atualiza o deslocamento acumulado: soma o tamanho do código objeto do módulo (em bytes)
            cumulativeOffset += obj.getObjectCode().length;
        }
        programName = programNameBuilder.toString();
    }

    // Getters para acesso aos resultados da primeira passagem

    /**
     * Retorna o mapeamento de cada módulo ao seu offset de relocação (em bytes).
     */
    public Map<ObjectFile, Integer> getModuleRelocationOffsets() {
        return moduleRelocationOffsets;
    }

    /**
     * Retorna a tabela de símbolos global construída.
     */
    public SymbolTable getGlobalSymbolTable() {
        return globalSymbolTable;
    }

    /**
     * Retorna o nome concatenado dos módulos, representando o programa linkado.
     */
    public String getProgramName() {
        return programName;
    }}
