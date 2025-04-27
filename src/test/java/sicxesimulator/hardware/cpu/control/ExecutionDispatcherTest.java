package sicxesimulator.hardware.cpu.control;

import org.junit.jupiter.api.Test;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.RegisterSet;
import sicxesimulator.hardware.memory.Memory;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionDispatcherTest {

    @Test
    void customRegistrationAndDispatchWorks() {
        ExecutionDispatcher d = new ExecutionDispatcher();
        d.register(0xFF, ctx -> "OK");

        ExecutionContext dummy =
                new ExecutionContext(new int[0], false, 0, new RegisterSet(), new Memory(1));

        assertEquals("OK", d.dispatch(0xFF, dummy));
        assertThrows(IllegalStateException.class,
                () -> d.dispatch(0xAB, dummy));      // opcode não registrado
    }
}
