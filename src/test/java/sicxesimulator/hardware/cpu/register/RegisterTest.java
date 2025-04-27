package sicxesimulator.hardware.cpu.register;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterTest {

    @Test
    void maskingWorksFor24bitRegisters() {
        Register a = new Register("A");
        a.setValue(0x1FF_FFFF);              // >24 bits — deve truncar
        assertEquals(0xFF_FFFF, a.getIntValue());
    }

    @Test
    void maskingWorksFor48bitRegisters() {
        Register f = new Register("F");
        f.setValue(0x1_FFFF_FFFF_FFFL);      // >48 bits — deve truncar
        assertEquals(0xFFFF_FFFF_FFFL, f.getLongValue());
    }

    @Test
    void wrongGetterThrows() {
        Register f = new Register("F");      // 48 bits
        assertThrows(IllegalStateException.class, f::getIntValue);
    }

    @Test
    void clearRegisterZerosValue() {
        Register x = new Register("X");
        x.setValue(0x123456);
        x.clearRegister();
        assertEquals(0, x.getIntValue());
    }
}
