package sicxesimulator.hardware.cpu.exec.jump;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

public final class JSUB extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int returnAddress = c.regs().getRegister("PC").getIntValue();
        int ea = c.effectiveAddress();
        c.regs().getRegister("L").setValue(returnAddress);
        c.regs().getRegister("PC").setValue(ea);
        return String.format("JSUB: L ← %06X, PC ← %06X", ea, returnAddress);
    }
}