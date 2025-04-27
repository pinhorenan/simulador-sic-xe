package sicxesimulator.hardware.cpu.exec.store;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;

public final class STCH extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int ea        = c.effectiveAddress();
        int byteValue = c.regs().getRegister("A").getIntValue() & 0xFF;
        c.mem().writeByte(ea, byteValue);
        return String.format("STCH: Mem[%06X] ← %02X", ea, byteValue);
    }
}