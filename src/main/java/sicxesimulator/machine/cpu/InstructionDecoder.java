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
            // Para formato 3 (ou 4, se implementado), extraímos os 6 bits de opcode e os bits n e i.
            // Extração do opcode: bits 7..2
            opcode = (fullByte & 0xFC) >> 2;
            // Aqui poderíamos também extrair os bits n e i, se necessário para a lógica do simulador.
            // Neste exemplo, não os utilizamos diretamente na criação da Instruction.

            // Decodifica os demais bytes no formato 3
            // Atualize o método para ler corretamente os flags do segundo byte (x, b, p, e) e o deslocamento de 12 bits.
            operands = decodeFormat3();
            indexed = (operands[1] == 1); // O segundo valor indica se o flag X (indexado) está ativo
            int addr12 = operands[0];
            effectiveAddress = calculateEffectiveAddress(addr12, indexed);
        }

        // Cria e retorna a instância de Instruction com os valores decodificados
        return new Instruction(opcode, operands, format, indexed, false, effectiveAddress);
    }

    public void setFetchedBytes(byte[] bytes) {
        this.fetchedBytes = bytes;
    }

    /**
     * Determina o formato da instrução.
     * Para instruções em formato 2, utiliza o byte completo; para formato 3, extrai os 6 bits de opcode.
     * Aqui é feita uma verificação simples; você pode expandir esse método para outros opcodes.
     */
    private int determineInstructionFormat(int fullByte) {
        // Verifica se é formato 2: os opcodes conhecidos para formato 2 (ex: CLEAR = 0x04, ADDR = 0x90, etc.)
        if (fullByte == 0x04 || fullByte == 0x90) {
            return 2;
        }
        // Para RSUB, o objeto no formato 3 vem como 0x4C, mas após extração teremos:
        // (0x4C & 0xFC) >> 2 = (0x4C) >> 2 = 0x13
        int opcodeExtracted = (fullByte & 0xFC) >> 2;
        if (opcodeExtracted == 0x13) { // RSUB
            return 3;
        }
        // Caso padrão: formato 3
        return 3;
    }

    /**
     * Decodifica instruções em formato 2 (2 bytes).
     * O segundo byte contém dois registradores (4 bits cada).
     */
    private int[] decodeFormat2() {
        // Lê o segundo byte (offset=1) para extrair os registradores
        int byte2 = memory.readByte(pcValue, 1) & 0xFF;
        int r1 = (byte2 >> 4) & 0xF;
        int r2 = byte2 & 0xF;
        return new int[]{ r1, r2 };
    }

    /**
     * Decodifica instruções em formato 3 (3 bytes).
     * A estrutura do formato 3 é:
     *   - Byte 1: bits 7..2 = opcode; bits 1..0 = n e i (não usados aqui diretamente)
     *   - Byte 2: bit 7 = flag X (indexado); bits 6-4 = b, p, e; bits 3..0 = 4 bits altos do deslocamento
     *   - Byte 3: 8 bits do deslocamento (parte baixa)
     *
     * Retorna um array onde:
     *   [0] = deslocamento (12 bits) e
     *   [1] = 1 se o flag indexado (X) estiver ativo, 0 caso contrário.
     */
    private int[] decodeFormat3() {
        // Note que usamos o método readByte com offsets 1, 2 e 3 (cada palavra tem 3 bytes)
        // Primeiro byte já foi lido (usado para extrair o opcode)
        int secondByte = memory.readByte(pcValue, 2) & 0xFF;
        int thirdByte = memory.readByte(pcValue, 3) & 0xFF;

        // O flag indexado (X) está no bit 7 do segundo byte
        boolean indexed = (secondByte & 0x80) != 0;
        // Os 12 bits de endereço: os 4 bits menos significativos do segundo byte e os 8 bits do terceiro byte
        int disp12 = ((secondByte & 0x0F) << 8) | thirdByte;

        return new int[]{ disp12, indexed ? 1 : 0 };
    }

    /**
     * Calcula o endereço efetivo somando o valor do registrador de índice se a flag indexado estiver ativa.
     */
    private int calculateEffectiveAddress(int addr12, boolean indexed) {
        int ea = addr12;
        if (indexed) {
            ea += indexRegisterIntValue;
        }
        return ea;
    }
}
