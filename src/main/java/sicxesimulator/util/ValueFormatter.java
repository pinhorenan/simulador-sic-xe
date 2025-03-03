package sicxesimulator.util;

@SuppressWarnings("EnhancedSwitchMigration")
public class ValueFormatter {
    public static String formatByte(int value, String format) {
        switch (format) {
            case "DEC":
                return Integer.toString(value);
            case "OCT":
                return String.format("%03o", value); // 3 dígitos em octal
            case "HEX":
            default:
                return String.format("%02X", value); // 2 dígitos em HEX
        }
    }

    public static String formatAddress(int address, String format) {
        switch (format) {
            case "DEC":
                return Integer.toString(address);
            case "OCT":
                return Integer.toOctalString(address);
            case "HEX":
            default:
                return String.format("%04X", address); // 4 dígitos em HEX
        }
    }

    public static String formatRegister(int value, String format) {
        switch (format) {
            case "DEC":
                return Integer.toString(value);
            case "OCT":
                return Integer.toOctalString(value);
            case "HEX":
            default:
                return String.format("%06X", value); // 6 dígitos para registradores (24 bits)
        }
    }
}