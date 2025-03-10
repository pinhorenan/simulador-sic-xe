package sicxesimulator.models;

/**
 * @param format 1, 2, 3 ou 4
 */
public record Instruction(int opcode, int[] operands, int format, boolean indexed, int effectiveAddress) {

    // Tamanho da instrução em palavras
    public int getSizeInWords() {
        return switch (format) {
            case 1 -> 1; // Formato 1: 1 byte, 1 palavra
            case 2 -> 1; // Formato 2: 2 bytes, 1 palavra
            case 3 -> 1; // Formato 3: 3 bytes, 1 palavras
            case 4 -> 2; // Formato 4: 4 bytes, 2 palavras
            default -> throw new IllegalArgumentException("Formato de instrução inválido: " + format);
        };
    }

    public int getSizeInBytes() {
        return getSizeInWords() * 3;
    }
}
