package sicxesimulator.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // Adiciona um serialVersionUID
    private final int startAddress; // Endereço de início em palavras (cada palavra = 3 bytes)
    private final byte[] machineCode; // Código objeto em bytes
    private List<String> rawSourceCode; // Código-fonte original
    private List<String> processedSourceCode; // Código-fonte com macros expandidos
    private final SymbolTable symbolTable;
    private final String fileName;
    private boolean isRelocated;

    public ObjectFile(int startAddress, byte[] machineCode, SymbolTable symbolTable, String fileName) {
        if (machineCode == null || symbolTable == null || fileName == null) {
            throw new IllegalArgumentException("Nenhum parâmetro pode ser nulo.");
        }
        this.startAddress = startAddress;
        this.machineCode = machineCode;
        this.symbolTable = symbolTable;
        this.fileName = fileName;
        this.rawSourceCode = new ArrayList<>();
        this.processedSourceCode = new ArrayList<>();
        this.isRelocated = false;
    }

    public boolean isRelocated() {
        return isRelocated;
    }

    public void setRelocated(boolean relocated) {
        isRelocated = relocated;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public byte[] getMachineCode() {
        return machineCode;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public String getFilename() {
        return fileName;
    }

    public int getProgramLength() {
        return machineCode.length;
    }

    public List<String> getRawSourceCode() {
        return rawSourceCode;
    }

    public List<String> getProcessedSourceCode() {
        return processedSourceCode;
    }

    @Override
    public String toString() {
        String stringBuilder = "Nome do programa: " + fileName + "\n" +
                "Endereço inicial: " + String.format("%04X", startAddress) + "\n" +
                "Tamanho do programa: " + getProgramLength() + " bytes\n";
        return stringBuilder.trim();
    }

    public void setRawSourceCode(List<String> rawSourceCode) {
        this.rawSourceCode = rawSourceCode;
    }

    public void setProcessedSourceCode(List<String> processedSourceCode) {
        this.processedSourceCode = processedSourceCode;
    }
}
