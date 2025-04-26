package sicxesimulator.hardware.cpu.exec.arith;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;
import sicxesimulator.hardware.cpu.core.Register;

public final class DIV extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        Register A = c.regs().getRegister("A");
        int divisor = getValueOrImmediate(c);
        if (divisor == 0) {
            throw new ArithmeticException("Divisão por zero");
        }
        int res = A.getIntValue() / divisor;
        A.setValue(res);
        updateCC(c, res);
        return String.format("DIV: A=%06X / %06X => %06X", res * divisor, divisor, res);
    }
}
