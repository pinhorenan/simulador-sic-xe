package sicxesimulator.hardware.cpu.exec.store;

import org.junit.jupiter.api.Test;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.register.RegisterSet;
import sicxesimulator.hardware.memory.Memory;

import static org.junit.jupiter.api.Assertions.*;

class StoreWordExecutorsTest {

    private static ExecutionContext ctxWord(int ea, RegisterSet rs, Memory mem) {
        return new ExecutionContext(new int[0], false, ea, rs, mem);
    }

    private static int bytesToInt(byte[] w) {
        return ((w[0] & 0xFF) << 16)
                | ((w[1] & 0xFF) << 8)
                | (w[2] & 0xFF);
    }

    @Test
    void sta_writesAValueAsWord() {
        Memory mem = new Memory(9);
        RegisterSet rs = new RegisterSet();
        rs.getRegister("A").setValue(0xABCDEF);

        // endereço 3 → wordIndex = 1
        new STA().execute(ctxWord(3, rs, mem));

        byte[] w = mem.readWord(1);
        assertEquals(0xABCDEF, bytesToInt(w));
    }

    @Test
    void stb_writesBValueAsWord() {
        Memory mem = new Memory(9);
        RegisterSet rs = new RegisterSet();
        rs.getRegister("B").setValue(0x123456);

        new STB().execute(ctxWord(6, rs, mem));  // 6/3=2

        assertEquals(0x123456, bytesToInt(mem.readWord(2)));
    }

    @Test
    void stl_writesLValueAsWord() {
        Memory mem = new Memory(6);
        RegisterSet rs = new RegisterSet();
        rs.getRegister("L").setValue(0x000123);

        new STL().execute(ctxWord(0, rs, mem));  // 0/3=0

        assertEquals(0x000123, bytesToInt(mem.readWord(0)));
    }

    @Test
    void sts_writesSValueAsWord() {
        Memory mem = new Memory(12);
        RegisterSet rs = new RegisterSet();
        rs.getRegister("S").setValue(0x00FF00);

        new STS().execute(ctxWord(9, rs, mem));  // 9/3=3

        assertEquals(0x00FF00, bytesToInt(mem.readWord(3)));
    }

    @Test
    void stt_writesTValueAsWord() {
        Memory mem = new Memory(6);
        RegisterSet rs = new RegisterSet();
        rs.getRegister("T").setValue(0x0000AA);

        new STT().execute(ctxWord(3, rs, mem));  // 3/3=1

        assertEquals(0x0000AA, bytesToInt(mem.readWord(1)));
    }

    @Test
    void stx_writesXValueAsWord() {
        Memory mem = new Memory(6);
        RegisterSet rs = new RegisterSet();
        rs.getRegister("X").setValue(0x0F0F0F);

        new STX().execute(ctxWord(0, rs, mem));

        assertEquals(0x0F0F0F, bytesToInt(mem.readWord(0)));
    }
}
