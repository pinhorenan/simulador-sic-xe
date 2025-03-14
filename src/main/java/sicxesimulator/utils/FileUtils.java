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
     * Garante que o diretório especificado existe, criando-o se necessário.
     * @param directoryPath O caminho do diretório.
     * @throws IOException  Se ocorrer erro na criação.
     */
    public static void ensureDirectoryExists(String directoryPath) throws IOException {
        Files.createDirectories(Path.of(directoryPath));
    }

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

    /**
     * Escreve o conteúdo em um arquivo dentro do diretório especificado.
     *
     * @param directoryPath O caminho do diretório.
     * @param fileName      O nome do arquivo.
     * @param content       O conteúdo a ser escrito.
     * @throws IOException Se ocorrer erro de I/O.
     */
    public static void writeFileInDir(String directoryPath, String fileName, String content) throws IOException {
        ensureDirectoryExists(directoryPath);
        writeFile(directoryPath + "/" + fileName, content);
    }
}