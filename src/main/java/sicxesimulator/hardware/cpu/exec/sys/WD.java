package sicxesimulator.hardware.cpu.exec.sys;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

/**
 * WD – Write to Device: escreve o byte menos significativo de A na saída padrão.
 */
public final class WD extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int ch = c.regs().getRegister("A").getIntValue() & 0xFF;
        System.out.print((char)ch);
        return String.format("WD: saída ← 0x%02X ('%c')", ch, (char)ch);
    }
}