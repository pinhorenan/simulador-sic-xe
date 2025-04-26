package sicxesimulator.hardware.cpu.exec.logic;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;
import sicxesimulator.hardware.cpu.core.Register;
import sicxesimulator.common.utils.Mapper;

public final class COMPR extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int[] ops = c.operands();
        Register r1 = Mapper.getRegisterByNumber(ops[0], c.regs());
        Register r2 = Mapper.getRegisterByNumber(ops[1], c.regs());
        int cmp = r1.getIntValue() - r2.getIntValue();
        updateCC(c, cmp);
        return String.format("COMPR: %s (%06X) comparado com %s (%06X) (SW=%s)",
                r1.getName(), r1.getIntValue(),
                r2.getName(), r2.getIntValue(),
                ccDesc(c));
    }
}