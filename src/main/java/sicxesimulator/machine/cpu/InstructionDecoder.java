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

        // Lê o primeiro byte completo
        int fullByte = fetchedBytes[0] & 0xFF;

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
            // Para formato 3 (ou 4, se implementado), extrai os 6 bits de opcode
            opcode = (fullByte & 0xFC) >> 2;
            // Decodifica os demais bytes no formato 3, extraindo os flags x, b, p e e, e o deslocamento de 12 bits.
            operands = decodeFormat3(); // Retorna array com [disp12, x, b, p, e]
            indexed = (operands[1] == 1); // flag x

            effectiveAddress = calculateEffectiveAddress(operands[0], operands[1], operands[2], operands[3], operands[4]);
        }

        return new Instruction(opcode, operands, format, indexed, false, effectiveAddress);
    }

    public void setFetchedBytes(byte[] bytes) {
        this.fetchedBytes = bytes;
    }

    /**
     * Determina o formato da instrução.
     * Para instruções em formato 2, utiliza o byte completo; para formato 3, extrai os 6 bits de opcode.
     */
    private int determineInstructionFormat(int fullByte) {
        // Verifica se é formato 2: opcodes conhecidos para formato 2 (ex: CLEAR = 0x04, ADDR = 0x90)
        if (fullByte == 0x04 || fullByte == 0x90) {
            return 2;
        }
        // Para RSUB, o objeto no formato 3 vem como 0x4C, mas após extração teremos:
        // (0x4C & 0xFC) >> 2 = 0x13
        int opcodeExtracted = (fullByte & 0xFC) >> 2;
        //noinspection IfStatementWithIdenticalBranches
        if (opcodeExtracted == 0x13) { // RSUB. If decorativo apenas para especificar aqui no código.
            return 3;
        }
        return 3;
    }

    /**
     * Decodifica instruções em formato 2 (2 bytes).
     * O segundo byte contém dois registradores (4 bits cada).
     */
    private int[] decodeFormat2() {
        int byte2 = memory.readByte(pcValue, 1) & 0xFF;
        int r1 = (byte2 >> 4) & 0xF;
        int r2 = byte2 & 0xF;
        return new int[]{ r1, r2 };
    }

    /**
     * Decodifica instruções em formato 3 (3 bytes).
     * A estrutura do formato 3 é:
     *   - Byte 1: bits 7..2 = opcode; bits 1..0 = n e i (não usados diretamente)
     *   - Byte 2: bits: x (bit 7), b (bit 6), p (bit 5), e (bit 4), e bits 3..0 = 4 bits altos do deslocamento
     *   - Byte 3: 8 bits do deslocamento (parte baixa)
     * Retorna um array com:
     *   [0] = deslocamento (12 bits)
     *   [1] = flag indexado (x): 1 se ativo, 0 caso contrário
     *   [2] = bit b (1 se ativo, 0 caso contrário)
     *   [3] = bit p (1 se ativo, 0 caso contrário)
     *   [4] = bit e (1 se ativo, 0 caso contrário)
     */
    private int[] decodeFormat3() {
        int pc = registers.getRegister("PC").getIntValue();
        int wordIndex = pc / 3;
        int offset = pc % 3;

        int secondByte = memory.readByte(wordIndex, offset + 1) & 0xFF;
        int thirdByte = memory.readByte(wordIndex, offset + 2) & 0xFF;

        int x = (secondByte & 0x80) >> 7;  // flag indexado
        int b = (secondByte & 0x40) >> 6;  // base-relativo
        int p = (secondByte & 0x20) >> 5;  // PC-relativo
        int e = (secondByte & 0x10) >> 4;  // formato extendido (por enquanto, ignorado)
        int dispHigh = secondByte & 0x0F;
        int disp12 = (dispHigh << 8) | thirdByte;

        return new int[]{ disp12, x, b, p, e };
    }

    /**
     * Calcula o endereço efetivo a partir do deslocamento e dos bits de modo.
     * - Se p == 1 (PC-relativo): EA = (PC_original + 3) + disp12 (convertido para valor com sinal)
     * - Se b == 1 (base-relativo): EA = (valor do registrador B) + disp12
     * - Caso contrário: EA = disp12 (endereço absoluto)
     * Se o flag indexado (x) estiver ativo, soma o valor do registrador X.
     * O parâmetro e é atualmente ignorado, pois estamos tratando apenas instruções não extendidas.
     */
    private int calculateEffectiveAddress(int disp12, int x, int b, int p, int ignoredE) {
        int EA = disp12;
        // Converte o deslocamento de 12 bits para um valor com sinal
        if ((disp12 & 0x800) != 0) { // se o bit 11 está setado
            EA = disp12 - 0x1000;
        }
        if (p == 1) {
            EA = (pcValue + 3) + EA; // PC-relativo: utiliza o PC original (pcValue)
        } else if (b == 1) {
            EA = registers.getRegister("B").getIntValue() + EA;
        }
        // EA permanece como endereço absoluto se p e b estiverem zerados

        if (x == 1) {
            EA += indexRegisterIntValue;
        }
        return EA;
    }
}
