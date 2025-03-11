package sicxesimulator.models;

public record AssemblyLine(String label, String mnemonic, String operand, int address) {
    @Override
    public String toString() {
        return String.format("%04X: %-10s %-8s %s", address, (label != null ? label : ""), mnemonic, (operand != null ? operand : ""));
    }
}

