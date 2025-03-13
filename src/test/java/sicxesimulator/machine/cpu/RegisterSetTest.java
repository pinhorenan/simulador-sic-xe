package sicxesimulator.machine.cpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RegisterSetTest {

    @Test
    public void testAllRegistersExist() {
        RegisterSet regSet = new RegisterSet();
        String[] expected = {"A", "X", "L", "B", "S", "T", "F", "PC", "SW"};
        for (String regName : expected) {
            Register reg = regSet.getRegister(regName);
            assertNotNull(reg, "Registrador " + regName + " deve existir");
        }
    }

    @Test
    public void testClearAllRegisters() {
        RegisterSet regSet = new RegisterSet();
        regSet.getRegister("A").setValue(0xABCDEF);
        regSet.clearAll();
        assertEquals(0, regSet.getRegister("A").getIntValue());
    }
}
