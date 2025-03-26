package sicxesimulatorTest.hardware;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import sicxesimulator.hardware.Machine;
import sicxesimulator.hardware.Memory;
import sicxesimulator.hardware.cpu.RegisterSet;

public class MachineTest {

    @Test
    void testMachineInitialization() {
        // Cria uma nova instância da máquina
        Machine machine = new Machine();
        // Verifica se a memória e o conjunto de registradores estão inicializados
        Memory mem = machine.getMemory();
        RegisterSet regs = machine.getControlUnit().getRegisterSet();
        assertNotNull(mem, "A memória da máquina não deve ser nula");
        assertNotNull(regs, "O conjunto de registradores não deve ser nulo");
        // Verifica se o tamanho da memória é pelo menos 1KB, conforme especificado
        assertTrue(mem.getSize() >= 1024, "O tamanho da memória deve ser maior ou igual a 1024 bytes");
    }

    @Test
    void testMachineReset() {
        Machine machine = new Machine();
        Memory mem = machine.getMemory();
        RegisterSet regs = machine.getControlUnit().getRegisterSet();
        // Altera alguns valores na memória e nos registradores
        mem.writeByte(0, 0x55);
        regs.getRegister("A").setValue(999);
        // Chama o reset da máquina
        machine.reset();
        // Após o reset, espera-se que a memória esteja zerada e os registradores resetados
        assertEquals(0, mem.readByte(0), "Após reset, o byte na memória deve ser 0");
        assertEquals(0, regs.getRegister("A").getIntValue(), "Após reset, o registrador A deve ser 0");
    }
}
