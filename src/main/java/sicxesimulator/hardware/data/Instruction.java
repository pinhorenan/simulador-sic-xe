package sicxesimulator.hardware.data;

/**
 * Representa uma instrução decodificada da arquitetura SIC/XE.
 *
 * <p>Cada instância contém opcode, vetor de operandos, formato,
 * flag de indexação e endereço efetivo já calculado.</p>
 *
 * @param opcode           código da operação
 * @param operands         vetor de operandos (registradores, endereços, etc.)
 * @param format           formato da instrução (1, 2, 3 ou 4)
 * @param indexed          true se houver indexação
 * @param effectiveAddress endereço efetivo da instrução
 *
 * @author Renan
 * @since 1.0.0
 */
public record Instruction(
        int opcode,
        int[] operands,
        int format,
        boolean indexed,
        int effectiveAddress
) {
    /**
     * Retorna o tamanho da instrução em bytes, com base no formato.
     *
     * @return número de bytes (1, 2, 3 ou 4)
     * @throws IllegalArgumentException se o formato for inválido
     */
    public int getSize() {
        return switch (format) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            default -> throw new IllegalArgumentException("Formato de instrução inválido: " + format);
        };
    }
}
