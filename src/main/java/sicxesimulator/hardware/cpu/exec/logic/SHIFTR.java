package sicxesimulator.hardware.cpu.exec.logic;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;
import sicxesimulator.hardware.cpu.core.Register;
import sicxesimulator.utils.Mapper;

public final class SHIFTR extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int[] ops = c.operands();
        Register r = Mapper.getRegisterByNumber(ops[0], c.regs());
        int count  = ops[1];
        int res    = r.getIntValue() >>> count;
        r.setValue(res);
        updateCC(c, res);
        return String.format("SHIFTR: R%d >> %d = %06X", ops[0], count, res);
    }
}