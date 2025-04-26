package sicxesimulator.hardware.cpu.exec.jump;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

public final class JLT extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int sw = c.regs().getRegister("SW").getIntValue();
        int ea = c.effectiveAddress();
        if (sw == 1) {
            c.regs().getRegister("PC").setValue(ea);
            return String.format("JLT: PC ← %06X (Jump realizado)", ea);
        } else {
            return "JLT: Condição não satisfeita (sem jump)";
        }
    }
}