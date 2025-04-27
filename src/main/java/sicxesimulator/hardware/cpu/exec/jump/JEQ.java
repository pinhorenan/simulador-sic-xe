package sicxesimulator.hardware.cpu.exec.jump;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

public final class JEQ extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int sw = c.regs().getRegister("SW").getIntValue();
        int ea = c.effectiveAddress();
        if (sw == 0) {
            c.regs().getRegister("PC").setValue(ea);
            return String.format("JEQ: PC ← %06X (Jump realizado)", ea);
        } else {
            return "JEQ: Condição não satisfeita (sem jump)";
        }
    }
}