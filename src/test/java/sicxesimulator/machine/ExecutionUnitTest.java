package sicxesimulator.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sicxesimulator.machine.cpu.ExecutionUnit;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.machine.cpu.RegisterSet;
import sicxesimulator.machine.memory.Memory;
import sicxesimulator.utils.Convert;

public class ExecutionUnitTest {

    private Memory memory;
    private RegisterSet registers;
    private ExecutionUnit executionUnit;

    @BeforeEach
    public void setUp() {
        memory = new Memory(1024);
        registers = new RegisterSet();
        executionUnit = new ExecutionUnit(registers, memory);
    }

    @Test
    public void testExecuteADD() {
        // Configura o registrador A com um valor e coloca outro valor na memória para somar
        Register regA = registers.getRegister("A");
        regA.setValue(0x000010);

        byte[] memData = Convert.intTo3Bytes(0x000020); // Converter 32 para 3 bytes
        memory.writeWord(5, memData); // endereço 5 (em palavras)

        // Chama a execução do ADD: o effectiveAddress deve ser 5 * 3 = 15
        String log = executionUnit.executeADD(new int[]{}, false, 15);

        // Resultado esperado: 0x10 + 0x20 = 0x30
        assertEquals(0x30, regA.getIntValue());
        assertTrue(log.contains("ADD"));
    }

    @Test
    public void testExecuteRSUB() {
        // Configura o registrador L com um endereço de retorno e testa o RSUB
        Register regL = registers.getRegister("L");
        Register regPC = registers.getRegister("PC");
        regL.setValue(0x000100);

        String log = executionUnit.executeRSUB();
        assertEquals(0x000100, regPC.getIntValue());
        assertTrue(log.contains("RSUB"));
    }

    @Test
    public void testDivideByZero() {
        // Configura o registrador A e coloca zero na memória para forçar divisão por zero
        Register regA = registers.getRegister("A");
        regA.setValue(0x100);

        byte[] zeroData = Convert.intTo3Bytes(0x0);
        memory.writeWord(4, zeroData);

        // JUnit 5: use assertThrows para verificar exceção
        assertThrows(ArithmeticException.class, () -> executionUnit.executeDIV(new int[]{}, false, 4*3));
    }
}
