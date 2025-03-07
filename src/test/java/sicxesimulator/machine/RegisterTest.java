package sicxesimulator.machine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sicxesimulator.machine.cpu.Register;

public class RegisterTest {

    @Test
    public void testRegister24BitValue() {
        Register regA = new Register("A");
        regA.setValue(0x123456);
        assertEquals(0x123456, regA.getIntValue());
    }

    @Test
    public void testGetIntValueOn48BitRegister() {
        // Em JUnit 4: @Test(expected = IllegalStateException.class)
        // Em JUnit 5: usamos assertThrows
        Register regF = new Register("F");
        // Deve lançar exceção, pois F é de 48 bits
        assertThrows(IllegalStateException.class, regF::getIntValue);
    }

    @Test
    public void testGetLongValueOn48BitRegister() {
        Register regF = new Register("F");
        long value = 0x123456789ABL;
        regF.setValue(value);
        assertEquals(value & 0xFFFFFFFFFFFFL, regF.getLongValue());
    }

    @Test
    public void testClearRegister() {
        Register regX = new Register("X");
        regX.setValue(0xFFFFFF);
        regX.clearRegister();
        assertEquals(0, regX.getIntValue());
    }
}
