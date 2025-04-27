package sicxesimulator.hardware.cpu.exec.arith;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.Register;
import sicxesimulator.common.utils.Mapper;

public final class ADDR extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int[] ops = c.operands();
        Register r1 = Mapper.getRegisterByNumber(ops[0], c.regs());
        Register r2 = Mapper.getRegisterByNumber(ops[1], c.regs());
        int res = r1.getIntValue() + r2.getIntValue();
        r2.setValue(res);
        updateCC(c, res);
        return String.format("ADDR: %s + %s => %06X", r1.getName(), r2.getName(), res);
    }
}
