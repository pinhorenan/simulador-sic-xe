package sicxesimulatorTest.software.macroprocessor;

import org.junit.jupiter.api.Test;
import sicxesimulator.software.macroprocessor.MacroProcessor;
import sicxesimulator.common.utils.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MacroProcessorTest {

    private String normalizeLine(String line) {
        return line.trim().replaceAll("\\s+", " ");
    }

    @Test
    public void testMacroExpansion() throws IOException {
        // Cria arquivo temporário de entrada
        Path inputFile = Files.createTempFile("macro_input", ".asm");

        // Gera nome simples para o arquivo de saída (apenas o nome, sem path completo)
        String outputFileName = "macro_output_" + System.nanoTime() + ".asm";

        // Código de entrada com macro
        String inputCode = """
            MYMACRO MACRO
            LDA   FIVE
            ADD   FOUR
            MEND
            SAMP01 START 0
            FIRST  MYMACRO
                   RSUB
            FIVE   WORD  5
            FOUR   WORD  4
            RESULT RESW  1
            END    FIRST
            """;

        Files.writeString(inputFile, inputCode, StandardCharsets.UTF_8);

        // Processa o arquivo (MacroProcessor grava em Constants.TEMP_DIR/outputFileName)
        MacroProcessor processor = new MacroProcessor();
        processor.process(inputFile.toString(), outputFileName);

        // Caminho completo para leitura do resultado
        Path outputPath = Paths.get(Constants.TEMP_DIR, outputFileName);
        String outputContent = Files.readString(outputPath, StandardCharsets.UTF_8);
        String[] outputLines = outputContent.split("\\r?\\n");

        // Remove linhas em branco
        outputLines = Arrays.stream(outputLines)
                .filter(line -> !line.trim().isEmpty())
                .toArray(String[]::new);

        System.out.println("=== Saída Gerada ===");
        for (String line : outputLines) {
            System.out.println(line);
        }
        System.out.println("====================");

        String[] expectedLines = new String[] {
                "SAMP01 START 0",
                "FIRST  LDA   FIVE",
                "ADD   FOUR",
                "RSUB",
                "FIVE   WORD  5",
                "FOUR   WORD  4",
                "RESULT RESW  1",
                "END    FIRST"
        };

        System.out.println("=== Saída Esperada ===");
        for (String line : expectedLines) {
            System.out.println(line);
        }
        System.out.println("======================");

        String[] normalizedOutput = Arrays.stream(outputLines)
                .map(this::normalizeLine)
                .toArray(String[]::new);

        String[] normalizedExpected = Arrays.stream(expectedLines)
                .map(this::normalizeLine)
                .toArray(String[]::new);

        System.out.println("=== Saída Normalizada ===");
        Arrays.stream(normalizedOutput).forEach(System.out::println);
        System.out.println("=========================");

        assertEquals(normalizedExpected.length, normalizedOutput.length, "Número de linhas diferente.");

        for (int i = 0; i < normalizedExpected.length; i++) {
            assertEquals(normalizedExpected[i], normalizedOutput[i], "Diferença na linha " + (i + 1));
        }

        // Limpeza (opcional)
        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputPath);
    }
}
