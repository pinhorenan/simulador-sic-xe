package sicxesimulatorTest.hardware.cpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sicxesimulator.hardware.cpu.InstructionDecoder;
import sicxesimulator.hardware.cpu.RegisterSet;
import sicxesimulator.hardware.data.Instruction;
import sicxesimulator.hardware.Memory;

/**
 * Classe de teste para InstructionDecoder.
 */
public class InstructionDecoderTest {

    /**
     * Implementação de memória para testes, baseada em um array de bytes.
     */
    private static class TestMemory extends Memory {
        private final byte[] data;

        public TestMemory(byte[] data) {
            super(data.length); // assume que Memory tem um construtor que recebe o tamanho
            this.data = data;
        }

        @Override
        public int getAddressRange() {
            return data.length;
        }

        @Override
        public int readByte(int address) {
            return data[address] & 0xFF;
        }

        @Override
        public byte[] readWord(int index) {
            int start = index * 3;
            byte[] word = new byte[3];
            System.arraycopy(data, start, word, 0, 3);
            return word;
        }
    }

    @Test
    void testDecodeFormat1() {
        // Formato 1: se o primeiro byte for um dos: 0xC4, 0xC0, 0xC8, 0xF4, 0xF0 ou 0xF8.
        // Usaremos 0xF4.
        byte[] memData = new byte[10];
        memData[0] = (byte)0xF4;
        TestMemory memory = new TestMemory(memData);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(0);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction instr = decoder.decodeInstruction();

        // Espera-se formato 1: opcode igual a 0xF4, sem operandos e EA zero.
        assertEquals(1, instr.format());
        assertEquals(0xF4, instr.opcode());
        assertEquals(0, instr.operands().length);
        assertEquals(0, instr.effectiveAddress());
        // PC deve ter sido incrementado em 1.
        assertEquals(1, registers.getRegister("PC").getIntValue());
    }

    @Test
    void testDecodeFormat2() {
        // Formato 2: se o primeiro byte for 0x04 (ou 0x90).
        // Colocamos: PC=0, mem[0]=0x04, mem[1]=0xAB (0xA = 10, 0xB = 11).
        byte[] memData = new byte[10];
        memData[0] = (byte)0x04;
        memData[1] = (byte)0xAB;
        TestMemory memory = new TestMemory(memData);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(0);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction instr = decoder.decodeInstruction();

        // Espera-se formato 2, opcode = 0x04 e operandos [10, 11].
        assertEquals(2, instr.format());
        assertEquals(0x04, instr.opcode());
        int[] ops = instr.operands();
        assertEquals(2, ops.length);
        assertEquals(10, ops[0]);
        assertEquals(11, ops[1]);
        assertEquals(0, instr.effectiveAddress());
        // Para formato 2, o PC não é atualizado internamente.
        assertEquals(0, registers.getRegister("PC").getIntValue());
    }

    @Test
    void testDecodeFormat3Direct() {
        // Testa uma instrução formato 3 em modo direto (n=0, i=0).
        // Usamos: firstByte = 0x18; secondByte = 0x20; thirdByte = 0x0A.
        // Para firstByte 0x18: n = (0x18>>1)&1 = 0, i = 0.
        // Para secondByte 0x20: x=0, b=0, p = (0x20 & 0x20)>>5 = 1, e = 0, high4 = 0.
        // disp12 = (0<<8)|0x0A = 10.
        // Se PC = 100, efetiva: EA = 10 + PC + 3 = 10 + 100 + 3 = 113.
        byte[] memData = new byte[110]; // array maior para incluir índice 100, 101 e 102
        memData[100] = (byte)0x18;
        memData[101] = (byte)0x20;
        memData[102] = (byte)0x0A;
        TestMemory memory = new TestMemory(memData);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(100);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction instr = decoder.decodeInstruction();

        int[] expectedOps = new int[]{10, 0, 0, 1, 0, 0, 0};
        assertEquals(3, instr.format());
        assertEquals(0x18, instr.opcode());
        assertArrayEquals(expectedOps, instr.operands());
        assertEquals(113, instr.effectiveAddress());
        // PC permanece 100, pois o decoder não atualiza PC para formato 3
        assertEquals(100, registers.getRegister("PC").getIntValue());
    }

    @Test
    void testDecodeFormat4Direct() {
        // Testa uma instrução formato 4 em modo direto.
        // Usamos: firstByte = 0x18; secondByte = 0x30 (e==1, p==1); thirdByte = 0x01; fourthByte = 0x02.
        // Para secondByte 0x30: x=0, b=0, p=1, e=1, high4 = 0.
        // addr20 = (0<<16)|(0x01<<8)|0x02 = 258.
        // EA = 258 + PC + 4, se PC = 100 => 258 + 100 + 4 = 362.
        byte[] memData = new byte[110]; // array maior para índice 100 a 103
        memData[100] = (byte)0x18;
        memData[101] = (byte)0x30;
        memData[102] = (byte)0x01;
        memData[103] = (byte)0x02;
        TestMemory memory = new TestMemory(memData);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(100);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction instr = decoder.decodeInstruction();

        int[] expectedOps = new int[]{258, 0, 0, 1, 1, 0, 0};
        assertEquals(4, instr.format());
        assertEquals(0x18, instr.opcode());
        assertArrayEquals(expectedOps, instr.operands());
        assertEquals(362, instr.effectiveAddress());
        assertEquals(100, registers.getRegister("PC").getIntValue());
    }

    @Test
    void testDecodeImmediateMode() {
        // Testa modo imediato: n=0 e i=1.
        // Usamos: firstByte = 0x19 (0001 1001: n=0, i=1); segundo byte = 0x20; terceiro byte = 0x0A.
        // Assim, disp12 = 10, e o EA imediato deve ser 10.
        byte[] memData = new byte[10];
        memData[0] = (byte)0x19;
        memData[1] = (byte)0x20;
        memData[2] = (byte)0x0A;
        TestMemory memory = new TestMemory(memData);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(0);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction instr = decoder.decodeInstruction();

        int[] expectedOps = new int[]{10, 0, 0, 1, 0, 0, 1}; // disp12=10, x=0, b=0, p=1, e=0, n=0, i=1.
        assertEquals(3, instr.format());
        assertEquals(0x18, instr.opcode()); // opcode = firstByte & 0xFC
        assertArrayEquals(expectedOps, instr.operands());
        // EA = immediate (10)
        assertEquals(10, instr.effectiveAddress());
    }

    @Test
    void testDecodeIndirectMode() {
        // Testa modo indireto: n=1 e i=0.
        // Usamos: firstByte = 0x1A (0001 1010: n=1, i=0).
        // Para formato 3, escolhemos segundo byte = 0x00 (x=0, b=0, p=0, e=0, high4=0) e terceiro byte = 0x0C, assim disp12 = 12.
        // Como p==0, EA = addressBase = 12 e, em modo indireto, deve ler a memória em readWord(12/3=4).
        // Configuramos o TestMemory para que, ao ler word no índice 4, retorne [0x00, 0x00, 0xC8] (valor 200).
        byte[] memData = new byte[50]; // tamanho suficiente para acessar índice 12 e índice 4*3 = 12
        memData[0] = (byte)0x1A;
        memData[1] = (byte)0x00;
        memData[2] = (byte)0x0C;
        // Define os bytes para o word em índice 4 (começa no offset 12)
        memData[12] = 0x00;
        memData[13] = 0x00;
        memData[14] = (byte)0xC8; // 0xC8 = 200
        TestMemory memory = new TestMemory(memData);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(0);

        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        Instruction instr = decoder.decodeInstruction();

        int[] expectedOps = new int[]{12, 0, 0, 0, 0, 1, 0};
        assertEquals(3, instr.format());
        assertEquals(0x18, instr.opcode()); // firstByte & 0xFC de 0x1A = 0x18.
        assertArrayEquals(expectedOps, instr.operands());
        // Em modo indireto, EA é o valor lido na memória: 200.
        assertEquals(200, instr.effectiveAddress());
    }

    @Test
    void testResetProgramCounter() {
        // Testa se resetProgramCounter zera o PC.
        byte[] memData = new byte[10];
        TestMemory memory = new TestMemory(memData);
        RegisterSet registers = new RegisterSet();
        registers.getRegister("PC").setValue(500);
        InstructionDecoder decoder = new InstructionDecoder(registers, memory);
        decoder.resetProgramCounter();
        assertEquals(0, registers.getRegister("PC").getIntValue());
    }
}
