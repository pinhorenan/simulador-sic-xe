package sicxesimulator.models;

import java.util.ArrayList;
import java.util.List;

public class IntermediateRepresentation {
    private final SymbolTable symbolTable;
    private final List<AssemblyLine> assemblyLines;
    private String programName;
    private List<String> rawSourceLines;
    private int startAddress;

    // Construtor que permite definir o endereço de início
    public IntermediateRepresentation(int startAddress) {
        this.symbolTable = new SymbolTable();
        this.assemblyLines = new ArrayList<>();
        this.startAddress = startAddress;
    }

    // Construtor padrão com startAddress = 0
    public IntermediateRepresentation() {
        this(0);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public List<AssemblyLine> getAssemblyLines() {
        return assemblyLines;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(int startAddress) {
        this.startAddress = startAddress;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    /**
     * Adiciona uma nova linha de assembly à representação.
     */
    public void addAssemblyLine(AssemblyLine line) {
        assemblyLines.add(line);
    }

    /**
     * Registra um símbolo na tabela com seu endereço.
     */
    public void addSymbol(String symbol, int address) {
        symbolTable.addSymbol(symbol, address);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Start Address: ").append(Integer.toHexString(startAddress)).append("\n");
        sb.append("Symbol Table: ").append(symbolTable).append("\n");
        sb.append("Assembly Lines:\n");
        for (AssemblyLine line : assemblyLines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public void setRawSourceCode(List<String> rawSourceLines) {
        this.rawSourceLines = rawSourceLines;
    }

    public List<String> getRawSourceLines() {
        return rawSourceLines;
    }
}
