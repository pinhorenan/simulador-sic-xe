package sicxesimulator.assembler;

import org.junit.jupiter.api.Test;
import sicxesimulator.models.ObjectFile;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AssemblerTest {

    @Test
    public void testAssemblerBasicProgram() {
        Assembler assembler = new Assembler();

        // Código de teste para um programa SIC/XE mínimo
        List<String> sourceCode = Arrays.asList(
                "COPY START 1000",
                "FIRST LDA #45",      // Imediato (n=0, i=1)
                "SECOND STA 1030",    // Direto (n=1, i=1)
                "THIRD LDX #10",      // Imediato (n=0, i=1)
                "END COPY"
        );

        ObjectFile objectFile = assembler.assemble(sourceCode);

        assertNotNull(objectFile);
        assertEquals(0x1000, objectFile.getStartAddress());

        // Verifica que o código objeto foi gerado corretamente
        byte[] objectCode = objectFile.getMachineCode();
        assertNotNull(objectCode);
        assertTrue(objectCode.length > 0);

        // Verifica se o PC foi atualizado corretamente
        // (esperado: último endereço + tamanho da instrução)
        int expectedPC = 0x1000 + 3 + 3 + 3;
        assertEquals(expectedPC, objectFile.getSymbolTable().getAddress("THIRD"));
    }

    @Test
    public void testImmediateValueParsing() {
        Assembler assembler = new Assembler();

        List<String> sourceCode = Arrays.asList(
                "COPY START 4096",
                "FIRST LDA #10",  // Teste com valor imediato
                "END COPY"
        );

        ObjectFile objectFile = assembler.assemble(sourceCode);
        assertNotNull(objectFile);
    }
}
