package sicxesimulator.hardware.cpu.exec.logic;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.Register;
import sicxesimulator.common.utils.Mapper;

public final class CLEAR extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int regNum = c.operands()[0];
        Register r = Mapper.getRegisterByNumber(regNum, c.regs());
        r.setValue(0);
        return String.format("CLEAR: %s zerado", r.getName());
    }
}