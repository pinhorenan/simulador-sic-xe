package sicxesimulator.software.data;

import sicxesimulator.software.data.records.RelocationRecord;
import sicxesimulator.utils.Constants;
import sicxesimulator.utils.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

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
    private final String fileName;
    private final List<String> rawSourceCode;
    private final SymbolTable symbolTable;

    private boolean fullyRelocated;

    // Símbolos importados. Por definição eles não têm endereço, apenas nome, logo fica mais fácil armazenar como um conjunto de String.
    private final Set<String> importedSymbols;

    // Lista de registros de reloc, indicando quais bytes do machineCode precisam ser ajustados
    private final List<RelocationRecord> relocationRecords;

    private ObjectFileOrigin origin;

    public ObjectFile(int startAddress,
                      byte[] machineCode,
                      SymbolTable symbolTable,
                      String fileName,
                      List<String> rawSourceCode,
                      Set<String> importedSymbols,
                      List<RelocationRecord> relocationRecords) {
        if (machineCode == null || symbolTable == null || fileName == null) {
            throw new IllegalArgumentException("Nenhum parametro pode ser nulo.");
        }
        this.startAddress = startAddress;
        this.machineCode = machineCode;
        this.symbolTable = symbolTable;
        this.fileName = fileName;
        this.rawSourceCode = rawSourceCode;
        this.fullyRelocated = false;
        this.importedSymbols = importedSymbols;
        this.relocationRecords = relocationRecords;
    }

    /// ===== Métodos Getters ===== ///

    public int getStartAddress() {
        return startAddress;
    }

    public int getProgramLength() {
        return machineCode.length;
    }

    public byte[] getObjectCode() {
        return machineCode;
    }

    public String getObjectCodeAsString() {
        File objFile = new File(Constants.SAVE_DIR, this.getProgramName() + ".obj");
        if (!objFile.exists()) {
            return "Arquivo .obj nao encontrado.";
        }
        try {
            return Files.readString(objFile.toPath());
        } catch (IOException e) {
            return "Erro ao ler o arquivo .obj: " + e.getMessage();
        }
    }

    public boolean isFullyRelocated() {
        return fullyRelocated;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public String getProgramName() {
        return fileName;
    }

    public Set<String> getImportedSymbols() {
        return importedSymbols;
    }

    public List<String> getRawSourceCode() {
        return rawSourceCode;
    }

    public List<RelocationRecord> getRelocationRecords() {
        return relocationRecords;
    }

    public ObjectFileOrigin getOrigin() {
        return origin;
    }

    /// ===== Métodos Setter ===== ///

    public void setFullyRelocated(boolean relocated) {
        fullyRelocated = relocated;
    }

    public void setOrigin(ObjectFileOrigin origin) {
        this.origin = origin;
    }

    /// ===== Métodos de Serialização ===== ///

    /**
     * Lê um objeto ObjectFile serializado a partir de um arquivo .obj.
     *
     * @param file O arquivo .obj a ser carregado
     * @return instância de ObjectFile
     * @throws IOException se houver erro de E/S ou se a classe não for encontrada
     */
    public static ObjectFile loadFromFile(File file) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ObjectFile) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Formato do arquivo invalido ou classe nao encontrada ao ler ObjectFile.", e);
        }
    }

    /**
     * Salva o objeto ObjectFile serializado em um arquivo .obj.
     *
     * @param file O arquivo .obj a ser salvo
     */
    public void saveToFile(File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (IOException e) {
            Logger.logError("Erro ao salvar ObjectFile em: " + file.getAbsolutePath(), e);
        }
    }


    @Override
    public String toString() {
        return "Nome: " + fileName
                + "\nEndereco de inicio = " + String.format("%04X", startAddress)
                + "\nTamanho = " + machineCode.length + " bytes";
    }

    /**
     * Enumeração para indicar a origem de um arquivo-objeto.
     */
    public enum ObjectFileOrigin {
        SINGLE_MODULE,
        LINKED_MODULES
    }
}
