package sicxesimulator.hardware.cpu.decoder;

import org.junit.jupiter.api.Test;
import sicxesimulator.hardware.cpu.model.Instruction;
import sicxesimulator.hardware.cpu.register.RegisterSet;
import sicxesimulator.hardware.memory.Memory;

import static org.junit.jupiter.api.Assertions.*;

class InstructionDecoderTest {

    private static InstructionDecoder newDecoder(byte... bytes) {
        Memory mem = new Memory(64);
        for (int i = 0; i < bytes.length; i++) mem.writeByte(i, bytes[i]);
        RegisterSet regs = new RegisterSet();        // PC = 0
        return new InstructionDecoder(regs, mem);
    }

    @Test
    void decodesFormat1() {
        // 0xC4 = FIX (formato 1)
        Instruction ins = newDecoder((byte) 0xC4).decodeInstruction();
        assertEquals(1, ins.format());
        assertEquals(0xC4, ins.opcode());
        assertEquals(0, ins.operands().length);
    }

    @Test
    void decodesFormat2() {
        // 0x90 0x01 = ADDR A,X  (formato 2)
        Instruction ins = newDecoder((byte) 0x90, (byte) 0x01).decodeInstruction();
        assertEquals(2, ins.format());
        assertArrayEquals(new int[]{0, 1}, ins.operands());
    }
}
