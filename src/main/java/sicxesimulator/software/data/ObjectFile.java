package sicxesimulator.software.data;

import sicxesimulator.common.utils.Constants;
import sicxesimulator.common.utils.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Representa um módulo objeto serializável (resultante do assembler ou linker).
 * Contém bytes de código, tabela de símbolos, registros de relocação e metadados.
 */
public class ObjectFile implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /* campos imutáveis ---------------------------------------------------- */
    private final int           startAddress;
    private final byte[]        machineCode;          // cópia defensiva
    private final SymbolTable   symbolTable;
    private final String        programName;
    private final List<String>  rawSourceCode;
    private final Set<String>   importedSymbols;
    private final List<RelocationRecord> relocationRecords;

    /* mutáveis ------------------------------------------------------------ */
    private boolean            fullyRelocated = false;
    private ObjectFileOrigin   origin         = ObjectFileOrigin.SINGLE_MODULE;

    /* -------------------------------------------------------------------- */

    public ObjectFile(int startAddress,
                      byte[] machineCode,
                      SymbolTable symbolTable,
                      String programName,
                      List<String> rawSourceCode,
                      Set<String> importedSymbols,
                      List<RelocationRecord> relocationRecords) {

        this.startAddress      = startAddress;
        this.machineCode       = machineCode.clone();          // defensive
        this.symbolTable       = Objects.requireNonNull(symbolTable, "symbolTable");
        this.programName       = Objects.requireNonNull(programName, "programName");
        this.rawSourceCode     = rawSourceCode;
        this.importedSymbols   = importedSymbols;
        this.relocationRecords = relocationRecords;
    }

    /* getters ------------------------------------------------------------- */
    public int                 getStartAddress()     { return startAddress; }
    public int                 getProgramLength()    { return machineCode.length; }
    public byte[]              getObjectCode()       { return machineCode.clone(); }   // defensive copy
    public String              getProgramName()      { return programName; }
    public SymbolTable         getSymbolTable()      { return symbolTable; }
    public boolean             isFullyRelocated()    { return fullyRelocated; }
    public Set<String>         getImportedSymbols()  { return importedSymbols; }
    public List<String>        getRawSourceCode()    { return rawSourceCode; }
    public List<RelocationRecord> getRelocationRecords() { return relocationRecords; }
    public ObjectFileOrigin    getOrigin()           { return origin; }

    /* setters mutáveis ---------------------------------------------------- */
    public void setFullyRelocated(boolean value)     { fullyRelocated = value; }
    public void setOrigin(ObjectFileOrigin origin)   { this.origin = origin; }

    /* util ---------------------------------------------------------------- */
    public String getObjectCodeAsString() {
        File obj = new File(Constants.SAVE_DIR, programName + ".obj");
        if (!obj.exists()) return "Arquivo .obj não encontrado.";
        try { return Files.readString(obj.toPath()); }
        catch (IOException e) { return "Erro ao ler .obj: " + e.getMessage(); }
    }

    /* serialização -------------------------------------------------------- */
    public void saveToFile(File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        } catch (IOException e) {
            Logger.error("Erro ao salvar ObjectFile em " + file.getAbsolutePath(), e);
        }
    }
    public static ObjectFile loadFromFile(File file) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ObjectFile) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Falha ao desserializar ObjectFile", e);
        }
    }

    @Override public String toString() {
        return "ObjectFile[" + programName +
                ", start=0x" + Integer.toHexString(startAddress).toUpperCase() +
                ", size=" + machineCode.length + "]";
    }

    /* origem -------------------------------------------------------------- */
    public enum ObjectFileOrigin { SINGLE_MODULE, LINKED_MODULES }
}
