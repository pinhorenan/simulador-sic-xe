package sicxesimulator.machine.cpu;

public class Instruction {
    private final int opcode;
    private final int[] operands;
    private final int format; // 1, 2, 3, 4
    private final boolean indexed;
    private final boolean extended;
    private final int effectiveAddress;

    public Instruction(int opcode, int[] operands, int format, boolean indexed, boolean extended, int effectiveAddress) {
        this.opcode = opcode;
        this.operands = operands;
        this.format = format;
        this.indexed = indexed;
        this.extended = extended;
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
                return 1; // Formato 1: 1 palavra
            case 2:
                return 2; // Formato 2: 2 palavras
            case 3:
                return 3; // Formato 3: 3 palavras
            case 4:
                return 4; // Formato 4: 4 palavras
            default:
                throw new IllegalArgumentException("Formato de instrução inválido: " + format);
        }
    }

    // Tamanho da instrução em bytes
    public int getSizeInBytes() {
        return getSizeInWords() * 3; // Cada palavra tem 3 bytes
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

    public boolean isExtended() {
        return extended;
    }

    public int getEffectiveAddress() {
        return effectiveAddress;
    }

    // Método para verificar se a instrução é de formato 4 (exemplo: RSUB)
    public boolean isFormat4() {
        return format == 4;
    }
}
