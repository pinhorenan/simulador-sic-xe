package sicxesimulator.models;

import java.io.*;
import java.util.List;

public class ObjectFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int startAddress;
    private final byte[] machineCode;
    private final List<String> rawSourceCode;
    private final SymbolTable symbolTable;
    private final String fileName;
    private boolean isRelocated;

    public ObjectFile(int startAddress, byte[] machineCode, SymbolTable symbolTable, String fileName, List<String> rawSourceCode) {
        if (machineCode == null || symbolTable == null || fileName == null) {
            throw new IllegalArgumentException("Nenhum parâmetro pode ser nulo.");
        }
        this.startAddress = startAddress;
        this.machineCode = machineCode;
        this.symbolTable = symbolTable;
        this.fileName = fileName;
        this.rawSourceCode = rawSourceCode;
        this.isRelocated = false;
    }

    /**
     * Carrega um objeto "ObjectFile" de um arquivo salvo no disco.
     *
     * @param file O arquivo ".obj" a ser carregado.
     * @return Um objeto "ObjectFile" correspondente ao arquivo.
     * @throws IOException Se ocorrer um erro ao ler o arquivo.
     */
    public static ObjectFile loadFromFile(File file) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ObjectFile) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Formato do arquivo inválido: " + file.getName(), e);
        }
    }

    /**
     * Salva o objeto "ObjectFile" no disco em formato serializado.
     *
     * @param file O arquivo onde o objeto será salvo.
     * @throws IOException Se ocorrer um erro ao salvar o arquivo.
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    public boolean isRelocated() {
        return isRelocated;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getProgramLength() {
        return machineCode.length;
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

    public List<String> getRawSourceCode() {
        return rawSourceCode;
    }

    public void setRelocated(boolean relocated) {
        isRelocated = relocated;
    }

    @Override
    public String toString() {
        String stringBuilder = "Nome do programa: " + fileName + "\n" +
                "Endereço inicial: " + String.format("%04X", startAddress) + "\n" +
                "Tamanho do programa: " + getProgramLength() + " bytes\n";
        return stringBuilder.trim();
    }
}