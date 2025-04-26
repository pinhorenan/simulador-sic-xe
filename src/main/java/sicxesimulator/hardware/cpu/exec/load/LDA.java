package sicxesimulator.hardware.cpu.exec.load;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

public final class LDA extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int value = getValueOrImmediate(c);
        c.regs().getRegister("A").setValue(value);
        return String.format("LDA: A ← %06X", value);
    }
}