package sicxesimulator.utils;

import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.machine.cpu.Register;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ValueFormatter {

    /**
     * Formata um valor de byte (0-255) conforme o formato especificado.
     *
     * @param value  O valor a ser formatado (0-255).
     * @param format O formato desejado ("DEC", "OCT", "HEX").
     * @return O valor formatado como String.
     * @throws IllegalArgumentException Se o valor estiver fora do intervalo 0-255.
     */
    public static String formatByte(int value, String format) {
        // Valida o valor do byte
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("O valor do byte deve estar entre 0 e 255.");
        }

        // Formata o valor conforme o formato especificado
        return switch (format.toUpperCase()) {
            case "DEC" -> Integer.toString(value); // Decimal
            case "OCT" -> String.format("%03o", value); // Octal com 3 dígitos
            case "HEX" -> String.format("%02X", value); // Hexadecimal com 2 dígitos
            default -> throw new IllegalArgumentException("Formato inválido: " + format);
        };
    }

    /**
     * Formata um endereço conforme o formato especificado.
     *
     * @param byteAddress O endereço a ser formatado.
     * @param format  O formato desejado ("DEC", "OCT", "HEX").
     * @return O endereço formatado como String.
     * @throws IllegalArgumentException Se o endereço for negativo.
     */
    public static String formatAddress(int byteAddress, String format) {
        // Formatação do endereço com base no formato
        if ("HEX".equals(format)) {
            return String.format("%04X", byteAddress);
        } else if ("DEC".equals(format)) {
            return String.format("%d", byteAddress);
        } else {
            return String.format("%04X", byteAddress);  // Padrão HEX
        }
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

    public static String processAddresses(String message) {
        Matcher matcher = Pattern.compile("0x([0-9A-Fa-f]{1,8})").matcher(message);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            int wordAddress = Integer.parseInt(matcher.group(1), 16);
            int byteAddress = wordAddress * 3;
            matcher.appendReplacement(sb, "0x" + Integer.toHexString(byteAddress).toUpperCase());
        }
        return matcher.appendTail(sb).toString();
    }

    public static String formatRegisterValue(Register reg) {
        String regName = reg.getName().toUpperCase();
        if ("PC".equals(regName)) {
            return String.format("%06X", reg.getIntValue());
        } else if ("F".equals(regName)) {
            return String.format("%012X", reg.getLongValue());
        }
        return String.format("%06X", reg.getIntValue());
    }

    /**
     * Formata o código objeto para uma string legível.
     */
    public static String formatObjectCode(ObjectFile objectFile) {
        StringBuilder formattedCode = new StringBuilder();
        for (byte b : objectFile.getObjectCode()) {
            formattedCode.append(String.format("%02X ", b & 0xFF));
        }
        return formattedCode.toString().trim();
    }
}