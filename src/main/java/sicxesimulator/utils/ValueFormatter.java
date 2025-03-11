package sicxesimulator.utils;

import sicxesimulator.machine.cpu.Register;

public abstract class ValueFormatter {

    /**
     * Formata um endereço conforme o formato especificado.
     *
     * @param address O endereço a ser formatado.
     * @param format  O formato desejado ("DEC", "OCT", "HEX").
     * @return O endereço formatado como String.
     * @throws IllegalArgumentException Se o endereço for negativo.
     */
    public static String formatAddress(int address, String format) {
        // Supondo que o endereço seja tratado similarmente
        return switch (format.toUpperCase()) {
            case "HEX" -> String.format("%06X", address);
            case "OCT" -> Integer.toOctalString(address);
            case "BIN" -> Convert.intToBinaryString24(address);
            default -> Integer.toString(address);
        };
    }

    public static String formatValue(byte[] word, String format) {
        // Formatação do valor com base no formato
        if ("HEX".equals(format)) {
            StringBuilder sb = new StringBuilder();
            for (byte b : word) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } else if ("DEC".equals(format)) {
            int value = 0;
            for (int i = 0; i < word.length; i++) {
                value |= (word[i] & 0xFF) << (8 * i);  // Assumindo little-endian
            }
            return String.valueOf(value);
        } else {
            return Convert.bytesToHex(word);  // Padrão HEX
        }
    }

    public static String formatRegisterValue(Register reg, String format) {
        if ("F".equalsIgnoreCase(reg.getName())) {
            long value = reg.getLongValue();
            return switch (format.toUpperCase()) {
                case "HEX" -> String.format("%012X", value);  // 12 dígitos para 48 bits
                case "OCT" -> Long.toOctalString(value);
                case "BIN" -> Convert.longToBinaryString48(value);
                default -> Long.toString(value);
            };
        } else {
            int value = reg.getIntValue();
            return switch (format.toUpperCase()) {
                case "HEX" -> String.format("%06X", value);
                case "OCT" -> Integer.toOctalString(value);
                case "BIN" -> Convert.intToBinaryString24(value);
                default -> Integer.toString(value);
            };
        }
    }
}