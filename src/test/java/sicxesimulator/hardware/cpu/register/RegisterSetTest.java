package sicxesimulator.hardware.cpu.register;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterSetTest {

    @Test
    void getRegisterReturnsSameInstance() {
        RegisterSet rs = new RegisterSet();
        assertSame(rs.getRegister("A"), rs.getRegister("A"));
    }

    @Test
    void clearAllZerosEveryRegister() {
        RegisterSet rs = new RegisterSet();
        rs.getRegister("B").setValue(0xABCDEF);
        rs.clearAll();
        rs.getAllRegisters().forEach(r -> {
            if (!r.getName().equals("F"))
                assertEquals(0, r.getIntValue());
            else
                assertEquals(0, r.getLongValue());
        });
    }
}
