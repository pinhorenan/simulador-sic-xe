package sicxesimulator;

import org.junit.jupiter.api.Test;
import sicxesimulator.assembler.Assembler;

import java.util.List;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class AssemblerTest {

    @Test
    public void testAssembleSimpleProgram() {
        List<String> sourceLines = Arrays.asList(
                "COPY START 1000",
                "FIRST  LDA   FIVE",
                "       ADD   FOUR",
                "       STA   RESULT",
                "       RSUB",
                "FIVE   WORD  5",
                "FOUR   WORD  4",
                "RESULT RESW  1"
        );

        Assembler assembler = new Assembler();
        byte[] objectCode = assembler.assemble(sourceLines);

        byte[] expected = new byte[] {
                0x00, 0x00, 0x0C,   // LDA FIVE
                0x18, 0x00, 0x0F,   // ADD FOUR
                0x0C, 0x00, 0x12,   // STA RESULT
                0x4C, 0x00, 0x00,   // RSUB
                0x00, 0x00, 0x05,   // FIVE = 5
                0x00, 0x00, 0x04,   // FOUR = 4
                0x00, 0x00, 0x00    // RESULT RESW 1
        };

        assertArrayEquals(expected, objectCode, "O codigo objeto gerado nao esta correto.");
    }
}
