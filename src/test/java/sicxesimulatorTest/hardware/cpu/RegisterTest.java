package sicxesimulatorTest.hardware.cpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import sicxesimulator.hardware.cpu.Register;

public class RegisterTest {

    @Test
    void testInitialValueIsZero() {
        // Supondo que seja possível criar um registro via construtor com o nome
        Register reg = new Register("A");
        // Para registros de 24 bits, usamos getIntValue; para F (48 bits), getLongValue
        if (!"F".equals(reg.getName())) {
            assertEquals(0, reg.getIntValue(), "Valor inicial do registrador " + reg.getName() + " deve ser zero");
        } else {
            assertEquals(0L, reg.getLongValue(), "Valor inicial do registrador F deve ser zero");
        }
    }

    @Test
    void testSetAndGetValue() {
        Register reg = new Register("X");
        reg.setValue(12345);
        assertEquals(12345, reg.getIntValue(), "O valor do registrador X deve ser 12345");
    }

    @Test
    void testLongValueForF() {
        Register reg = new Register("F");
        reg.setValue(987654321L);
        assertEquals(987654321L, reg.getLongValue(), "O valor do registrador F deve ser 987654321");
    }
}
