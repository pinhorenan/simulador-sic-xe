package sicxesimulator.machine.cpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import sicxesimulator.machine.Memory;
import sicxesimulator.machine.records.Instruction;

public class InstructionDecoderTest {

    @Test
    public void testFormat1() {
        // Cria memória e RegisterSet
        Memory memory = new Memory(256);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(0);

        // Escreve um opcode de formato 1 (ex: 0xC4)
        memory.writeByte(0, 0xC4);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction inst = decoder.decodeInstruction();

        // Verifica se o formato é 1
        assertEquals(1, inst.format(), "Formato deve ser 1");
        // Verifica se o opcode está correto
        assertEquals(0xC4, inst.opcode(), "Opcode deve ser 0xC4");
        // Instruções de formato 1 não possuem operandos nem EA
        assertEquals(0, inst.operands().length, "Operands devem estar vazios");
        assertEquals(0, inst.effectiveAddress(), "EA deve ser 0 para formato 1");
    }

    @Test
    public void testFormat2() {
        Memory memory = new Memory(256);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(0);

        // Escreve uma instrução de formato 2:
        // Primeiro byte: 0x04 (opcode), segundo byte: 0xAB
        memory.writeByte(0, 0x04);
        memory.writeByte(1, (byte)0xAB);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction inst = decoder.decodeInstruction();

        // Verifica se o formato é 2
        assertEquals(2, inst.format(), "Formato deve ser 2");
        // Para formato 2, opcode é o primeiro byte
        assertEquals(0x04, inst.opcode(), "Opcode deve ser 0x04");

        int[] operands = inst.operands();
        // Segundo byte 0xAB => r1 = 0xA, r2 = 0xB
        assertEquals(0xA, operands[0], "Registrador 1 deve ser 0xA");
        assertEquals(0xB, operands[1], "Registrador 2 deve ser 0xB");
    }

    @Test
    public void testFormat3() {
        Memory memory = new Memory(256);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(0);

        // Escreve uma instrução de formato 3:
        // Primeiro byte: 0x1F => binário 0001 1111 (n=1, i=1)
        memory.writeByte(0, 0x1F);
        // Segundo byte: 0x20 => binário 0010 0000 (x=0, b=0, p=1, e=0, high4=0)
        memory.writeByte(1, 0x20);
        // Terceiro byte: 0x10 (disp baixo)
        memory.writeByte(2, 0x10);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction inst = decoder.decodeInstruction();

        // Verifica se o formato é 3
        assertEquals(3, inst.format(), "Formato deve ser 3");
        // Opcode = firstByte & 0xFC => 0x1F & 0xFC = 0x1C
        assertEquals(0x1C, inst.opcode(), "Opcode deve ser 0x1C");

        // EA: p=1, disp = 0x10 (16), tamanho da instrução = 3 bytes -> EA = 0 + 3 + 16 = 19
        assertEquals(19, inst.effectiveAddress(), "EA deve ser 19");

        // Verifica os operands: devem ser [16, 0, 0, 1, 0, 1, 1]
        int[] ops = inst.operands();
        assertEquals(16, ops[0], "Disp/addr deve ser 16");
        assertEquals(0, ops[1], "x deve ser 0");
        assertEquals(0, ops[2], "b deve ser 0");
        assertEquals(1, ops[3], "p deve ser 1");
        assertEquals(0, ops[4], "e deve ser 0");
        assertEquals(1, ops[5], "n deve ser 1");
        assertEquals(1, ops[6], "i deve ser 1");
        assertFalse(inst.indexed(), "Indexed deve ser false");
    }

    @Test
    public void testFormat4() {
        Memory memory = new Memory(256);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(0);

        // Escreve uma instrução de formato 4:
        // Primeiro byte: 0x1F (n=1, i=1)
        memory.writeByte(0, 0x1F);
        // Segundo byte: 0x31 => binário 0011 0001 (x=0, b=0, p=1, e=1, high4 = 0x1)
        memory.writeByte(1, 0x31);
        // Terceiro byte: 0x02, quarto byte: 0x03
        memory.writeByte(2, 0x02);
        memory.writeByte(3, 0x03);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction inst = decoder.decodeInstruction();

        // Verifica se o formato é 4
        assertEquals(4, inst.format(), "Formato deve ser 4");
        // Opcode = firstByte & 0xFC => 0x1F & 0xFC = 0x1C
        assertEquals(0x1C, inst.opcode(), "Opcode deve ser 0x1C");

        // addr20 = (high4 << 16) | (thirdByte << 8) | fourthByte
        // high4 = 0x31 & 0x0F = 0x1; addr20 = (1<<16) | (0x02 << 8) | 0x03 = 0x10203 = 66051
        // EA = addr20 + PC + 4 = 66051 + 0 + 4 = 66055
        assertEquals(66055, inst.effectiveAddress(), "EA deve ser 66055");

        int[] ops = inst.operands();
        assertEquals(66051, ops[0], "Disp/addr deve ser 66051");
        assertEquals(0, ops[1], "x deve ser 0");
        assertEquals(0, ops[2], "b deve ser 0");
        assertEquals(1, ops[3], "p deve ser 1");
        assertEquals(1, ops[4], "e deve ser 1");
        assertEquals(1, ops[5], "n deve ser 1");
        assertEquals(1, ops[6], "i deve ser 1");
        assertFalse(inst.indexed(), "Indexed deve ser false");
    }
}
