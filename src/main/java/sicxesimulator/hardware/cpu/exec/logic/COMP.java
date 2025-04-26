package sicxesimulator.hardware.cpu.exec.logic;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;
import sicxesimulator.hardware.cpu.core.Register;

public final class COMP extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        Register A = c.regs().getRegister("A");
        int before = A.getIntValue();
        int op     = getValueOrImmediate(c);
        int cmp    = before - op;
        updateCC(c, cmp);
        return String.format("COMP: A (%06X) comparado com %06X (SW=%s)",
                before, op, ccDesc(c));
    }
}