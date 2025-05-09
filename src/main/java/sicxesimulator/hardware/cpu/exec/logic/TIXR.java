package sicxesimulator.hardware.cpu.exec.logic;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.Register;
import sicxesimulator.common.utils.Mapper;

public final class TIXR extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        Register X = c.regs().getRegister("X");
        X.setValue(X.getIntValue() + 1);
        int regNum = c.operands()[0];
        Register r = Mapper.getRegisterByNumber(regNum, c.regs());
        int cmp = X.getIntValue() - r.getIntValue();
        updateCC(c, cmp);
        return String.format("TIXR: X incrementado para %06X e comparado com %s (%06X) (SW=%s)",
                X.getIntValue(), r.getName(), r.getIntValue(), ccDesc(c));
    }
}