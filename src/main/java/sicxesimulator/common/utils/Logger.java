package sicxesimulator.common.utils;

import sicxesimulator.hardware.Memory;
import sicxesimulator.hardware.cpu.core.RegisterSet;
import sicxesimulator.hardware.cpu.core.Register;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

public class Logger {
    private static final int LIMIT = 10 * 1024 * 1024;
    private static final int COUNT = 10;

    private static final java.util.logging.Logger detailedLogger = java.util.logging.Logger.getLogger("DetailedLogger");

    static {
        try {
            File logDir = new File("logging");
            if (!logDir.exists()) {
                boolean ok = logDir.mkdirs();
                if (!ok) {
                    System.err.println("Unable to create logging directory: " + logDir.getAbsolutePath());
                }
            }

            FileHandler detailedHandler = new FileHandler("logging/detailed.log", LIMIT, COUNT, false);
            detailedHandler.setFormatter(new SimpleFormatter());
            detailedLogger.addHandler(detailedHandler);
            detailedLogger.setLevel(Level.ALL);

            java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao inicializar DetailedLogger: " + e.getMessage());
        }
    }

    public static void logError(String message, Throwable throwable) {
        detailedLogger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Loga o estado detalhado da máquina para auxiliar na depuração.
     * São registrados:
     * - A memória: endereços (a cada 3 bytes) que possuem valor diferente de zero,
     *   no formato "Endereço ⇾ Valor".
     * - O estado de todos os registradores.
     * - O código objeto carregado (texto).
     * - A tabela de símbolos: mapeamento do símbolo para seu endereço.
     * - O código-fonte, conforme o rawSourceCode do ObjectFile.
     * - A saída de execução (histórico).
     *
     * @param memory         Instância da memória.
     * @param registers      Conjunto de registradores (RegisterSet).
     * @param objectCode     Código objeto (texto) carregado na máquina.
     * @param symbolTable    Tabela de símbolos (mapa de símbolo para endereço).
     * @param sourceCode     Código-fonte (raw source) como texto.
     * @param executionOutput Histórico ou saída da execução.
     * @param contextMessage Contexto da captura do log.
     */
    public static void logMachineState(Memory memory, RegisterSet registers, String objectCode,
                                       Map<String, Integer> symbolTable, String sourceCode,
                                       List<String> executionOutput, String contextMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("            ESTADO DETALHADO DA MÁQUINA\n");
        sb.append("            Contexto: ").append(contextMessage).append("\n");
        sb.append("========================================================\n\n");

        sb.append("Memória (endereços com valor != 0):\n");
        sb.append(String.format("%-8s | %-8s\n", "Endereço", "Valor"));
        sb.append("----------------------------------------\n");
        int numWords = memory.getSize() / 3;
        for (int i = 0; i < numWords; i++) {
            byte[] word = memory.readWord(i);
            int value = Convert.bytesToInt(word);
            if (value != 0) {
                int address = i * 3;
                sb.append(String.format("%06X  | %06X\n", address, value));
            }
        }
        sb.append("\n");

        sb.append("Registradores:\n");
        sb.append(String.format("%-4s | %-12s\n", "Reg", "Valor"));
        sb.append("-----------------------------\n");
        for (Register reg : registers.getAllRegisters()) {
            if ("F".equals(reg.getName())) {
                sb.append(String.format("%-4s | %012X\n", reg.getName(), reg.getLongValue()));
            } else {
                sb.append(String.format("%-4s | %06X\n", reg.getName(), reg.getIntValue()));
            }
        }
        sb.append("\n");

        sb.append("Código Objeto Carregado:\n");
        sb.append("--------------------------------------------------------\n");
        sb.append(objectCode).append("\n");
        sb.append("--------------------------------------------------------\n\n");

        sb.append("Tabela de Símbolos:\n");
        sb.append(String.format("%-15s | %-8s\n", "Símbolo", "Endereço"));
        sb.append("----------------------------------------\n");
        for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            sb.append(String.format("%-15s | %06X\n", entry.getKey(), entry.getValue()));
        }
        sb.append("\n");

        sb.append("Código Fonte:\n");
        sb.append("--------------------------------------------------------\n");
        sb.append(sourceCode).append("\n");
        sb.append("--------------------------------------------------------\n\n");

        sb.append("Saída da Execução:\n");
        sb.append("--------------------------------------------------------\n");
        sb.append(executionOutput).append("\n");
        sb.append("--------------------------------------------------------\n");

        sb.append("========================================================\n");

        detailedLogger.severe(sb.toString());
    }
}
