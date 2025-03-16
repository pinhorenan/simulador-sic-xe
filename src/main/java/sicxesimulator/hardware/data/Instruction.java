package sicxesimulator.hardware.data;

/**
 * Representa uma instrução do código objeto.
 * @param opcode Código da operação
 * @param operands Operandos da operação
 * @param format Formato da instrução
 * @param indexed Se a instrução é indexada
 * @param effectiveAddress Endereço efetivo da instrução
 */
public record Instruction(int opcode, int[] operands, int format, boolean indexed, int effectiveAddress) {

    /**
     * Retorna o tamanho da instrução em bytes.
     * @return Tamanho da instrução
     */
    public int getSize() {
        return switch (format) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            default -> throw new IllegalArgumentException("Formato de instrucao invalido: " + format);
        };
    }
}
