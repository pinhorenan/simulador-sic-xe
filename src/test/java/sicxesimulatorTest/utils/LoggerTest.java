package sicxesimulatorTest.utils;

import org.junit.jupiter.api.Test;
import sicxesimulator.common.utils.Logger;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

class LoggerTest {

    @Test
    void testLogError() {
        Logger.logError("Test error", new Exception("Test exception"));
        // Verifica se o arquivo de log criado existe.
        // Como o FileHandler está configurado com COUNT=10, o primeiro arquivo geralmente é "logging/detailed.log.0"
        File logFile = new File("logging/detailed.log.0");
        assertTrue(logFile.exists(), "O arquivo de log não foi criado.");
    }
}
