package sicxesimulator.hardware.cpu.exec.load;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

public final class LDL extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int value = getValueOrImmediate(c);
        c.regs().getRegister("L").setValue(value);
        return String.format("LDL: L ← %06X", value);
    }
}