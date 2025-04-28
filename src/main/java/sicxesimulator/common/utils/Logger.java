package sicxesimulator.common.utils;

import sicxesimulator.hardware.cpu.register.Register;
import sicxesimulator.hardware.cpu.register.RegisterSet;
import sicxesimulator.hardware.memory.Memory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

/**
 * <h2>Logger central do simulador SIC/XE</h2>
 *
 * <p>Grava saídas de depuração em &nbsp;<code>logging/detailed.log</code>&nbsp;
 * (rotação - 10 MB por arquivo / 10 arquivos).</p>
 *
 * <p>Principais facilidades:</p>
 * <ul>
 *   <li>{@link #info(String)} mensagens informativas rápidas;</li>
 *   <li>{@link #error(String, Throwable)} erros/exceções;</li>
 *   <li>{@link #logMachineState(Memory, RegisterSet, String, Map, String, List, String)}
 *        captura completa do estado da máquina (para depuração “post-mortem”).</li>
 * </ul>
 *
 * <p>A classe é <em>final</em> e não-instanciável.</p>
 */
@SuppressWarnings("unused")
public final class Logger {

    /* ====================================================================== */
    /*  Configuração do java.util.logging                                     */
    /* ====================================================================== */

    private static final int FILE_LIMIT  = 10 * 1024 * 1024;    // 10 MB
    private static final int FILE_COUNT  = 10;                  // rotação

    /** Logger sub-sistema (não polui o root). */
    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger("SICXE.Detailed");

    static {
        try {
            /* diretório de logs */
            File dir = new File("logging");
            if (!dir.exists() && !dir.mkdirs()) {
                System.err.println("Falha ao criar diretório de logs: " + dir.getAbsolutePath());
            }

            /* FileHandler com rotação fixa */
            FileHandler fh = new FileHandler("logging/detailed.log",
                    FILE_LIMIT, FILE_COUNT, /*append=*/false);
            fh.setFormatter(new SimpleFormatter());
            LOG.addHandler(fh);
            LOG.setLevel(Level.ALL);

            /* remove console-handler default */
            java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
            for (Handler h : root.getHandlers()) {
                if (h instanceof ConsoleHandler) root.removeHandler(h);
            }
        } catch (IOException ex) {
            System.err.println("Erro ao inicializar logger: " + ex.getMessage());
        }
    }

    /* impede instanciação */
    private Logger() { throw new AssertionError(); }

    /* ====================================================================== */
    /*  API pública curta                                                     */
    /* ====================================================================== */

    /** Mensagem simples <code>INFO</code>. */
    public static void info(String msg) {
        LOG.info(msg);
    }

    /** Erro/severe com exceção opcional. */
    public static void error(String msg, Throwable t) {
        LOG.log(Level.SEVERE, msg, t);
    }

    /* ====================================================================== */
    /*  Dump completo do estado da máquina                                    */
    /* ====================================================================== */

    /**
     * Captura detalhada do estado da máquina.
     *
     * <p>Inclui memória (apenas palavras&nbsp; ≠&nbsp; 0), registradores, object-code,
     * tabela de símbolos, fonte original e histórico de execução.</p>
     *
     * @param memory          instância da memória
     * @param registers       conjunto de registradores
     * @param objectCodeText  texto do ficheiro <code>.obj</code> carregado
     * @param symbolTable     mapa símbolo&nbsp;→&nbsp; endereço (já realocado)
     * @param sourceCode      fonte RAW concatenado
     * @param executionOutput lista/trace de mensagens da execução
     * @param context         rótulo livre indicando o momento da captura
     */
    public static void logMachineState(Memory memory,
                                       RegisterSet registers,
                                       String objectCodeText,
                                       Map<String, Integer> symbolTable,
                                       String sourceCode,
                                       List<String> executionOutput,
                                       String context) {

        StringBuilder sb = new StringBuilder(12_000);

        sb.append("═════════════════════════════════════════════════════════════\n")
                .append("          DUMP COMPLETO DA MÁQUINA  @ ")
                .append(LocalDateTime.now())
                .append("\n          Contexto: ").append(context).append('\n')
                .append("═════════════════════════════════════════════════════════════\n\n");

        /* -------------------------------------------------- */
        /* Memória                                            */
        /* -------------------------------------------------- */
        sb.append("Memória (palavras de 3 bytes ≠ 0)\n")
                .append(String.format("%-8s | %-8s%n", "End.", "Valor"))
                .append("-------------------------------\n");

        for (int word = 0; word < memory.getSize() / 3; word++) {
            int val = Convert.bytesToInt(memory.readWord(word));
            if (val != 0) sb.append(String.format("%06X   | %06X%n", word * 3, val));
        }
        sb.append('\n');

        /* -------------------------------------------------- */
        /* Registradores                                      */
        /* -------------------------------------------------- */
        sb.append("Registradores\n")
                .append(String.format("%-3s | %-12s%n", "Reg", "Valor"))
                .append("----------------------\n");
        for (Register r : registers.getAllRegisters()) {
            String val = "F".equals(r.getName())
                    ? String.format("%012X", r.getLongValue())
                    : String.format("%06X" , r.getIntValue());
            sb.append(String.format("%-3s | %s%n", r.getName(), val));
        }
        sb.append('\n');

        /* -------------------------------------------------- */
        /* Object-code                                        */
        /* -------------------------------------------------- */
        sb.append("--- Object-code ------------------------------------------------\n")
                .append(objectCodeText).append('\n')
                .append("----------------------------------------------------------------\n\n");

        /* -------------------------------------------------- */
        /* Símbolos                                           */
        /* -------------------------------------------------- */
        sb.append("Tabela de símbolos (exportados + locais)\n")
                .append(String.format("%-15s | %-8s%n", "Símbolo", "End."))
                .append("---------------------------------------\n");
        symbolTable.forEach((s, addr) ->
                sb.append(String.format("%-15s | %06X%n", s, addr)));
        sb.append('\n');

        /* -------------------------------------------------- */
        /* Fonte                                              */
        /* -------------------------------------------------- */
        sb.append("--- Source ----------------------------------------------------\n")
                .append(sourceCode).append('\n')
                .append("----------------------------------------------------------------\n\n");

        /* -------------------------------------------------- */
        /* Saída de execução                                  */
        /* -------------------------------------------------- */
        sb.append("--- Execution output -----------------------------------------\n")
                .append(executionOutput).append('\n')
                .append("----------------------------------------------------------------\n");

        sb.append("═════════════════════════════════════════════════════════════\n");

        LOG.severe(sb.toString());
    }
}
