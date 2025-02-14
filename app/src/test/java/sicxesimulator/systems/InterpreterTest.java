package sicxesimulator.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import sicxesimulator.components.Machine;
import sicxesimulator.components.operations.Instruction;

class InterpreterTest {

    private Machine machine;
    private Interpreter interpreter;

    @BeforeEach
    void setUp() {
        machine = new Machine();
        interpreter = new Interpreter(machine);
    }

    @Test
    void testInterpreterExecution() {
        // Cria um programa simples: LDA e ADD
        // Vamos supor que a posição de memória 1000 contenha "000005" e 1001 contenha "000003"
        machine.getMemory().write(1000, "000005");
        machine.getMemory().write(1001, "000003");

        Instruction lda = new Instruction("", "LDA", new String[]{"1000"}, 1);
        Instruction add = new Instruction("", "ADD", new String[]{"1001"}, 2);
        interpreter.setInstructions(List.of(lda, add));

        interpreter.runNextInstruction(); // LDA: A = 000005
        assertEquals("000005", machine.getRegister("A").getValue(), "Após LDA, A deve ser 000005");

        interpreter.runNextInstruction(); // ADD: A = 000005 + 000003 = 000008
        assertEquals("000008", machine.getRegister("A").getValue(), "Após ADD, A deve ser 000008");
    }
}
