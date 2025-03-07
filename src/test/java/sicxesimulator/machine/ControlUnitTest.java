package sicxesimulator.machine;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sicxesimulator.machine.cpu.ControlUnit;
import sicxesimulator.machine.cpu.RegisterSet;
import sicxesimulator.machine.memory.Memory;

public class ControlUnitTest {

    private Memory memory;
    private ControlUnit controlUnit;
    private RegisterSet registers;

    @BeforeEach
    public void setUp() {
        memory = new Memory(1024);
        controlUnit = new ControlUnit(memory);
        registers = controlUnit.getRegisterSet();
        registers.getRegister("PC").setValue(0);
    }

    @Test
    public void testFetchDecodeAndExecuteCycle() {
        byte[] ldaInstruction = {0x00, 0x00, 0x09};
        memory.writeWord(0, ldaInstruction);

        byte[] value = {0x01, 0x02, 0x03};
        memory.writeWord(3, value);

        controlUnit.fetch();
        controlUnit.decode();
        String log = controlUnit.execute();

        int expectedValue = ((value[0] & 0xFF) << 16)
                | ((value[1] & 0xFF) << 8)
                | (value[2] & 0xFF);
        int regAValue = registers.getRegister("A").getIntValue();
        assertEquals(expectedValue, regAValue);

        assertTrue(log.contains("LDA"));
    }

    @Test
    public void testPCIncrementAfterDecode() {
        byte[] instruction = {0x00, 0x00, 0x09};
        memory.writeWord(0, instruction);
        registers.getRegister("PC").setValue(0);

        controlUnit.fetch();
        controlUnit.decode();

        assertEquals(3, registers.getRegister("PC").getIntValue());
    }
}
