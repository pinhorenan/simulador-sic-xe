package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;

public class InstructionDecoder {
    private final Memory memory;
    private final RegisterSet registers;
    private int programCounter;

    public InstructionDecoder(RegisterSet registers, Memory memory) {
        this.memory = memory;
        this.registers = registers;
    }

    /**
     * Decodifica a instrução a partir do valor atual de PC, lendo os bytes diretamente da memória.
     */
    public Instruction decodeInstruction() {
        // Atualiza o PC a partir dos registradores.
        programCounter = registers.getRegister("PC").getIntValue();
        // Lê o primeiro byte da instrução da memória (offset 0)
        int fullByte = memory.readByte(programCounter, 0) & 0xFF;

        // Determina o formato da instrução
        int format = determineInstructionFormat(fullByte);

        int opcode;
        int[] operands;
        boolean indexed = false;
        int effectiveAddress = 0;

        if (format == 2) {
            // Para formato 2, o opcode é lido integralmente (não há bits n e i)
            opcode = fullByte;
            operands = decodeFormat2();
        } else {
            // Para formato 3 (ou 4, se implementado), extrai os 6 bits de opcode.
            opcode = fullByte & 0xFC;
            operands = decodeFormat3(); // Retorna array com [disp12, x, b, p, e]
            indexed = (operands[1] == 1); // flag x
            effectiveAddress = calculateEffectiveAddress(operands[0], operands[1], operands[2], operands[3], operands[4]);
        }

        return new Instruction(opcode, operands, format, indexed, false, effectiveAddress);
    }

    /**
     * Determina o formato da instrução.
     * Para instruções em formato 2, utiliza o byte completo; para formato 3, extrai os 6 bits de opcode.
     */
    private int determineInstructionFormat(int fullByte) {
        // Exemplo: se o primeiro byte for 0x04 ou 0x90, consideramos formato 2.
        if (fullByte == 0x04 || fullByte == 0x90) {
            return 2;
        }
        // Para RSUB, o objeto no formato 3 vem como 0x4C, mas após extração teremos:
        return 3;
    }

    /**
     * Decodifica instruções em formato 2 (2 bytes).
     * O segundo byte contém dois registradores (4 bits cada).
     */
    private int[] decodeFormat2() {
        // Lê o segundo byte da instrução (offset 1 a partir do PC)
        int secondByte = memory.readByte(programCounter, 1) & 0xFF;
        int r1 = (secondByte >> 4) & 0xF;
        int r2 = secondByte & 0xF;
        return new int[]{ r1, r2 };
    }

    /**
     * Decodifica instruções em formato 3 (3 bytes).
     * Estrutura:
     *   - Byte 1: bits 7..2 = opcode; bits 1..0 = n e i (não usados diretamente)
     *   - Byte 2: bit 7 = x; bit 6 = b; bit 5 = p; bit 4 = e; bits 3..0 = 4 bits altos do deslocamento
     *   - Byte 3: 8 bits do deslocamento (parte baixa)
     * Retorna um array com: [deslocamento (12 bits), x, b, p, e]
     */
    private int[] decodeFormat3() {
        // Lê os bytes 2 e 3 da instrução a partir do PC
        int secondByte = memory.readByte(programCounter, 1) & 0xFF;
        int thirdByte = memory.readByte(programCounter, 2) & 0xFF;

        int x = (secondByte & 0x80) >> 7;
        int b = (secondByte & 0x40) >> 6;
        int p = (secondByte & 0x20) >> 5;
        int e = (secondByte & 0x10) >> 4;
        int dispHigh = secondByte & 0x0F;
        int disp12 = (dispHigh << 8) | thirdByte;

        return new int[]{ disp12, x, b, p, e };
    }

    /**
     * Calcula o endereço efetivo (EA) a partir do deslocamento e dos bits de modo.
     * - Se p == 1 (PC-relativo): EA = (PC_original + 3) + disp12 (convertido para valor com sinal)
     * - Se b == 1 (base-relativo): EA = (valor do registrador B) + disp12
     * - Caso contrário: EA = disp12 (endereço absoluto)
     * Se o flag indexado (x) estiver ativo, soma o valor do registrador X.
     */
    private int calculateEffectiveAddress(int disp12, int x, int b, int p, int ignoredE) {
        int EA = disp12;
        // Converte o deslocamento de 12 bits para um valor com sinal
        if ((disp12 & 0x800) != 0) { // se o bit 11 está setado
            EA = disp12 - 0x1000;
        }
        if (p == 1) {
            EA = (programCounter + 3) + EA;
        } else if (b == 1) {
            EA = registers.getRegister("B").getIntValue() + EA;
        }
        if (x == 1) {
            EA += registers.getRegister("X").getIntValue();
        }
        return EA;
    }
}
