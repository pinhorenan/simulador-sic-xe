package sicxesimulator.hardware.cpu.exec.load;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

public final class LDS extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int value = getValueOrImmediate(c);
        c.regs().getRegister("S").setValue(value);
        return String.format("LDS: S ← %06X", value);
    }
}