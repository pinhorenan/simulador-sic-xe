package sicxesimulatorTest.utils;

import org.junit.jupiter.api.Test;
import sicxesimulator.utils.FileUtils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileUtilsTest {

    @Test
    void testWriteAndReadFile() throws IOException {
        String content = "Hello, world!";
        Path tempFile = Files.createTempFile("testFile", ".txt");
        FileUtils.writeFile(tempFile.toString(), content);
        String readContent = FileUtils.readFile(tempFile.toString());
        assertEquals(content, readContent);
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testEnsureDirectoryExists() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        // Remove o diretório para simular que ele não existe
        Files.delete(tempDir);
        FileUtils.ensureDirectoryExists(tempDir.toString());
        assertTrue(Files.exists(tempDir));
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testWriteFileInDir() throws IOException {
        Path tempDir = Files.createTempDirectory("testDir");
        String fileName = "output.txt";
        String content = "File in directory";
        FileUtils.writeFileInDir(tempDir.toString(), fileName, content);
        Path filePath = tempDir.resolve(fileName);
        assertTrue(Files.exists(filePath));
        String readContent = Files.readString(filePath);
        assertEquals(content, readContent);
        Files.deleteIfExists(filePath);
        Files.deleteIfExists(tempDir);
    }
}
