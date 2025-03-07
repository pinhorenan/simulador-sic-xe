package sicxesimulator.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MachineTest {

    private Machine machine;

    @BeforeEach
    public void setUp() {
        // Inicializa a máquina com um tamanho arbitrário (por exemplo, 24576 bytes conforme o construtor)
        machine = new Machine();
    }

    @Test
    public void testInitialMemorySize() {
        // Verifica se o tamanho da memória está conforme especificado
        int size = machine.getMemorySize();
        assertTrue(size >= 1024);
    }

    @Test
    public void testRunCycleAndReset() {
        // Simula um ciclo simples: escreva uma instrução na memória e rode um ciclo.
        byte[] fakeInstruction = {0x00, 0x00, 0x09};
        machine.getMemory().writeWord(0, fakeInstruction);

        int initialPC = machine.getControlUnit().getIntValuePC();
        machine.runCycle();
        int newPC = machine.getControlUnit().getIntValuePC();
        // Espera que o PC seja incrementado em 3 bytes
        assertEquals(initialPC + 3, newPC);

        // Testa o reset da máquina
        machine.reset();
        assertEquals(0, machine.getControlUnit().getIntValuePC());
    }
}
