package sicxesimulator.software.assembler.data;

/**
 * Representa uma linha do código-fonte montado.
 * @param label Nome do rótulo
 * @param mnemonic Mnemônico da instrução
 * @param operand Operando da instrução
 * @param address Endereço da instrução
 */
public record AssemblyLine(String label, String mnemonic, String operand, int address) {
    @Override
    public String toString() {
        return String.format("%04X: %-10s %-8s %s", address, (label != null ? label : ""), mnemonic, (operand != null ? operand : ""));
    }
}
