package sicxesimulator.hardware.data;

/**
 * Representa uma instrução decodificada da arquitetura SIC/XE.
 *
 * Cada instrução possui:
 * - um opcode
 * - um vetor de operandos (registradores, endereços, etc.)
 * - um formato (1, 2, 3 ou 4)
 * - um flag de indexação
 * - o endereço efetivo calculado
 *
 * @param opcode Código da operação
 * @param operands Vetor de operandos
 * @param format Formato da instrução
 * @param indexed Flag de indexação
 * @param effectiveAddress Endereço efetivo da instrução
 */
public record Instruction(int opcode, int[] operands, int format, boolean indexed, int effectiveAddress) {

    /**
     * Retorna o tamanho da instrução em bytes, com base no formato.
     *
     * @return 1, 2, 3 ou 4 bytes, dependendo do formato.
     * @throws IllegalArgumentException Se o formato for inválido.
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
