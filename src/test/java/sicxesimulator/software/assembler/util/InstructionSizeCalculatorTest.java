package sicxesimulator.software.assembler.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstructionSizeCalculatorTest {

    @Test
    void testCalculateSizeWord() {
        // WORD sempre 3 bytes
        assertEquals(3, InstructionSizeCalculator.calculateSize("WORD", "123"));
    }

    @Test
    void testCalculateSizeResw() {
        // RESW <n> => n * 3 bytes
        assertEquals(6, InstructionSizeCalculator.calculateSize("RESW", "2"));
        assertEquals(9, InstructionSizeCalculator.calculateSize("RESW", "3"));
        assertThrows(IllegalArgumentException.class, () ->
                InstructionSizeCalculator.calculateSize("RESW", null));
    }

    @Test
    void testCalculateSizeResb() {
        // RESB <n> => n bytes
        assertEquals(5, InstructionSizeCalculator.calculateSize("RESB", "5"));
        assertThrows(IllegalArgumentException.class, () ->
                InstructionSizeCalculator.calculateSize("RESB", null));
    }

    @Test
    void testCalculateSizeByteChar() {
        // BYTE C'ABC' => tamanho 3
        assertEquals(3, InstructionSizeCalculator.calculateSize("BYTE", "C'ABC'"));
    }

    @Test
    void testCalculateSizeByteHex() {
        // BYTE X'FF0A' => 2 bytes (4 hex = 2 bytes)
        assertEquals(2, InstructionSizeCalculator.calculateSize("BYTE", "X'FF0A'"));
    }

    @Test
    void testCalculateSizeInstructionFormat() {
        // CLEAR => formato 2
        assertEquals(2, InstructionSizeCalculator.calculateSize("CLEAR", null));
        // LDA => formato 3
        assertEquals(3, InstructionSizeCalculator.calculateSize("LDA", "ALPHA"));
        // +JSUB => formato 4
        assertEquals(4, InstructionSizeCalculator.calculateSize("+JSUB", "SUBR"));
    }
}
