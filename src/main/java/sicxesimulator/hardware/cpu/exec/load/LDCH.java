package sicxesimulator.hardware.cpu.exec.load;

import sicxesimulator.hardware.cpu.exec.BaseExecutor;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.Register;

public final class LDCH extends BaseExecutor {
    @Override
    public String execute(ExecutionContext c) {
        int[] op = c.operands();
        int n = op[5], i = op[6];
        int ea = c.effectiveAddress();
        int byteValue = (n == 0 && i == 1)
                ? (ea & 0xFF)
                : c.mem().readByte(ea);

        Register A = c.regs().getRegister("A");
        int orig = A.getIntValue();
        int updated = (orig & 0xFFFF00) | (byteValue & 0xFF);
        A.setValue(updated);

        return String.format("LDCH: A[byte] ← %02X", byteValue);
    }
}