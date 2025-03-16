package sicxesimulator.software.assembler.data;

import sicxesimulator.data.SymbolTable;
import java.util.List;
import java.util.Set;

/**
 * Representação intermediária do código-fonte após a primeira passagem do montador.
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
        sb.append("Endereço de início: ").append(Integer.toHexString(startAddress)).append("\n");
        sb.append("Symbol Table: ").append(symbolTable).append("\n");
        sb.append("Assembly Lines:\n");
        for (AssemblyLine line : assemblyLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
