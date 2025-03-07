package sicxesimulator.assembler.models;

public class ObjectFile {
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
        StringBuilder sb = new StringBuilder();
        sb.append("Endereço inicial: ").append(String.format("%04X", startAddress)).append("\n");
        sb.append("Tamanho do programa: ").append(getProgramLength()).append(" bytes\n");
        sb.append("Object Code:\n");
        int counter = 0;
        for (byte b : objectCode) {
            sb.append(String.format("%02X ", b));
            counter++;
            if (counter == 9) {
                sb.append("\n");
                counter = 0;
            }
        }
        return sb.toString().trim();
    }
}
