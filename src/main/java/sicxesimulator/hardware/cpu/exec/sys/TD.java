package sicxesimulator.hardware.cpu.exec.sys;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

/**
 * TD – Test Device: sempre pronto (SW=0).
 */
public final class TD extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        c.regs().getRegister("SW").setValue(0);
        return "TD: Dispositivo pronto (SW=0)";
    }
}
