package sicxesimulator.components;

import org.junit.jupiter.api.Test;
import sicxesimulator.components.Register;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest {

    @Test
    void testDefaultValueFor24BitRegister() {
        Register regA = new Register("A");
        assertEquals("000000", regA.getValue(), "O registrador A deve iniciar com '000000'.");
    }

    @Test
    void testDefaultValueFor48BitRegister() {
        Register regF = new Register("F");
        assertEquals("000000000000", regF.getValue(), "O registrador F deve iniciar com '000000000000'.");
    }

    @Test
    void testSetValue24Bit() {
        Register reg = new Register("X");
        reg.setValue("ABCDEF");
        assertEquals("ABCDEF", reg.getValue());
        // Se o valor for menor, por exemplo, "123", ele deve ser preenchido com zeros à esquerda até 6 dígitos:
        reg.setValue("123");
        assertEquals("000123", reg.getValue());
    }

    @Test
    void testSetValue48Bit() {
        Register reg = new Register("F");
        reg.setValue("123456789ABC");
        assertEquals("123456789ABC", reg.getValue());
    }
}
