package sicxesimulator.hardware.cpu.core;

/**
 * Contrato de qualquer executor de instrução.
 */
@FunctionalInterface
public interface InstructionExecutor {
    /**
     * Executa a instrução e devolve um log textual.
     */
    String execute(ExecutionContext ctx);
}
