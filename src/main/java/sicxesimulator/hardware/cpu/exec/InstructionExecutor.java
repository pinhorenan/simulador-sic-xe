package sicxesimulator.hardware.cpu.exec;

import sicxesimulator.hardware.cpu.model.ExecutionContext;

/**
 * Contrato funcional de um executor de instrução SIC/XE.
 *
 * <p>Cada implementação recebe um {@link ExecutionContext} imutável e
 * devolve uma mensagem de log (usada pela GUI ou testes).</p>
 *
 * @author Renan
 * @since 1.0.0
 */
@FunctionalInterface
public interface InstructionExecutor {

    /**
     * Executa a instrução encapsulada em {@code ctx}.
     *
     * @return texto de log descrevendo a execução
     */
    String execute(ExecutionContext ctx);
}
