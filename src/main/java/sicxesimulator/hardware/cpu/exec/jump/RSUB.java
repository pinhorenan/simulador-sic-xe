package sicxesimulator.hardware.cpu.exec.jump;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

public final class RSUB extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int returnAddress = c.regs().getRegister("L").getIntValue();
        if (returnAddress == 0) {
            c.regs().getRegister("PC").setValue(0);
            return "RSUB: Encerrando execução (HALT).";
        } else {
            c.regs().getRegister("PC").setValue(returnAddress);
            return String.format("RSUB: PC ← L (%06X)", returnAddress);
        }
    }
}