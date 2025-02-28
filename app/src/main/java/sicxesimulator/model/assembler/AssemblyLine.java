package sicxesimulator.model.assembler;

public class AssemblyLine {
    protected final String label;
    protected final String operation;
    protected final String operand;
    protected final String comment;
    private int address; // Novo campo para armazenar o endereço

    public AssemblyLine(String label, String operation, String operand, String comment) {
        this.label = label;
        this.operation = operation;
        this.operand = operand;
        this.comment = comment;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    // Getters existentes...
    public String getLabel() { return label; }
    public String getOperation() { return operation; }
    public String getOperand() { return operand; }
    public String getComment() { return comment; }

    @Override
    public String toString() {
        return String.format("Address: %06X, Label: %s, Operation: %s, Operand: %s, Comment: %s",
                address, label, operation, operand, comment);
    }
}
