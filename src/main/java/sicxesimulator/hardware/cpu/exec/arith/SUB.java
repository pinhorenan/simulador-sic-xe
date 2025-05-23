package sicxesimulator.hardware.cpu.exec.arith;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.Register;

public final class SUB extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        Register A = c.regs().getRegister("A");
        int before = A.getIntValue();
        int op     = getValueOrImmediate(c);
        int res    = before - op;
        A.setValue(res);
        updateCC(c, res);
        return String.format("SUB: A=%06X - %06X => %06X", before, op, res);
    }
}
