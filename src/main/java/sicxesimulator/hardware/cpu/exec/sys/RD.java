package sicxesimulator.hardware.cpu.exec.sys;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

import java.io.IOException;

/**
 * RD – Read from Device: lê um byte da entrada padrão.
 */
public final class RD extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        try {
            int ch = System.in.read();
            if (ch < 0) ch = 0;
            c.regs().getRegister("A").setValue(ch);
            return String.format("RD: A ← 0x%02X (entrada)", ch);
        } catch (IOException e) {
            throw new RuntimeException("RD: erro de I/O", e);
        }
    }
}