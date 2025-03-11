package sicxesimulator.utils;

import java.io.IOException;
import java.util.logging.*;

public class SimulatorLogger {
    // Configurações para rotação: limite de 10MB por arquivo e 10 arquivos rotacionados
    private static final int LIMIT = 10 * 1024 * 1024; // 10MB
    private static final int COUNT = 10;

    // Loggers separados para cada categoria
    private static final Logger assemblyLogger = Logger.getLogger("SimulatorLogger.Assembly");
    private static final Logger machineLogger = Logger.getLogger("SimulatorLogger.Machine");
    private static final Logger executionLogger = Logger.getLogger("SimulatorLogger.Execution");
    private static final Logger errorLogger = Logger.getLogger("SimulatorLogger.Error");

    static {
        try {
            // Configuração do logger para Assembly
            FileHandler assemblyHandler = new FileHandler("logging/assembly.log", LIMIT, COUNT, true);
            assemblyHandler.setFormatter(new SimpleFormatter());
            assemblyLogger.addHandler(assemblyHandler);
            assemblyLogger.setLevel(Level.ALL);

            // Configuração do logger para Machine Code
            FileHandler machineHandler = new FileHandler("logging/machine.log", LIMIT, COUNT, true);
            machineHandler.setFormatter(new SimpleFormatter());
            machineLogger.addHandler(machineHandler);
            machineLogger.setLevel(Level.ALL);

            // Configuração do logger para Execution
            FileHandler executionHandler = new FileHandler("logging/execution.log", LIMIT, COUNT, true);
            executionHandler.setFormatter(new SimpleFormatter());
            executionLogger.addHandler(executionHandler);
            executionLogger.setLevel(Level.ALL);

            // Configuração do logger para Errors
            FileHandler errorHandler = new FileHandler("logging/error.log", LIMIT, COUNT, true);
            errorHandler.setFormatter(new SimpleFormatter());
            errorLogger.addHandler(errorHandler);
            errorLogger.setLevel(Level.ALL);

            // Opcional: remover o ConsoleHandler padrão para evitar duplicação no console
            Logger rootLogger = Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }
        } catch (IOException e) {
            System.err.println("Não foi possível inicializar os FileHandlers: " + e.getMessage());
        }
    }

    public static void logAssemblyCode(String assemblyCode) {
        assemblyLogger.info("Código Assembly Montado:\n" + assemblyCode);
    }

    public static void logMachineCode(String machineCode) {
        machineLogger.info("Código Objeto Carregado:\n" + machineCode);
    }

    public static void logExecution(String executionLog) {
        executionLogger.info("Log de Execução:\n" + executionLog);
    }

    public static void logError(String message, Throwable throwable) {
        errorLogger.log(Level.SEVERE, message, throwable);
    }
}
