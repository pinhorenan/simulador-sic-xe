package sicxesimulator.hardware.cpu.exec.load;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

public final class LDB extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int value = getValueOrImmediate(c);
        c.regs().getRegister("B").setValue(value);
        return String.format("LDB: B ← %06X", value);
    }
}