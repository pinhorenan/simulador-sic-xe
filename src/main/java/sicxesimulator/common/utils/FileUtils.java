package sicxesimulator.common.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Operações básicas de I/O (UTF-8).
 */
public final class FileUtils {

    /* ------------------------------------------------------------------ */

    public static void ensureDirectoryExists(String dir) throws IOException {
        Files.createDirectories(Path.of(dir));
    }

    public static String readFile(String file) throws IOException {
        return Files.readString(Path.of(file), StandardCharsets.UTF_8);
    }

    public static void writeFile(String file, String content) throws IOException {
        Files.writeString(Path.of(file), content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void writeFileInDir(String dir, String fileName, String content) throws IOException {
        ensureDirectoryExists(dir);
        writeFile(Path.of(dir, fileName).toString(), content);
    }
}