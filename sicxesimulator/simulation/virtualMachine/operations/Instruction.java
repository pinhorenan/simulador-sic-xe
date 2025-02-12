package sicxesimulator.simulation.virtualMachine.operations;

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
    private String address;
    private int lineNumber;


    public Instruction(String label, String name, String[] args, String address, int lineNumber) {
        this.label = label;
        this.name = name;
        this.args = args;
        this.address = address;
        this.lineNumber = lineNumber;
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

    public String getAddress() {
        return address;
    }

    public int getLineNumber() {
        return lineNumber;
    }

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

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return String.format("(%d) %s %s %s", lineNumber, name, address, String.join(", ", args));
    }
} 


