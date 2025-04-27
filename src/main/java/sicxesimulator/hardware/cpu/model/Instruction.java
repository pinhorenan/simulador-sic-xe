package sicxesimulator.hardware.cpu.model;

/**
 * Representa uma instrução SIC/XE já decodificada.
 *
 * @param opcode           opcode limpo (bits 8–1 ou 6–1, conforme formato)
 * @param operands         vetor de campos extraídos (registradores, displacement, flags)
 * @param format           1, 2, 3 ou 4
 * @param indexed          indica uso de indexação via registrador X
 * @param effectiveAddress endereço efetivo; <i>imediato</i> quando aplicável
 *
 * @author Renan
 * @since 1.0.0
 */
public record Instruction(
        int  opcode,
        int[] operands,
        int  format,
        boolean indexed,
        int  effectiveAddress
) {

    /**
     * Calcula o tamanho em bytes a partir do formato.
     *
     * @return 1 ≤ n ≤ 4
     * @throws IllegalArgumentException se {@code format} estiver fora do intervalo
     */
    public int getSize() {
        return switch (format) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            default ->
                    throw new IllegalArgumentException("Formato inválido: " + format);
        };
    }
}
