package sicxesimulatorTest.utils;

import org.junit.jupiter.api.Test;
import sicxesimulator.utils.Mapper;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    @Test
    void testMnemonicToOpcodeValid() {
        int opcode = Mapper.mnemonicToOpcode("ADD");
        assertEquals(0x18, opcode);
    }

    @Test
    void testMnemonicToOpcodeInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Mapper.mnemonicToOpcode("INVALID"));
        assertTrue(exception.getMessage().contains("Instrução desconhecida"));
    }

    @Test
    void testRegisterNameToNumber() {
        assertEquals(0, Mapper.registerNameToNumber("A"));
        assertEquals(1, Mapper.registerNameToNumber("x")); // teste case insensitive
        assertThrows(IllegalArgumentException.class, () -> Mapper.registerNameToNumber("Z"));
    }
}
