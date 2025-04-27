package sicxesimulator.hardware.cpu.exec.store;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.common.utils.Convert;

public final class STL extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int ea    = c.effectiveAddress();
        int value = c.regs().getRegister("L").getIntValue();
        c.mem().writeWord(toWordAddress(ea), Convert.intTo3Bytes(value));
        return String.format("STL: Mem[%06X] ← %06X", ea, value);
    }
}