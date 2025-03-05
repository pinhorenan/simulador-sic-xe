package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;

public class InstructionDecoder {
    private final Memory memory;
    private final RegisterSet registers;
    private int pcValue;
    private int indexRegisterIntValue;

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
        pcValue = registers.getRegister("PC").getIntValue();
        indexRegisterIntValue = registers.getRegister("X").getIntValue();
        int currentOpcode = fetchOpcode();
        int[] operands;
        int format = determineInstructionFormat();
        boolean indexed = false;
        boolean extended = false;
        int effectiveAddress = 0;

        // Decodificar os operandos e determinar os flags baseados no formato
        switch (format) {
            case 1:
                operands = new int[0];  // Formato 1 não tem operandos
                break;
            case 2:
                operands = decodeFormat2();
                break;
            case 3:
            case 4:
                operands = decodeFormat3or4();
                indexed = (currentOpcode & 0x10) != 0;  // Checa flag indexed
                extended = (currentOpcode & 0x01) != 0; // Checa flag extended
                effectiveAddress = calculateEffectiveAddress(operands, indexed, extended);
                break;
            default:
                throw new IllegalArgumentException("Formato de instrução inválido: " + format);
        }

        // Criar e retornar a instância de Instruction
        return new Instruction(currentOpcode, operands, format, indexed, extended, effectiveAddress);
    }

    private int fetchOpcode() {
        return memory.readByte(pcValue, 0) & 0xFF;  // Lê o opcode do primeiro byte
    }

    private int determineInstructionFormat() {
        if (fetchOpcode() == 0x4C) return 3;  // RSUB
        if (fetchOpcode() == 0x90 || fetchOpcode() == 0x04) return 2; // ADDR, CLEAR
        int flags = memory.readByte(pcValue, 1) & 0xFF;
        return (flags & 0x01) != 0 ? 4 : 3;
    }

    private int[] decodeFormat2() {
        int byte2 = memory.readByte(pcValue, 1) & 0xFF;
        return new int[]{ (byte2 >> 4) & 0xF, byte2 & 0xF };
    }

    private int[] decodeFormat3or4() {
        // Para formatos 3 e 4, operandos são compostos geralmente por endereços e/ou registradores.
        int[] operands = new int[2];  // Exemplo simplificado: dois operandos.

        int byte1 = memory.readByte(pcValue, 1) & 0xFF; // O primeiro byte do endereço efetivo
        int byte2 = memory.readByte(pcValue, 2) & 0xFF; // O segundo byte do endereço efetivo

        // Exemplo de cálculos para operandos
        operands[0] = ((byte1 << 8) | byte2); // Combina os dois bytes para formar o endereço.

        if (fetchOpcode() == 0x4C) {
            operands[1] = 0;  // Para o exemplo RSUB, não há operandos adicionais.
        }

        return operands;
    }

    private int calculateEffectiveAddress(int[] operands, boolean indexed, boolean extended) {
        // O cálculo do endereço efetivo depende de várias condições:
        // - Se é uma instrução indexada, a operação é diferente.
        // - Se é uma instrução estendida (formato 4), o endereço é mais longo.

        int effectiveAddress = operands[0];

        if (indexed) {
            // Se indexado, o endereço efetivo é acrescido do valor do registrador "X".
            // Exemplo: address = effectiveAddress + X
            effectiveAddress += indexRegisterIntValue;  // Supondo que existe um método para obter o valor de X.
        }

        if (extended) {
            // Se extended, o endereço pode ser mais longo.
            // O cálculo pode variar dependendo da especificação do modelo.
            effectiveAddress |= (operands[1] << 8);  // Exemplo de combinação para expandir o endereço.
        }

        return effectiveAddress;
    }
}
