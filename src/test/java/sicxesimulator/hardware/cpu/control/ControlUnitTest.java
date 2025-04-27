package sicxesimulator.hardware.cpu.control;

import org.junit.jupiter.api.Test;
import sicxesimulator.hardware.memory.Memory;

import static org.junit.jupiter.api.Assertions.*;

class ControlUnitTest {

    @Test
    void stepIncrementsPcAndLogsHistory() {
        Memory mem = new Memory(16);
        // RSUB (0x4C) formato 3: 0x4C 0x00 0x00
        mem.writeByte(0, 0x4C);
        mem.writeByte(1, 0x00);
        mem.writeByte(2, 0x00);

        ControlUnit cu = new ControlUnit(mem);
        assertEquals(0, cu.getIntValuePC());

        cu.step();                                   // executa 1 instrução
        assertEquals(3, cu.getIntValuePC());         // PC avançou
        assertEquals(1, cu.getExecutionHistory().size());
    }

    @Test
    void resetRestoresInitialState() {
        Memory mem = new Memory(16);
        ControlUnit cu = new ControlUnit(mem);
        cu.setIntValuePC(12);
        cu.setHalted(true);

        cu.reset();
        assertEquals(0, cu.getIntValuePC());
        assertFalse(cu.isHalted());
        assertTrue(cu.getExecutionHistory().isEmpty());
    }
}
