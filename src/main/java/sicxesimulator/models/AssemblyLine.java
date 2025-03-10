package sicxesimulator.models;

public class AssemblyLine {
    private final String label;
    private final String mnemonic;
    private final String operand;
    private final int address;

    public AssemblyLine(String label, String mnemonic, String operand, int address) {
        this.label = label;
        this.mnemonic = mnemonic;
        this.operand = operand;
        this.address = address;
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

    @Override
    public String toString() {
        return String.format("%04X: %-10s %-8s %s", address, (label != null ? label : ""), mnemonic, (operand != null ? operand : ""));
    }
}
