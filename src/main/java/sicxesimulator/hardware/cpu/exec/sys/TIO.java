package sicxesimulator.hardware.cpu.exec.sys;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

/**
 * TIO – Test I/O: alias de TD.
 */
public final class TIO extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        // reutiliza lógica de TD
        c.regs().getRegister("SW").setValue(
                c.regs().getRegister("SW").getIntValue()==0 ? 0 : 1
        );
        return "TIO: Teste de I/O (mesmo de TD)";
    }
}