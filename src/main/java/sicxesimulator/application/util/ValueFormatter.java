package sicxesimulator.application.util;

import sicxesimulator.hardware.cpu.Register;
import sicxesimulator.utils.Converter;

public abstract class ValueFormatter {

    /**
     * Formata um endereço conforme o formato especificado.
     *
     * @param address O endereço a ser formatado.
     * @param format  O formato desejado ("DEC", "OCT", "HEX", "BIN").
     * @return O endereço formatado como String.
     * @throws IllegalArgumentException Se o endereço for negativo.
     */
    public static String formatAddress(int address, String format) {
        if (address < 0) {
            throw new IllegalArgumentException("Endereço não pode ser negativo: " + address);
        }
        return switch (format.toUpperCase()) {
            case "HEX" -> String.format("%06X", address);
            case "OCT" -> Integer.toOctalString(address);
            case "BIN" -> Converter.intToBinaryString24(address);
            default -> Integer.toString(address);
        };
    }

    /**
     * Formata um valor (palavra) conforme o formato desejado.
     * Para "HEX" retorna uma string hexadecimal; para "DEC" interpreta os bytes em big-endian;
     * para outros, retorna o valor hexadecimal padrão.
     *
     * @param word   Array de bytes representando a palavra.
     * @param format Formato desejado ("HEX", "DEC", etc.).
     * @return Valor formatado como String.
     */
    public static String formatValue(byte[] word, String format) {
        if ("HEX".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            for (byte b : word) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } else if ("DEC".equalsIgnoreCase(format)) {
            int value = 0;
            // Interpreta os bytes em big-endian (primeiro byte = mais significativo)
            for (byte b : word) {
                value = (value << 8) | (b & 0xFF);
            }
            return String.valueOf(value);
        } else if ("BIN".equalsIgnoreCase(format)) {
            // Caso específico para BIN, utiliza o conversor adequado
            return Converter.bytesToBinaryString(word);
        } else {
            // Padrão: retorna hexadecimal
            return Converter.bytesToHex(word);
        }
    }

    /**
     * Formata o valor do registrador conforme o formato desejado.
     * Se o registrador for "F" (48 bits), utiliza 12 dígitos em HEX; caso contrário, 6 dígitos.
     *
     * @param reg    Registrador.
     * @param format Formato desejado ("HEX", "DEC", "OCT", "BIN").
     * @return Valor formatado como String.
     */
    public static String formatRegisterValue(Register reg, String format) {
        if ("F".equalsIgnoreCase(reg.getName())) {
            long value = reg.getLongValue();
            return switch (format.toUpperCase()) {
                case "HEX" -> String.format("%012X", value);  // 12 dígitos para 48 bits
                case "OCT" -> Long.toOctalString(value);
                case "BIN" -> Converter.longToBinaryString48(value);
                default -> Long.toString(value);
            };
        } else {
            int value = reg.getIntValue();
            return switch (format.toUpperCase()) {
                case "HEX" -> String.format("%06X", value);
                case "OCT" -> Integer.toOctalString(value);
                case "BIN" -> Converter.intToBinaryString24(value);
                default -> Integer.toString(value);
            };
        }
    }
}
