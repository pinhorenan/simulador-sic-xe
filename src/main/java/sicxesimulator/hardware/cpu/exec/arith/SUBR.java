package sicxesimulator.hardware.cpu.exec.arith;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;
import sicxesimulator.hardware.cpu.core.Register;
import sicxesimulator.common.utils.Mapper;

public final class SUBR extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int[] ops = c.operands();
        Register r1 = Mapper.getRegisterByNumber(ops[0], c.regs());
        Register r2 = Mapper.getRegisterByNumber(ops[1], c.regs());
        int res = r2.getIntValue() - r1.getIntValue();
        r2.setValue(res);
        updateCC(c, res);
        return String.format("SUBR: %s - %s => %06X", r2.getName(), r1.getName(), res);
    }
}
