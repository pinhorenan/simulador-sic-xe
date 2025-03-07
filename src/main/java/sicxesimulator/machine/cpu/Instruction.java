package sicxesimulator.machine.cpu;

public class Instruction {
    private final int opcode;
    private final int[] operands;
    private final int format; // 1, 2, 3 ou 4
    private final boolean indexed;
    private final int effectiveAddress;

    public Instruction(int opcode, int[] operands, int format, boolean indexed, boolean extended, int effectiveAddress) {
        this.opcode = opcode;
        this.operands = operands;
        this.format = format;
        this.indexed = indexed;
        this.effectiveAddress = effectiveAddress;
    }

    // Métodos getters para acessar os campos
    public int getOpcode() {
        return opcode;
    }

    // Tamanho da instrução em palavras
    public int getSizeInWords() {
        switch (format) {
            case 1:
                return 1; // Formato 1: 1 byte, 1 palavra
            case 2:
                return 1; // Formato 2: 2 bytes, 1 palavra
            case 3:
                return 1; // Formato 3: 3 bytes, 1 palavras
            case 4:
                return 2; // Formato 4: 4 bytes, 2 palavras
            default:
                throw new IllegalArgumentException("Formato de instrução inválido: " + format);
        }
    }

    public int getSizeInBytes() {
        return getSizeInWords() * 3;
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
}
