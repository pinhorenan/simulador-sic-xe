package sicxesimulator.hardware.cpu.model;

import sicxesimulator.hardware.memory.Memory;
import sicxesimulator.hardware.cpu.exec.InstructionExecutor;
import sicxesimulator.hardware.cpu.register.RegisterSet;

/**
 * Dados imutáveis fornecidos a cada {@link InstructionExecutor}.
 *
 * @param operands         vetor bruto de operandos/flags
 * @param indexed          <b>true</b> se instrução usa indexação (X)
 * @param effectiveAddress endereço efetivo calculado pelo decodificador
 * @param regs             referência ao {@link RegisterSet}
 * @param mem              referência à {@link Memory}
 *
 * @author Renan
 * @since 1.0.0
 */
public record ExecutionContext(
        int[]     operands,
        boolean   indexed,
        int       effectiveAddress,
        RegisterSet regs,
        Memory    mem
) {}
