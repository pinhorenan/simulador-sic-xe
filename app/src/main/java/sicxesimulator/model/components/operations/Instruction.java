package sicxesimulator.model.components.operations;

/**
 * Representa uma instrução do arquivo de montagem.
 * Cada instrução possui um rótulo (label), nome (mnemonic), argumentos, endereço (a ser determinado pelo interpretador)
 * e o número da linha onde foi encontrada no arquivo.
 */
@SuppressWarnings("unused")
public class Instruction {
    private String label;
    private String name;
    private String[] args;
    private final int address;
    private final int size;

    public Instruction(String label, String name, String[] args, int address, int size) {
        this.label = label;
        this.name = name;
        this.args = args;
        this.address = address;
        this.size = size;
    }

    /// Getters

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String[] getArgs() {
        return args;
    }

    public int getAddress() { return address; }

    public int getSize() { return size; }

    /// Setters

    public void setLabel(String label) {
        this.label = label;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }


    @Override
    public String toString() {
        return String.format(" %s %s", name, String.join(", ", args));
    }
} 


