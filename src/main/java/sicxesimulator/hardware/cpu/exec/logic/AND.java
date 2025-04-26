package sicxesimulator.hardware.cpu.exec.logic;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;
import sicxesimulator.hardware.cpu.core.Register;

public final class AND extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        Register A = c.regs().getRegister("A");
        int before = A.getIntValue();
        int op     = getValueOrImmediate(c);
        int res    = before & op;
        A.setValue(res);
        updateCC(c, res);
        return String.format("AND: A=%06X & %06X => %06X", before, op, res);
    }
}