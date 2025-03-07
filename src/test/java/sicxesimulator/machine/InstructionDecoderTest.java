package sicxesimulator.machine;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sicxesimulator.machine.cpu.ControlUnit;
import sicxesimulator.machine.cpu.RegisterSet;
import sicxesimulator.machine.memory.Memory;

public class InstructionDecoderTest {

    private Memory memory;
    private ControlUnit controlUnit;
    private RegisterSet registers;

    @BeforeEach
    public void setUp() {
        memory = new Memory(1024);
        controlUnit = new ControlUnit(memory);
        registers = controlUnit.getRegisterSet();
        registers.getRegister("PC").setValue(0);
    }

    @Test
    public void testFetchDecodeAndExecuteCycle() {
        // Prepara uma instrução LDA (opcode 0x00) no formato 3 com endereço absoluto 9 (byte address)
        byte[] ldaInstruction = {0x03, 0x00, 0x09}; // 0x03 & 0xFC = 0x00 -> LDA
        memory.writeByte(0, 0, ldaInstruction[0]);
        memory.writeByte(0, 1, ldaInstruction[1]);
        memory.writeByte(0, 2, ldaInstruction[2]);

        // Valor que será carregado em A, armazenado no endereço 9 (em bytes)
        byte[] dataAt9 = {0x00, 0x10, 0x20};
        // Ajuste: escreve na palavra correta (9/3 = 3)
        memory.writeWord(9 / 3, dataAt9);

        registers.getRegister("PC").setValue(0);

        controlUnit.step();

        int expectedValue = ((dataAt9[0] & 0xFF) << 16)
                | ((dataAt9[1] & 0xFF) << 8)
                | (dataAt9[2] & 0xFF);

        int regAValue = registers.getRegister("A").getIntValue();
        assertEquals(expectedValue, regAValue);
    }

    @Test
    public void testPCIncrementAfterDecode() {
        byte[] instruction = {0x4F, 0x00, 0x00}; // Exemplo RSUB formato 3
        memory.writeByte(0, 0, instruction[0]);
        memory.writeByte(0, 1, instruction[1]);
        memory.writeByte(0, 2, instruction[2]);

        registers.getRegister("PC").setValue(0);
        // Configure L para o valor esperado, de forma que RSUB faça PC ← L
        registers.getRegister("L").setValue(3);

        controlUnit.step();

        assertEquals(3, registers.getRegister("PC").getIntValue());
    }

}
