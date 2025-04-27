package sicxesimulator.software.data;

/**
 * Representa uma linha do código-fonte assembly após a primeira passagem.
 * <p>
 * Cada linha contém, opcionalmente, um rótulo, um mnemônico, um operando
 * e o endereço de memória correspondente no programa.
 *
 * @param label Rótulo associado à instrução (pode ser null).
 * @param mnemonic Mnemônico da instrução (ex: LDA, STA, ADD).
 * @param operand Operando da instrução (pode ser um literal, símbolo ou null).
 * @param address Endereço da instrução na memória.
 */

public record AssemblyLine(String label, String mnemonic, String operand, int address) {
    @Override
    public String toString() {
        return String.format("%04X: %-10s %-8s %s", address, (label != null ? label : ""), mnemonic, (operand != null ? operand : ""));
    }
}
