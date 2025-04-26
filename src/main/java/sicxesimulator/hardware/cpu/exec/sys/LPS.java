package sicxesimulator.hardware.cpu.exec.sys;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

/**
 * LPS – Load Processor Status: armazena SW em memória.
 */
public final class LPS extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int ea = c.effectiveAddress();
        int sw = c.regs().getRegister("SW").getIntValue();
        c.mem().writeByte(ea, sw);
        return String.format("LPS: Mem[%06X] ← SW(%d)", ea, sw);
    }
}