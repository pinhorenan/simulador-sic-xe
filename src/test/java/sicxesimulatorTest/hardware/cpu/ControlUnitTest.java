package sicxesimulatorTest.hardware.cpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sicxesimulator.hardware.cpu.ControlUnit;
import sicxesimulator.hardware.data.Instruction;
import sicxesimulator.hardware.Memory;
import sicxesimulatorTest.hardware.cpu.dummies.Dummies;
import sicxesimulatorTest.hardware.cpu.dummies.Dummies.DummyInstructionDecoder;
import sicxesimulatorTest.hardware.cpu.dummies.Dummies.DummyExecutionUnit;
import sicxesimulatorTest.hardware.cpu.dummies.Dummies.DummyMemory;

import java.lang.reflect.Field;

public class ControlUnitTest {

    @Test
    void testStepRSUB() throws Exception {
        Memory dummyMemory = new DummyMemory();
        ControlUnit cu = new ControlUnit(dummyMemory);
        cu.setIntValuePC(0x1000);

        Instruction dummyInstruction = Dummies.createDummyInstructionRSUB();
        Field decoderField = ControlUnit.class.getDeclaredField("decoder");
        decoderField.setAccessible(true);
        decoderField.set(cu, new DummyInstructionDecoder(cu.getRegisterSet(), dummyMemory, dummyInstruction));

        Field executionUnitField = ControlUnit.class.getDeclaredField("executionUnit");
        executionUnitField.setAccessible(true);
        executionUnitField.set(cu, new DummyExecutionUnit(cu.getRegisterSet(), dummyMemory));

        cu.step();

        assertEquals(0x1000 + 3, cu.getIntValuePC(), "PC deve ser incrementado em 3");
        assertEquals("RSUB executed - HALT", cu.getLastExecutionLog());
        assertTrue(cu.isProcessorHalted(), "Processador deve estar halted após RSUB");
        assertTrue(cu.getExecutionHistory().contains("RSUB executed - HALT"), "Histórico deve conter RSUB executed - HALT");
    }

    @Test
    void testFormat2_CLEAR_LDX() throws Exception {
        Memory dummyMemory = new DummyMemory();
        ControlUnit cu = new ControlUnit(dummyMemory);
        cu.setIntValuePC(0x2000);

        // Cria uma instrução dummy de formato 2 com opcode 0x04 (CLEAR_LDX)
        Instruction instrClear = new Instruction(0x04, new int[]{1, 2}, 2, false, 0);
        DummyInstructionDecoder decoder = new DummyInstructionDecoder(cu.getRegisterSet(), dummyMemory, instrClear);
        Field decoderField = ControlUnit.class.getDeclaredField("decoder");
        decoderField.setAccessible(true);
        decoderField.set(cu, decoder);

        // Cria um ExecutionUnit dummy que sobrescreve executeCLEAR_LDX
        DummyExecutionUnit dummyExec = new DummyExecutionUnit(cu.getRegisterSet(), dummyMemory) {
            @Override
            public String executeCLEAR_LDX(Instruction instr, int[] operands) {
                return "CLEAR_LDX executed";
            }
        };
        Field execField = ControlUnit.class.getDeclaredField("executionUnit");
        execField.setAccessible(true);
        execField.set(cu, dummyExec);

        cu.step();

        // Para instrução de formato 2, o tamanho é 2 bytes
        assertEquals(0x2000 + 2, cu.getIntValuePC(), "PC deve ser incrementado em 2");
        assertEquals("CLEAR_LDX executed", cu.getLastExecutionLog());
        assertFalse(cu.isProcessorHalted());
        assertTrue(cu.getExecutionHistory().contains("CLEAR_LDX executed"));
    }

    @Test
    void testFormat2_ADDR() throws Exception {
        Memory dummyMemory = new DummyMemory();
        ControlUnit cu = new ControlUnit(dummyMemory);
        cu.setIntValuePC(0x3000);

        // Cria uma instrução dummy de formato 2 com opcode 0x90 (ADDR)
        Instruction instrAddr = new Instruction(0x90, new int[]{5, 7}, 2, false, 0);
        DummyInstructionDecoder decoder = new DummyInstructionDecoder(cu.getRegisterSet(), dummyMemory, instrAddr);
        Field decoderField = ControlUnit.class.getDeclaredField("decoder");
        decoderField.setAccessible(true);
        decoderField.set(cu, decoder);

        // Cria um ExecutionUnit dummy que sobrescreve executeADDR
        DummyExecutionUnit dummyExec = new DummyExecutionUnit(cu.getRegisterSet(), dummyMemory) {
            @Override
            public String executeADDR(int[] operands) {
                return "ADDR executed";
            }
        };
        Field execField = ControlUnit.class.getDeclaredField("executionUnit");
        execField.setAccessible(true);
        execField.set(cu, dummyExec);

        cu.step();

        // Para instrução de formato 2, o tamanho é 2 bytes
        assertEquals(0x3000 + 2, cu.getIntValuePC(), "PC deve ser incrementado em 2");
        assertEquals("ADDR executed", cu.getLastExecutionLog());
        assertFalse(cu.isProcessorHalted());
        assertTrue(cu.getExecutionHistory().contains("ADDR executed"));
    }

    @Test
    void testFormat3_ADD() throws Exception {
        Memory dummyMemory = new DummyMemory();
        ControlUnit cu = new ControlUnit(dummyMemory);
        cu.setIntValuePC(0x4000);

        // Cria uma instrução dummy de formato 3 com opcode 0x18 (ADD)
        Instruction instrAdd = new Instruction(0x18, new int[]{10}, 3, false, 0x5000);
        DummyInstructionDecoder decoder = new DummyInstructionDecoder(cu.getRegisterSet(), dummyMemory, instrAdd);
        Field decoderField = ControlUnit.class.getDeclaredField("decoder");
        decoderField.setAccessible(true);
        decoderField.set(cu, decoder);

        // Cria um ExecutionUnit dummy que sobrescreve executeADD
        DummyExecutionUnit dummyExec = new DummyExecutionUnit(cu.getRegisterSet(), dummyMemory) {
            @Override
            public String executeADD(int[] operands, boolean indexed, int effectiveAddress) {
                return "ADD executed";
            }
        };
        Field execField = ControlUnit.class.getDeclaredField("executionUnit");
        execField.setAccessible(true);
        execField.set(cu, dummyExec);

        cu.step();

        // Para instrução de formato 3, o tamanho é 3 bytes
        assertEquals(0x4000 + 3, cu.getIntValuePC(), "PC deve ser incrementado em 3");
        assertEquals("ADD executed", cu.getLastExecutionLog());
        assertFalse(cu.isProcessorHalted());
        assertTrue(cu.getExecutionHistory().contains("ADD executed"));
    }

    @Test
    void testReset() throws Exception {
        Memory dummyMemory = new DummyMemory();
        ControlUnit cu = new ControlUnit(dummyMemory);
        cu.setIntValuePC(0x5000);
        cu.setHalted(true);

        // Simula execução para definir lastExecutionLog e modificar o estado
        Instruction dummyInstruction = Dummies.createDummyInstructionRSUB();
        DummyInstructionDecoder decoder = new DummyInstructionDecoder(cu.getRegisterSet(), dummyMemory, dummyInstruction);
        Field decoderField = ControlUnit.class.getDeclaredField("decoder");
        decoderField.setAccessible(true);
        decoderField.set(cu, decoder);

        DummyExecutionUnit dummyExec = new DummyExecutionUnit(cu.getRegisterSet(), dummyMemory);
        Field execField = ControlUnit.class.getDeclaredField("executionUnit");
        execField.setAccessible(true);
        execField.set(cu, dummyExec);

        cu.step();
        // Após a execução, lastExecutionLog e halted estão definidos; agora reseta a ControlUnit
        cu.reset();

        assertNull(cu.getLastExecutionLog(), "lastExecutionLog deve ser null após reset");
        assertFalse(cu.isProcessorHalted(), "Processador deve estar ativo após reset");
        // Após reset, os registradores são limpos; assumindo que o PC é zerado
        assertEquals(0, cu.getIntValuePC(), "PC deve ser 0 após reset");
    }

    @Test
    void testClearAllRegisters() {
        Memory dummyMemory = new DummyMemory();
        ControlUnit cu = new ControlUnit(dummyMemory);
        cu.setIntValuePC(0x1234);
        cu.clearAllRegisters();
        assertEquals(0, cu.getIntValuePC(), "PC deve ser zerado após clearAllRegisters");
    }
}
