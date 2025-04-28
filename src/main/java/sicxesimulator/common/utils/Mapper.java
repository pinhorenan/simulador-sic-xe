package sicxesimulator.common.utils;

import sicxesimulator.hardware.cpu.register.Register;
import sicxesimulator.hardware.cpu.register.RegisterSet;

/**
 * Mapas de conversão convenientes para o simulador SIC/XE.
 */
public final class Mapper {
    private Mapper() {}

    /* --------------------------------------------------------- */
    /* Mnemonic → opcode                                         */
    /* --------------------------------------------------------- */
    public static int mnemonicToOpcode(String mnemonic) {
        Integer op = Constants.OPCODES.get(mnemonic.toUpperCase());
        if (op == null)
            throw new IllegalArgumentException("Instrução desconhecida: " + mnemonic);
        return op;
    }

    /* --------------------------------------------------------- */
    /* Register name / number                                    */
    /* --------------------------------------------------------- */
    public static int registerNameToNumber(String name) {
        return switch (name.toUpperCase()) {
            case "A"  -> 0;
            case "X"  -> 1;
            case "L"  -> 2;
            case "B"  -> 3;
            case "S"  -> 4;
            case "T"  -> 5;
            case "F"  -> 6;
            case "PC" -> 8;
            case "SW" -> 9;
            default   -> throw new IllegalArgumentException("Registrador inválido: " + name);
        };
    }

    public static Register getRegisterByNumber(int num, RegisterSet set) {
        return switch (num) {
            case 0  -> set.getRegister("A");
            case 1  -> set.getRegister("X");
            case 2  -> set.getRegister("L");
            case 3  -> set.getRegister("B");
            case 4  -> set.getRegister("S");
            case 5  -> set.getRegister("T");
            case 6  -> set.getRegister("F");
            case 8  -> set.getRegister("PC");
            case 9  -> set.getRegister("SW");
            default -> throw new IllegalArgumentException("Número de registrador inválido: " + num);
        };
    }
}
