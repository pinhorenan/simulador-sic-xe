package sicxesimulator.hardware.cpu.exec.logic;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.Register;
import sicxesimulator.common.utils.Mapper;

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