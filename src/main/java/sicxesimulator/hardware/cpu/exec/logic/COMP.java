package sicxesimulator.hardware.cpu.exec.logic;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.Register;

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