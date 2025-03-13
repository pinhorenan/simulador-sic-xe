package sicxesimulator.models;

/**
 * @param format 1, 2, 3 ou 4
 */
public record Instruction(int opcode, int[] operands, int format, boolean indexed, int effectiveAddress) {

    public int getOpcode() {
        return opcode;
    }

    public int[] getOperands() {
        return operands;
    }

    public int getFormat() {
        return format;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public int getEffectiveAddress() {
        return effectiveAddress;
    }

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
