package sicxesimulator.hardware.cpu.exec.load;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

public final class LDX extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int value = getValueOrImmediate(c);
        c.regs().getRegister("X").setValue(value);
        return String.format("LDX: X ← %06X", value);
    }
}