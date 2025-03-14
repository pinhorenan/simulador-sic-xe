package sicxesimulator.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IntermediateRepresentation {
    private final List<AssemblyLine> assemblyLines;
    private List<String> rawSourceLines; // TODO: Seria bom setar na hora da montagem e botar como final, ou não?
    private final SymbolTable symbolTable;
    private final Set<String> importedSymbols;
    private String programName;
    private int startAddress;

    // Construtor que permite definir o endereço de início
    public IntermediateRepresentation(int startAddress) {
        this.symbolTable = new SymbolTable();
        this.importedSymbols = new HashSet<>();
        this.assemblyLines = new ArrayList<>();
        this.startAddress = startAddress;
    }

    // Construtor padrão com startAddress = 0
    public IntermediateRepresentation() {
        this(0);
    }

    /// ===== Métodos Getters ===== ///

    public String getProgramName() {
        return programName;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public Set<String> getImportedSymbols() {
        return importedSymbols;
    }

    public List<AssemblyLine> getAssemblyLines() {
        return assemblyLines;
    }

    public List<String> getRawSourceLines() {
        return rawSourceLines;
    }

    /// ===== Métodos Setters ===== ///

    public void setStartAddress(int startAddress) {
        this.startAddress = startAddress;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public void setRawSourceCode(List<String> rawSourceLines) {
        this.rawSourceLines = rawSourceLines;
    }

    /// ===== Métodos de adição de objetos ===== ///

    public void addAssemblyLine(AssemblyLine line) {
        assemblyLines.add(line);
    }

    public void addLocalSymbol(String symbol, int address) {
        symbolTable.addSymbol(symbol, address);
    }

    public void addExportedSymbol(String symbol) {
        // Se esse símbolo já existe na symbolTable, set isPublic = true.
        // Caso contrário, registra address = 0 e isPublic = true, e depois na hora que ver a definição real (label), atualiza o address.
        if (symbolTable.contains(symbol)) {
            symbolTable.getSymbolInfo(symbol).isPublic = true;
        } else {
            symbolTable.addSymbol(symbol, 0, true);
        }
    }

    public void addImportedSymbol(String symbol) {
        importedSymbols.add(symbol);
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
}
