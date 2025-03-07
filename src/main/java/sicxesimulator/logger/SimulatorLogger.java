package sicxesimulator.logger;

import java.io.IOException;
import java.util.logging.*;

public class SimulatorLogger {
    private static final Logger logger = Logger.getLogger("SimulatorLogger");
    private static final String LOG_FILE = "logging/simulator.log";

    static {
        try {
            // Configura um FileHandler para gravar os logs no arquivo
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            // Opcional: remover o ConsoleHandler padrão, se não quiser duplicação no console
            Logger rootLogger = Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }
        } catch (IOException e) {
            System.err.println("Não foi possível inicializar o FileHandler: " + e.getMessage());
        }
    }

    public static void logAssemblyCode(String assemblyCode) {
        logger.info("Código Assembly Montado:\n" + assemblyCode);
    }

    public static void logMachineCode(String machineCode) {
        logger.info("Código Objeto Carregado:\n" + machineCode);
    }

    public static void logExecution(String executionLog) {
        logger.info("Log de Execução:\n" + executionLog);
    }

    // Você pode criar métodos adicionais para warnings, erros, etc.
    public static void logError(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
}
