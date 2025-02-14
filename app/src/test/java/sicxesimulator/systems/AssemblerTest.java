package sicxesimulator.systems;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import sicxesimulator.components.operations.Instruction;

class AssemblerTest {

    @Test
    void testAssembleSimpleProgram() {
        // Cria um programa assembly sintético (cada linha representa uma instrução)
        List<String> sourceLines = List.of(
                "START 1000",
                "LDA 1000",
                "ADD 1001",
                "STA 1002",
                "RSUB"
        );
        Assembler assembler = new Assembler();
        List<Instruction> instructions = assembler.assemble(sourceLines);
        // Verifica se o assembler criou instruções (desconsiderando START)
        // Dependendo da implementação, talvez "RSUB" seja a última instrução.
        assertNotNull(instructions, "As instruções não devem ser nulas.");
        assertTrue(instructions.size() >= 3, "Deve ter pelo menos 3 instruções executáveis.");
    }
}
