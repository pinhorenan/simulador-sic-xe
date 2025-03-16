package sicxesimulator.assembler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import sicxesimulator.data.ObjectFile;
import sicxesimulator.software.assembler.Assembler;

import java.util.List;

public class AssemblerTest {

    @Test
    public void testAssembleSimpleProgram() {
        // Exemplo de programa assembly simples
        List<String> originalSource = List.of(
                "TEST    START   0",
                "        LDA     ALPHA",
                "        STA     BETA",
                "        END     TEST",
                "ALPHA   WORD    5",
                "BETA    RESW    1"
        );
        // Sem macros, o código expandido é igual ao original

        //noinspection UnnecessaryLocalVariable
        List<String> expandedSource = originalSource;

        Assembler assembler = new Assembler();
        ObjectFile objectFile = assembler.assemble(originalSource, expandedSource);

        // Verifica se o objectFile foi gerado
        assertNotNull(objectFile, "O ObjectFile não deve ser nulo");
        // Verifica se o startAddress é 0 (como definido na diretiva START)
        assertEquals(0, objectFile.getStartAddress(), "Start address deve ser 0");
        // Verifica se o código objeto não está vazio
        assertTrue(objectFile.getObjectCode().length > 0, "O código objeto não deve estar vazio");
        // Verifica se o nome do programa foi definido corretamente
        assertEquals("TEST", objectFile.getProgramName(), "O nome do programa deve ser TEST");
    }
}
