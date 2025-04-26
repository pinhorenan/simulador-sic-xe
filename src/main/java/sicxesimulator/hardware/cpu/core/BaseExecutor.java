package sicxesimulator.hardware.cpu.core;

import sicxesimulator.utils.Convert;

public abstract class BaseExecutor implements InstructionExecutor {

    /* ===== Helpers reutilizados da antiga ExecutionUnit ===== */

    protected int toWordAddress(int address) {
        if (address % 3 != 0)
            throw new IllegalArgumentException("Endereço não alinhado: " + address);
        return address / 3;
    }

    protected int getValueOrImmediate(ExecutionContext c) {
        int[] op = c.operands();
        int n = op[5], i = op[6];
        if (n == 0 && i == 1) {        // imediato
            return c.effectiveAddress();
        }
        if (n == 1 && i == 0) {        // indireto
            int addr = Convert.bytesToInt(c.mem().readWord(toWordAddress(c.effectiveAddress())));
            return Convert.bytesToInt(c.mem().readWord(toWordAddress(addr)));
        }
        // direto
        return Convert.bytesToInt(c.mem().readWord(toWordAddress(c.effectiveAddress())));
    }

    protected void updateCC(ExecutionContext c, int value) {
        int cc = (value == 0) ? 0 : (value < 0 ? 1 : 2);
        c.regs().getRegister("SW").setValue(cc);
    }

    protected String ccDesc(ExecutionContext c) {
        return switch (c.regs().getRegister("SW").getIntValue()) {
            case 0 -> "Igual";
            case 1 -> "Menor";
            case 2 -> "Maior";
            default -> "Desconhecido";
        };
    }
}
