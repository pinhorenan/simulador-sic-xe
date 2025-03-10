package sicxesimulator.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
/**
 * Utility class para operações comuns de leitura e escrita de arquivos.
 */
public abstract class FileUtils {

    /**
     * Lê o conteúdo inteiro de um arquivo como uma única string.
     *
     * @param filePath O caminho do arquivo.
     * @return O conteúdo do arquivo como string.
     * @throws IOException Se ocorrer um erro de I/O.
     */
    public static String readFile(String filePath) throws IOException {
        return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
    }

    /**
     * Escreve o conteúdo fornecido em um arquivo, usando UTF-8.
     * Se o arquivo não existir, ele será criado.
     *
     * @param filePath O caminho do arquivo.
     * @param content  O conteúdo a ser escrito.
     * @throws IOException Se ocorrer um erro de I/O.
     */
    public static void writeFile(String filePath, String content) throws IOException {
        Files.writeString(Path.of(filePath), content, StandardCharsets.UTF_8);
    }
}