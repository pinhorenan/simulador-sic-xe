package sicxesimulator.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Constants {

    /**
     * Registradores válidos.
     */
    public static final String[] VALID_REGISTERS = {"A", "X", "L", "B", "S", "T", "F", "PC", "SW"};

    public static final Set<String> VALID_MNEMONICS = new HashSet<>(Arrays.asList(
            "START", "END", "BYTE", "WORD", "RESB", "RESW", "BASE", "NOBASE", "EQU", "LTORG",
            "ADD", "ADDR", "AND", "CLEAR", "COMP", "COMPR", "DIV", "DIVR",
            "J", "JEQ", "JGT", "JLT", "JSUB", "LDA", "LDB", "LDCH", "LDL", "LDS",
            "LDT", "LDX", "MUL", "MULR", "OR", "RMO", "RSUB", "SHIFTL", "SHIFTR",
            "STA", "STB", "STCH", "STL", "STS", "STT", "STX", "SUB", "SUBR", "TIX",
            "TIXR"
    ));

    /**
     * Mapeamento de mnemônicos para códigos de operação (opcode).
     */
    public static final Map<String, Integer> OPCODES = Map.ofEntries(
            Map.entry("ADD", 0x18),
            Map.entry("ADDR", 0x90),
            Map.entry("AND", 0x40),
            Map.entry("CLEAR", 0x04),
            Map.entry("LDX", 0x04),
            Map.entry("COMP", 0x28),
            Map.entry("COMPR", 0xA0),
            Map.entry("DIV", 0x24),
            Map.entry("DIVR", 0x9C),
            Map.entry("J", 0x3C),
            Map.entry("JEQ", 0x30),
            Map.entry("JGT", 0x34),
            Map.entry("JLT", 0x38),
            Map.entry("JSUB", 0x48),
            Map.entry("LDA", 0x00),
            Map.entry("LDB", 0x68),
            Map.entry("LDCH", 0x50),
            Map.entry("LDL", 0x08),
            Map.entry("LDS", 0x6C),
            Map.entry("LDT", 0x74),
            Map.entry("MUL", 0x20),
            Map.entry("MULR", 0x98),
            Map.entry("OR", 0x44),
            Map.entry("RMO", 0xAC),
            Map.entry("RSUB", 0x4C),
            Map.entry("SHIFTL", 0xA4),
            Map.entry("SHIFTR", 0xA8),
            Map.entry("STA", 0x0C),
            Map.entry("STB", 0x78),
            Map.entry("STCH", 0x54),
            Map.entry("STL", 0x14),
            Map.entry("STS", 0x7C),
            Map.entry("STT", 0x84),
            Map.entry("STX", 0x10),
            Map.entry("SUB", 0x1C),
            Map.entry("SUBR", 0x94),
            Map.entry("TIX", 0x2C),
            Map.entry("TIXR", 0xB8)
    );

    public static final String SAVE_DIR = "src/main/resources/saved";

    public static final String SAMPLES_DIR = "src/main/resources/examples";

}
