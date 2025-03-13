package sicxesimulator.models;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Representa um módulo objeto gerado pelo montador,
 * contendo o code, a tabela de símbolos local, e:
 * - importedSymbols: símbolos externos (nome -> offset? Ou contagem?)
 * - relocationRecords: lista de posições a corrigir
 */
public class ObjectFile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int startAddress;
    private final byte[] machineCode;
    private final SymbolTable symbolTable;
    private final String fileName;
    private final List<String> rawSourceCode;

    private boolean isRelocated;

    // Símbolos importados. Se você quiser armazenar quantas refs, ou offsets, etc., ajuste o tipo do value.
    private final Map<String, Integer> importedSymbols;

    // Lista de registros de reloc, indicando quais bytes do code precisam ser ajustados
    private final List<RelocationRecord> relocationRecords;

    public ObjectFile(int startAddress,
                      byte[] machineCode,
                      SymbolTable symbolTable,
                      String fileName,
                      List<String> rawSourceCode,
                      Map<String,Integer> importedSymbols,
                      List<RelocationRecord> relocationRecords) {
        if (machineCode == null || symbolTable == null || fileName == null) {
            throw new IllegalArgumentException("Nenhum parâmetro pode ser nulo.");
        }
        this.startAddress = startAddress;
        this.machineCode = machineCode;
        this.symbolTable = symbolTable;
        this.fileName = fileName;
        this.rawSourceCode = rawSourceCode;
        this.isRelocated = false;
        this.importedSymbols = importedSymbols;
        this.relocationRecords = relocationRecords;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public byte[] getObjectCode() {
        return machineCode;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public String getProgramName() {
        return fileName;
    }

    public List<String> getRawSourceCode() {
        return rawSourceCode;
    }

    public boolean isRelocated() {
        return isRelocated;
    }
    public void setRelocated(boolean relocated) {
        isRelocated = relocated;
    }

    public int getProgramLength() {
        return machineCode.length;
    }

    public Map<String,Integer> getImportedSymbols() {
        return importedSymbols;
    }

    public List<RelocationRecord> getRelocationRecords() {
        return relocationRecords;
    }

    /**
     * Lê um objeto ObjectFile serializado a partir de um arquivo .obj.
     * @param file O arquivo .obj a ser carregado
     * @return instância de ObjectFile
     * @throws IOException se houver erro de E/S ou se a classe não for encontrada
     */
    public static ObjectFile loadFromFile(File file) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ObjectFile) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Formato do arquivo inválido ou classe não encontrada ao ler ObjectFile.", e);
        }
    }

    @Override
    public String toString() {
        return "Nome: " + fileName
                + "\nEndereço de início = " + String.format("%04X", startAddress)
                + "\nTamanho = " + machineCode.length + " bytes";
    }
}
