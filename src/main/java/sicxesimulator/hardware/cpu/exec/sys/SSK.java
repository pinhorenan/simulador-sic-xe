package sicxesimulator.hardware.cpu.exec.sys;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

/**
 * SSK – Set Storage Key / Proteção: no simulador, sem efeito.
 */
public final class SSK extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        return "SSK: Proteção de memória (não implementado no simulador)";
    }
}