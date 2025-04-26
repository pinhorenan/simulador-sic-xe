package sicxesimulator.hardware.cpu.exec.store;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;
import sicxesimulator.utils.Convert;

public final class STA extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int ea    = c.effectiveAddress();
        int value = c.regs().getRegister("A").getIntValue();
        c.mem().writeWord(toWordAddress(ea), Convert.intTo3Bytes(value));
        return String.format("STA: Mem[%06X] ← %06X", ea, value);
    }
}