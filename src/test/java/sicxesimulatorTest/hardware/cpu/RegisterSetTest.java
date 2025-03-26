package sicxesimulatorTest.hardware.cpu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import sicxesimulator.hardware.cpu.RegisterSet;
import sicxesimulator.hardware.cpu.Register;

public class RegisterSetTest {

    private RegisterSet registerSet;

    @BeforeEach
    void setUp() {
        registerSet = new RegisterSet();
    }

    @Test
    void testGetRegisterByName() {
        // Testa se os registradores básicos estão disponíveis
        String[] registerNames = {"A", "X", "L", "B", "S", "T", "F", "PC", "SW"};
        for (String name : registerNames) {
            Register reg = registerSet.getRegister(name);
            assertNotNull(reg, "O registrador " + name + " não deve ser nulo");
            // Valor inicial deve ser zero
            if (!"F".equals(name)) {
                assertEquals(0, reg.getIntValue(), "Valor inicial de " + name + " deve ser zero");
            } else {
                assertEquals(0L, reg.getLongValue(), "Valor inicial de F deve ser zero");
            }
        }
    }

    @Test
    void testClearAllRegisters() {
        // Define valores em alguns registradores
        registerSet.getRegister("A").setValue(100);
        registerSet.getRegister("X").setValue(200);
        // Chama o clearAll e verifica se os valores foram resetados para 0
        registerSet.clearAll();
        assertEquals(0, registerSet.getRegister("A").getIntValue(), "Registrador A deve ser zerado após clearAll");
        assertEquals(0, registerSet.getRegister("X").getIntValue(), "Registrador X deve ser zerado após clearAll");
    }
}
