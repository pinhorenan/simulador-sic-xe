package sicxesimulator.software.data;

import java.util.List;
import java.util.Set;

/**
 * Representa o resultado da primeira passagem do montador.
 * <p>
 * Essa estrutura contém todas as informações necessárias para a segunda
 * passagem, incluindo as linhas de assembly analisadas, tabela de símbolos,
 * símbolos importados, nome do programa e endereço de início.
 *
 * @param assemblyLines Lista de linhas de código montadas.
 * @param rawSourceLines Código-fonte original (para referência e exibição).
 * @param symbolTable Tabela de símbolos contendo definições locais e globais.
 * @param importedSymbols Conjunto de símbolos externos referenciados.
 * @param programName Nome do programa (rótulo START).
 * @param startAddress Endereço de início do programa.
 */

public record IntermediateRepresentation(
        List<AssemblyLine> assemblyLines,
        List<String> rawSourceLines,
        SymbolTable symbolTable,
        Set<String> importedSymbols,
        String programName,
        int startAddress
) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Endereco de inicio: ").append(Integer.toHexString(startAddress)).append("\n");
        sb.append("Symbol Table: ").append(symbolTable).append("\n");
        sb.append("Assembly Lines:\n");
        for (AssemblyLine line : assemblyLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
