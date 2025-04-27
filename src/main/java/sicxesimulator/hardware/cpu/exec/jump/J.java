package sicxesimulator.hardware.cpu.exec.jump;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

public final class J extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int ea = c.effectiveAddress();
        c.regs().getRegister("PC").setValue(ea);
        return String.format("J: PC ← %06X", ea);
    }
}