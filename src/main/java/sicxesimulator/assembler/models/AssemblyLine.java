package sicxesimulator.assembler.models;

public class AssemblyLine {
    private String label;
    private String mnemonic;
    private String operand;
    private int address;
    private int lineNumber;

    public AssemblyLine(String label, String mnemonic, String operand, int address, int lineNumber) {
        this.label = label;
        this.mnemonic = mnemonic;
        this.operand = operand;
        this.address = address;
        this.lineNumber = lineNumber;
    }

    public String getLabel() {
        return label;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getOperand() {
        return operand;
    }

    public int getAddress() {
        return address;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return String.format("%04X: %-10s %-8s %s", address, (label != null ? label : ""), mnemonic, (operand != null ? operand : ""));
    }
}
