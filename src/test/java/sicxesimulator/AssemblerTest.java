package sicxesimulator;

import org.junit.jupiter.api.Test;
import sicxesimulator.assembler.Assembler;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class AssemblerTest {

    @Test
    public void testAssembleValidInstruction() {
        Assembler assembler = new Assembler();

        // Exemplo de código de assembly
        String assemblyCode = "LDA #45";  // Carregar 45 no acumulador
        String expectedMachineCode = "00A545"; // Supondo que a tradução para código de máquina seja esse

        // Chama o método que traduz o código
        //String machineCode = assembler.assemble(assemblyCode);

        // Verifica se o código de máquina está correto
        //assertEquals(expectedMachineCode, machineCode);
    }

    @Test
    public void testAssembleInvalidInstruction() {
        Assembler assembler = new Assembler();

        // Exemplo de código de assembly inválido
        String invalidAssemblyCode = "INVALID CODE";

        // Espera que uma exceção seja lançada
        assertThrows(IllegalArgumentException.class, () -> {
            assembler.assemble(Collections.singletonList(invalidAssemblyCode));
        });
    }
}
