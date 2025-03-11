package sicxesimulator.macroprocessor;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class MacroProcessorTest {

    private String normalizeLine(String line) {
        // Remove espaços extras: trim remove espaços no início/fim e replaceAll substitui grupos de espaços internos por um único espaço.
        return line.trim().replaceAll("\\s+", " ");
    }

    @Test
    public void testMacroExpansion() throws IOException {
        // Cria arquivos temporários para entrada e saída
        Path inputFile = Files.createTempFile("macro_input", ".asm");
        Path outputFile = Files.createTempFile("macro_output", ".asm");

        // Exemplo de código-fonte com definição e chamada de macro.
        // A definição da macro MYMACRO deve ser removida do arquivo de saída,
        // e a chamada "FIRST MYMACRO" deve ser expandida para o corpo da macro.
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

        // Processa o arquivo, expandindo as macros.
        MacroProcessor processor = new MacroProcessor();
        processor.process(inputFile.toString(), outputFile.toString());

        // Lê o conteúdo do arquivo de saída como String e divide em linhas
        String outputContent = Files.readString(outputFile, StandardCharsets.UTF_8);
        String[] outputLines = outputContent.split("\\r?\\n");
        // Remove linhas em branco
        outputLines = Arrays.stream(outputLines)
                .filter(line -> !line.trim().isEmpty())
                .toArray(String[]::new);

        // Loga a saída gerada
        System.out.println("=== Saída Gerada ===");
        for (String line : outputLines) {
            System.out.println(line);
        }
        System.out.println("====================");

        // Define as linhas esperadas após a expansão
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

        // Loga as linhas esperadas
        System.out.println("=== Saída Esperada ===");
        for (String line : expectedLines) {
            System.out.println(line);
        }
        System.out.println("======================");

        // Normaliza ambas as saídas para comparar com espaços internos uniformes
        String[] normalizedOutput = Arrays.stream(outputLines)
                .map(this::normalizeLine)
                .toArray(String[]::new);
        String[] normalizedExpected = Arrays.stream(expectedLines)
                .map(this::normalizeLine)
                .toArray(String[]::new);

        // Exibe as linhas normalizadas para depuração
        System.out.println("=== Saída Normalizada ===");
        Arrays.stream(normalizedOutput).forEach(System.out::println);
        System.out.println("=========================");

        // Verifica se o número de linhas é igual
        assertEquals(normalizedExpected.length, normalizedOutput.length, "Número de linhas diferente.");

        // Compara linha a linha
        for (int i = 0; i < normalizedExpected.length; i++) {
            assertEquals(normalizedExpected[i], normalizedOutput[i], "Diferença na linha " + (i + 1));
        }

        // Remove os arquivos temporários
        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }
}
