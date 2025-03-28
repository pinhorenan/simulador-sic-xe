package sicxesimulatorTest.hardware.cpu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sicxesimulator.hardware.cpu.ExecutionUnit;
import sicxesimulator.hardware.cpu.RegisterSet;
import sicxesimulator.hardware.Memory;
import sicxesimulator.utils.Convert;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionUnitTest {

    private ExecutionUnit executionUnit;
    private RegisterSet registers;
    private TestMemory memory;

    private static class TestMemory extends Memory {
        byte[] data;

        public TestMemory(int size) {
            super(size);
            data = new byte[size];
        }

        @Override
        public int getAddressRange() {
            return data.length;
        }

        @Override
        public int readByte(int byteAddress) {
            return data[byteAddress] & 0xFF;
        }

        @Override
        public byte[] readWord(int index) {
            byte[] word = new byte[3];
            System.arraycopy(data, index * 3, word, 0, 3);
            return word;
        }

        @Override
        public void writeWord(int index, byte[] word) {
            System.arraycopy(word, 0, data, index * 3, 3);
        }

        @Override
        public void writeByte(int byteAddress, int value) {
            data[byteAddress] = (byte) (value & 0xFF);
        }
    }

    @BeforeEach
    void setUp() {
        registers = new RegisterSet();
        memory = new TestMemory(1024);
        executionUnit = new ExecutionUnit(registers, memory);
    }

    @Test
    void testExecuteADD() {
        registers.getRegister("A").setValue(10);
        memory.writeWord(0, Convert.intTo3Bytes(20));

        String log = executionUnit.executeADD(new int[]{0,0,0,0,0,1,1}, false, 0);
        assertEquals(30, registers.getRegister("A").getIntValue());
        assertTrue(log.contains("ADD"));
    }

    @Test
    void testExecuteADDR() {
        registers.getRegister("A").setValue(5);
        registers.getRegister("X").setValue(7);

        String log = executionUnit.executeADDR(new int[]{0, 1});
        assertEquals(12, registers.getRegister("X").getIntValue());
        assertTrue(log.contains("ADDR"));
    }

    @Test
    void testExecuteSUB_ImmediateMode() {
        registers.getRegister("A").setValue(50);

        String log = executionUnit.executeSUB(new int[]{0,0,0,0,0,0,1}, false, 30);
        assertEquals(20, registers.getRegister("A").getIntValue());
        assertTrue(log.contains("SUB"));
    }

    @Test
    void testExecuteDIV_DivisionByZero() {
        registers.getRegister("A").setValue(10);
        memory.writeWord(0, Convert.intTo3Bytes(0));

        assertThrows(ArithmeticException.class,
                () -> executionUnit.executeDIV(new int[]{0,0,0,0,0,1,1}, false, 0));
    }

    @Test
    void testExecuteJ() {
        executionUnit.executeJ(new int[0], false, 0x300);
        assertEquals(0x300, registers.getRegister("PC").getIntValue());
    }

    @Test
    void testExecuteJEQ_ConditionTrue() {
        registers.getRegister("SW").setValue(0);
        executionUnit.executeJEQ(new int[0], false, 0x200);
        assertEquals(0x200, registers.getRegister("PC").getIntValue());
    }

    @Test
    void testExecuteJEQ_ConditionFalse() {
        registers.getRegister("SW").setValue(1);
        executionUnit.executeJEQ(new int[0], false, 0x200);
        assertNotEquals(0x200, registers.getRegister("PC").getIntValue());
    }

    @Test
    void testExecuteLDA_DirectMode() {
        memory.writeWord(0, Convert.intTo3Bytes(123456));
        executionUnit.executeLDA(new int[]{0,0,0,0,0,1,1}, false, 0);
        assertEquals(123456, registers.getRegister("A").getIntValue());
    }

    @Test
    void testExecuteLDA_ImmediateMode() {
        executionUnit.executeLDA(new int[]{0,0,0,0,0,0,1}, false, 789);
        assertEquals(789, registers.getRegister("A").getIntValue());
    }

    @Test
    void testExecuteSTA() {
        registers.getRegister("A").setValue(654321);
        executionUnit.executeSTA(new int[]{0,0,0,0,0,1,1}, false, 3);
        assertArrayEquals(Convert.intTo3Bytes(654321), memory.readWord(1));
    }

    @Test
    void testExecuteRSUB_HALT() {
        registers.getRegister("L").setValue(0);
        String log = executionUnit.executeRSUB();
        assertEquals(0, registers.getRegister("PC").getIntValue());
        assertTrue(log.contains("HALT"));
    }

    @Test
    void testExecuteRSUB_Return() {
        registers.getRegister("L").setValue(0x123456);
        String log = executionUnit.executeRSUB();
        assertEquals(0x123456, registers.getRegister("PC").getIntValue());
        assertTrue(log.contains("RSUB"));
    }

    @Test
    void testExecuteLDCH() {
        memory.writeByte(5, 0x7F);
        registers.getRegister("A").setValue(0xFFFFFF);
        executionUnit.executeLDCH(new int[]{0,0,0,0,0,1,1}, false, 5);
        assertEquals(0xFFFF7F, registers.getRegister("A").getIntValue());
    }

    @Test
    void testExecuteSHIFTL() {
        registers.getRegister("A").setValue(0x123456);
        executionUnit.executeSHIFTL(new int[]{0, 4});
        assertEquals(0x234560, registers.getRegister("A").getIntValue());
    }

    @Test
    void testExecuteCOMPR_Equal() {
        registers.getRegister("A").setValue(100);
        registers.getRegister("X").setValue(100);
        executionUnit.executeCOMPR(new int[]{0, 1});
        assertEquals(0, registers.getRegister("SW").getIntValue());
    }

    @Test
    void testExecuteCOMPR_Greater() {
        registers.getRegister("A").setValue(150);
        registers.getRegister("X").setValue(100);
        executionUnit.executeCOMPR(new int[]{0, 1});
        assertEquals(2, registers.getRegister("SW").getIntValue());
    }

    @Test
    void testExecuteCOMPR_Less() {
        registers.getRegister("A").setValue(50);
        registers.getRegister("X").setValue(100);
        executionUnit.executeCOMPR(new int[]{0, 1});
        assertEquals(1, registers.getRegister("SW").getIntValue());
    }

    @Test
    void testExecuteMULF() {
        registers.getRegister("F").setValue(2);
        memory.writeWord(0, new byte[]{0,0,0});
        memory.writeWord(1, new byte[]{0,0,5}); // valor 5 em 48 bits
        executionUnit.executeMULF(new int[0], false, 0);
        assertEquals(10, registers.getRegister("F").getLongValue());
    }

    @Test
    void testExecuteDIVF_DivisionByZero() {
        registers.getRegister("F").setValue(123456);
        memory.writeWord(0, new byte[]{0,0,0});
        memory.writeWord(1, new byte[]{0,0,0}); // divisor zero
        assertThrows(ArithmeticException.class,
                () -> executionUnit.executeDIVF(new int[0], false, 0));
    }
}
