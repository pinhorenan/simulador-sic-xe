package sicxesimulator.hardware.cpu.core;

import sicxesimulator.hardware.Memory;

/**
 * Contexto imutável entregue a cada executor.
 */
public record ExecutionContext(
        int[] operands,
        boolean indexed,
        int effectiveAddress,
        RegisterSet regs,
        Memory mem
) {}
