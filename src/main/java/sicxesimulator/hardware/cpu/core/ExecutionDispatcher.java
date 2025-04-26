package sicxesimulator.hardware.cpu.core;

import sicxesimulator.hardware.cpu.exec.arith.*;
import sicxesimulator.hardware.cpu.exec.jump.*;
import sicxesimulator.hardware.cpu.exec.load.*;
import sicxesimulator.hardware.cpu.exec.logic.*;
import sicxesimulator.hardware.cpu.exec.store.*;
import sicxesimulator.hardware.cpu.exec.sys.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry central de opcodes → executores.
 */
public class ExecutionDispatcher {

    private final Map<Integer, InstructionExecutor> map = new HashMap<>();

    public ExecutionDispatcher() {
        loadBuiltIns();
    }

    /** Roteia o opcode para o executor cadastrado. */
    public String dispatch(int opcode, ExecutionContext ctx) {
        InstructionExecutor exec = map.get(opcode);
        if (exec == null)
            throw new IllegalStateException("Opcode não suportado: " + Integer.toHexString(opcode));
        return exec.execute(ctx);
    }

    /** Registro programático extra (plugins, testes, etc.). */
    public void register(int opcode, InstructionExecutor exec) {
        map.put(opcode, exec);
    }

    /* ------------------------------------------------------------------ */
    /*                     Registro das instruções base                   */
    /* ------------------------------------------------------------------ */
    private void loadBuiltIns() {
        // arith
        map.put(0x18, new ADD());
        map.put(0x1C, new SUB());
        map.put(0x20, new MUL());
        map.put(0x24, new DIV());
        map.put(0x90, new ADDR());
        map.put(0x94, new SUBR());
        map.put(0x98, new MULR());
        map.put(0x9C, new DIVR());

        // jump
        map.put(0x3C, new J());
        map.put(0x30, new JEQ());
        map.put(0x34, new JGT());
        map.put(0x38, new JLT());
        map.put(0x48, new JSUB());
        map.put(0x4C, new RSUB());

        // load
        map.put(0x00, new LDA());
        map.put(0x68, new LDB());
        map.put(0x50, new LDCH());
        map.put(0x08, new LDL());
        map.put(0x6C, new LDS());
        map.put(0x74, new LDT());
        map.put(0x04, new LDX());

        // logic
        map.put(0x40, new AND());
        map.put(0x44, new OR());
        map.put(0x28, new COMP());
        map.put(0xA0, new COMPR());
        map.put(0xB4, new CLEAR());
        map.put(0xA4, new SHIFTL());
        map.put(0xA8, new SHIFTR());
        map.put(0xB8, new TIXR());

        // store
        map.put(0x0C, new STA());
        map.put(0x78, new STB());
        map.put(0x54, new STCH());
        map.put(0x14, new STL());
        map.put(0x7C, new STS());
        map.put(0x84, new STT());
        map.put(0x10, new STX());

        // sys
        map.put(0xF4, new LPS());
        map.put(0xF8, new SIO());
        map.put(0xEC, new SSK());
        map.put(0xD8, new RD());
        map.put(0xE0, new TD());
        map.put(0xE4, new TIO());
        map.put(0xDC, new WD());
        map.put(0xB0, new SVC());

    }
}
