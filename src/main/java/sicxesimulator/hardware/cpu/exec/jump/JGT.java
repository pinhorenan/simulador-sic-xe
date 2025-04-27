package sicxesimulator.hardware.cpu.exec.jump;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

public final class JGT extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int sw = c.regs().getRegister("SW").getIntValue();
        int ea = c.effectiveAddress();
        if (sw == 2) {
            c.regs().getRegister("PC").setValue(ea);
            return String.format("JGT: PC ← %06X (Jump realizado)", ea);
        } else {
            return "JGT: Condição não satisfeita (sem jump)";
        }
    }
}