package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;

public class InstructionDecoder {
    private final Memory memory;
    private final RegisterSet registers;
    private int pcValue;
    private int indexRegisterIntValue;
    private byte[] fetchedBytes;

    public InstructionDecoder(RegisterSet registers, Memory memory) {
        this.memory = memory;
        this.registers = registers;
        pcValue = registers.getRegister("PC").getIntValue();
        indexRegisterIntValue = registers.getRegister("X").getIntValue();
    }

    /**
     * Decodifica a instrução com base no valor atual de PC.
     * Retorna uma instância da classe Instruction com todos os detalhes decodificados.
     */
    public Instruction decodeInstruction() {
        if (fetchedBytes == null) {
            throw new IllegalStateException("Nenhuma instrução foi buscada para decodificação.");
        }

        // 1) Lê o opcode
        int currentOpcode = fetchedBytes[0] & 0xFF;

        // 2) Atualiza PC e X
        pcValue = registers.getRegister("PC").getIntValue();
        indexRegisterIntValue = registers.getRegister("X").getIntValue();

        // 3) Determina formato
        int format = determineInstructionFormat(currentOpcode);

        int[] operands;
        boolean indexed = false;
        int effectiveAddress = 0;

        switch (format) {
            case 1:
                // Formato 1 não tem operandos
                operands = new int[0];
                break;
            case 2:
                // Formato 2 = 2 bytes = 1 word, mas segundo byte contém registradores
                operands = decodeFormat2();
                break;
            case 3:
            default:
                operands = decodeFormat3();
                indexed = (operands[1] == 1); // O segundo valor retornado indica se é indexado
                int addr12 = operands[0];

                effectiveAddress = calculateEffectiveAddress(addr12, indexed);
                break;
        }

        // Criar e retornar a instância de Instruction
        return new Instruction(currentOpcode, operands, format, indexed,false, effectiveAddress);
    }

    public void setFetchedBytes(byte[] bytes) {
        this.fetchedBytes = bytes;
    }

    // Decide formato 1, 2 ou 3 (formato 4 não implementado)
    private int determineInstructionFormat(int opcode) {
        // RSUB
        if (opcode == 0x4C) return 3;
        // ADDR, CLEAR, etc.
        if (opcode == 0x90 || opcode == 0x04) return 2;
        // Caso contrário, default = 3
        return 3;
    }

    private int[] decodeFormat2() {
        // Lê o segundo byte (offset=1) para extrair r1 e r2
        int byte2 = memory.readByte(pcValue, 1) & 0xFF;
        int r1 = (byte2 >> 4) & 0xF;
        int r2 = byte2 & 0xF;
        return new int[]{ r1, r2 };
    }

    private int[] decodeFormat3() {
        // Lê byte1 e byte2 para formar 12 bits de endereço
        int byte1 = memory.readByte(pcValue, 1) & 0xFF;
        int byte2 = memory.readByte(pcValue, 2) & 0xFF;

        // Extração do bit de index (bit 7)
        boolean indexed = (byte1 & 0x80) != 0;
        int addr12 = ((byte1 & 0x7F) << 8) | byte2;

        // Retorna array contendo [addr2, (indexed ? 1 : 0)]
        return new int[]{ addr12, indexed ? 1 : 0 };
    }

    private int calculateEffectiveAddress(int addr12, boolean indexed) {
        int ea = addr12;
        if (indexed) {
            ea += indexRegisterIntValue;
        }
        return ea;
    }
}
