package sicxesimulator.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sicxesimulator.machine.cpu.Instruction;
import sicxesimulator.machine.cpu.InstructionDecoder;
import sicxesimulator.machine.cpu.RegisterSet;
import sicxesimulator.machine.memory.Memory;

public class InstructionDecoderTest {

    private RegisterSet registers;
    private InstructionDecoder decoder;

    @BeforeEach
    public void setUp() {
        // Cria uma memória com 1024 bytes (mínimo) e inicializa os registradores.
        Memory memory = new Memory(1024);
        registers = new RegisterSet();
        decoder = new InstructionDecoder(registers, memory);
    }

    @Test
    public void testDecodeFormat3Instruction() {
        // Supondo que queremos testar uma instrução LDA no formato 3
        // Exemplo: opcode LDA (0x00) seguido de bytes que definam deslocamento, flags, etc.
        byte[] instructionBytes = {0x00, 0x00, 0x09};
        decoder.setFetchedBytes(instructionBytes);
        // Define o PC manualmente (por exemplo, 0)
        registers.getRegister("PC").setValue(0);

        Instruction instr = decoder.decodeInstruction();
        // Verifica se o opcode é o esperado (0x00 para LDA)
        assertEquals(0x00, instr.getOpcode());
        // Verifica o tamanho em bytes (3)
        assertEquals(3, instr.getSizeInBytes());
        // E o effectiveAddress deve ser calculado (neste caso, 9)
        assertEquals(9, instr.getEffectiveAddress());
    }

    @Test
    public void testDecodeFormat2Instruction() {
        // Para testar uma instrução em formato 2 (por exemplo, CLEAR ou ADDR)
        // Vamos simular o CLEAR, definido com opcode 0x04.
        byte[] instructionBytes = {0x04, 0x21, 0x00};
        decoder.setFetchedBytes(instructionBytes);
        registers.getRegister("PC").setValue(0);

        Instruction instr = decoder.decodeInstruction();
        // Em formato 2, o opcode deve ser o valor inteiro do primeiro byte.
        assertEquals(0x04, instr.getOpcode());
        // Os operandos devem estar corretamente extraídos
        int[] operands = instr.getOperands();
        assertEquals(2, operands[0]);
        assertEquals(1, operands[1]);
    }
}
