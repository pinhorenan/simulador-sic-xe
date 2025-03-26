package sicxesimulatorTest.software.assembler.util;

import org.junit.jupiter.api.Test;
import sicxesimulator.software.assembler.util.Parser;
import sicxesimulator.data.SymbolTable;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testParseAddressHex() {
        assertEquals(0x100, Parser.parseAddress("0x100"));
        assertThrows(IllegalArgumentException.class, () -> Parser.parseAddress("xyz"));
    }

    @Test
    void testParseAddressDecimal() {
        assertEquals(256, Parser.parseAddress("256"));
    }

    @Test
    void testParseNumberHex() {
        assertEquals(255, Parser.parseNumber("0xFF"));
    }

    @Test
    void testParseNumberDecimal() {
        assertEquals(100, Parser.parseNumber("100"));
    }

    @Test
    void testParseByteOperandChar() {
        byte[] result = Parser.parseByteOperand("C'AB'");
        assertArrayEquals(new byte[]{0x41, 0x42}, result);
    }

    @Test
    void testParseByteOperandHex() {
        byte[] result = Parser.parseByteOperand("X'FF01'");
        assertArrayEquals(new byte[]{(byte)0xFF, 0x01}, result);
    }

    @Test
    void testResolveOperandAddress() {
        // Tabela de símbolos com FOO=0x300
        SymbolTable st = new SymbolTable();
        st.addSymbol("FOO", 0x300, true);
        // Se passamos "FOO", deve retornar 0x300
        assertEquals(0x300, Parser.resolveOperandAddress("FOO", st));
        // Se passamos "#100", deve interpretar imediato=100
        assertEquals(100, Parser.resolveOperandAddress("#100", st));
        // Se passamos "200", interpreta decimal=200
        assertEquals(200, Parser.resolveOperandAddress("200", st));
    }

    @Test
    void testDetermineInstructionFormat() {
        // '+' => formato 4
        assertEquals(4, Parser.determineInstructionFormat("+LDA"));
        // CLEAR => 2
        assertEquals(2, Parser.determineInstructionFormat("CLEAR"));
        // FLOAT => 1
        assertEquals(1, Parser.determineInstructionFormat("FLOAT"));
        // LDA => 3 (padrão)
        assertEquals(3, Parser.determineInstructionFormat("LDA"));
    }
}
