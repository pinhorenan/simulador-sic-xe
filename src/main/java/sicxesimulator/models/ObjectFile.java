package sicxesimulator.models;

import java.io.Serial;
import java.io.Serializable;

public class ObjectFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // Adiciona um serialVersionUID
    private final int startAddress; // Endereço de início em palavras (cada palavra = 3 bytes)
    private final byte[] objectCode; // Código objeto em bytes
    private final SymbolTable symbolTable;
    private final String fileName;

    public ObjectFile(int startAddress, byte[] objectCode, SymbolTable symbolTable, String fileName) {
        if (objectCode == null || symbolTable == null || fileName == null) {
            throw new IllegalArgumentException("Nenhum parâmetro pode ser nulo.");
        }
        this.startAddress = startAddress;
        this.objectCode = objectCode;
        this.symbolTable = symbolTable;
        this.fileName = fileName;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public byte[] getObjectCode() {
        return objectCode;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public String getFilename() {
        return fileName;
    }

    public int getProgramLength() {
        return objectCode.length;
    }

    @Override
    public String toString() {
        String stringBuilder = "Nome do programa: " + fileName + "\n" +
                "Endereço inicial: " + String.format("%04X", startAddress) + "\n" +
                "Tamanho do programa: " + getProgramLength() + " bytes\n";
        return stringBuilder.trim();
    }
}
