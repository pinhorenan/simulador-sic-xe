package sicxesimulatorTest.hardware.cpu.dummies;

import sicxesimulator.hardware.data.Instruction;
import sicxesimulator.hardware.cpu.InstructionDecoder;
import sicxesimulator.hardware.cpu.ExecutionUnit;
import sicxesimulator.hardware.cpu.RegisterSet;
import sicxesimulator.hardware.Memory;

/**
 * Implementações dummy auxiliares para teste da ControlUnit.
 */
public class Dummies {

    /**
     * Cria uma instância dummy de Instruction representando uma instrução RSUB.
     * - opcode: 0x4C (RSUB)
     * - operands: array vazio
     * - format: 3 (instrução de 3 bytes)
     * - indexed: false
     * - effectiveAddress: 0
     *
     * @return Instância dummy de Instruction.
     */
    public static Instruction createDummyInstructionRSUB() {
        return new Instruction(0x4C, new int[0], 3, false, 0);
    }

    /**
     * Dummy InstructionDecoder que sempre retorna a instrução dummy fornecida.
     */
    public static class DummyInstructionDecoder extends InstructionDecoder {
        private final Instruction dummy;

        public DummyInstructionDecoder(RegisterSet rs, Memory mem, Instruction dummy) {
            super(rs, mem);
            this.dummy = dummy;
        }

        @Override
        public Instruction decodeInstruction() {
            return dummy;
        }
    }

    /**
     * Dummy ExecutionUnit que implementa apenas o executeRSUB.
     */
    public static class DummyExecutionUnit extends ExecutionUnit {
        public DummyExecutionUnit(RegisterSet rs, Memory mem) {
            super(rs, mem);
        }

        @Override
        public String executeRSUB() {
            // Retorna uma mensagem contendo "HALT" para que a ControlUnit marque o processador como halted.
            return "RSUB executed - HALT";
        }
    }

    /**
     * Dummy Memory com implementação mínima para testes.
     * Aqui, assumimos que Memory possui um construtor que recebe um inteiro (por exemplo, o tamanho da memória).
     */
    public static class DummyMemory extends Memory {
        public DummyMemory() {
            super(1024); // Valor default: 1024 bytes (ajuste conforme necessário)
        }

        @Override
        public int getAddressRange() {
            return 1; // valor dummy
        }

        @Override
        public byte[] readWord(int address) {
            // Retorna um "word" dummy com 3 bytes (todos zeros)
            return new byte[]{0, 0, 0};
        }
    }
}
