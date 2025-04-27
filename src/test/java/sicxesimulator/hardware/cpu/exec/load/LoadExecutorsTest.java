package sicxesimulator.hardware.cpu.exec.load;

import org.junit.jupiter.api.Test;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.RegisterSet;
import sicxesimulator.hardware.memory.Memory;

import static org.junit.jupiter.api.Assertions.*;

class LoadExecutorsTest {

    private static ExecutionContext ctxImm(RegisterSet rs, int imm, Memory mem) {
        int[] ops = new int[7];
        ops[5] = 0;  // n = 0 → imediato
        ops[6] = 1;  // i = 1
        return new ExecutionContext(ops, false, imm, rs, mem);
    }

    private static ExecutionContext ctxDir(RegisterSet rs, int wordIndex, Memory mem) {
        int[] ops = new int[7];
        ops[5] = 1;  // n = 1 → direto
        ops[6] = 1;  // i = 1
        int ea = wordIndex * 3;
        return new ExecutionContext(ops, false, ea, rs, mem);
    }

    @Test
    void lda_immAndDir() {
        Memory mem = new Memory(9);
        RegisterSet rs1 = new RegisterSet();
        new LDA().execute(ctxImm(rs1, 0x123456, mem));
        assertEquals(0x123456, rs1.getRegister("A").getIntValue());

        // prepare memória: palavra 0 → 0x000010
        mem.writeWord(0, new byte[]{0x00, 0x00, 0x10});
        RegisterSet rs2 = new RegisterSet();
        new LDA().execute(ctxDir(rs2, 0, mem));
        assertEquals(0x10, rs2.getRegister("A").getIntValue());
    }

    @Test
    void ldb_immAndDir() {
        Memory mem = new Memory(6);
        RegisterSet rs1 = new RegisterSet();
        new LDB().execute(ctxImm(rs1, 0x0000FF, mem));
        assertEquals(0xFF, rs1.getRegister("B").getIntValue());

        mem.writeWord(1, new byte[]{0x00, 0x01, 0x00});  // 0x0100
        RegisterSet rs2 = new RegisterSet();
        new LDB().execute(ctxDir(rs2, 1, mem));
        assertEquals(0x0100, rs2.getRegister("B").getIntValue());
    }

    @Test
    void ldx_immAndDir() {
        Memory mem = new Memory(12);
        RegisterSet rs1 = new RegisterSet();
        new LDX().execute(ctxImm(rs1, 0x000001, mem));
        assertEquals(1, rs1.getRegister("X").getIntValue());

        mem.writeWord(2, new byte[]{0x00, 0x00, 0x02});  // 0x0002
        RegisterSet rs2 = new RegisterSet();
        new LDX().execute(ctxDir(rs2, 2, mem));
        assertEquals(2, rs2.getRegister("X").getIntValue());
    }

    @Test
    void ldl_immAndDir() {
        Memory mem = new Memory(9);
        RegisterSet rs1 = new RegisterSet();
        new LDL().execute(ctxImm(rs1, 0x000100, mem));
        assertEquals(0x100, rs1.getRegister("L").getIntValue());

        mem.writeWord(0, new byte[]{0x00, 0x02, 0x00});  // 0x0200
        RegisterSet rs2 = new RegisterSet();
        new LDL().execute(ctxDir(rs2, 0, mem));
        assertEquals(0x0200, rs2.getRegister("L").getIntValue());
    }

    @Test
    void lds_lAndt_immAndDir() {
        Memory mem = new Memory(12);

        RegisterSet rsS = new RegisterSet();
        new LDS().execute(ctxImm(rsS, 0x000ABC, mem));
        assertEquals(0xABC, rsS.getRegister("S").getIntValue());

        mem.writeWord(3, new byte[]{0x00, 0x03, (byte)0x00});  // 0x0300
        RegisterSet rsS2 = new RegisterSet();
        new LDS().execute(ctxDir(rsS2, 3, mem));
        assertEquals(0x0300, rsS2.getRegister("S").getIntValue());

        RegisterSet rsT = new RegisterSet();
        new LDT().execute(ctxImm(rsT, 0x0000EE, mem));
        assertEquals(0xEE, rsT.getRegister("T").getIntValue());

        mem.writeWord(1, new byte[]{0x00, 0x00, (byte)0x0F});  // 0x000F
        RegisterSet rsT2 = new RegisterSet();
        new LDT().execute(ctxDir(rsT2, 1, mem));
        assertEquals(0x0F, rsT2.getRegister("T").getIntValue());
    }
}
