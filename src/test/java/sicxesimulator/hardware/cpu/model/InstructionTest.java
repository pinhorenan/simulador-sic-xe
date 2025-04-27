package sicxesimulator.hardware.cpu.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstructionTest {

    @Test
    void getSize_returnsCorrectValues() {
        assertEquals(1, new Instruction(0xC4, new int[0], 1, false, 0).getSize());
        assertEquals(2, new Instruction(0x90, new int[0], 2, false, 0).getSize());
        assertEquals(3, new Instruction(0x00, new int[0], 3, false, 0).getSize());
        assertEquals(4, new Instruction(0x48, new int[0], 4, false, 0).getSize());
    }

    @Test
    void getSize_invalidFormatThrows() {
        Instruction bogus = new Instruction(0, new int[0], 7, false, 0);
        assertThrows(IllegalArgumentException.class, bogus::getSize);
    }
}
