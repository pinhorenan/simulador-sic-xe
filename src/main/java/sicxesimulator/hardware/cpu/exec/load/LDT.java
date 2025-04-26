package sicxesimulator.hardware.cpu.exec.load;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

public final class LDT extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int value = getValueOrImmediate(c);
        c.regs().getRegister("T").setValue(value);
        return String.format("LDT: T ← %06X", value);
    }
}