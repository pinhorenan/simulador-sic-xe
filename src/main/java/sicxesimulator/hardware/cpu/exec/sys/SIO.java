package sicxesimulator.hardware.cpu.exec.sys;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

/**
 * SIO – Start I/O: aqui não há dispositivo, mas sinalizamos o início.
 */
public final class SIO extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        return "SIO: Início de I/O (nenhum dispositivo conectado)";
    }
}