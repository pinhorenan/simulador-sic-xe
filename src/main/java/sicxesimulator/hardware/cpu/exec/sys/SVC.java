package sicxesimulator.hardware.cpu.exec.sys;

import sicxesimulator.hardware.cpu.core.BaseExecutor;
import sicxesimulator.hardware.cpu.core.ExecutionContext;

/**
 * SVC – Supervisor Call: sinaliza HALT.
 */
public final class SVC extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        return "SVC: Chamada de sistema (HALT)";  // Contém “HALT” para parar a CPU
    }
}