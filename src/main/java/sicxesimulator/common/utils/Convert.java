package sicxesimulator.common.utils;

/**
 * Classe utilitária para conversões de valores.
 */
public abstract class Convert {
    /**
     * Converte um inteiro para um array de 3 bytes (24 bits) em ordem big-endian.
     * Se o valor ultrapassar o intervalo de 24 bits, ele será truncado.
     *
     * @param value O valor a ser convertido.
     * @return Um array de 3 bytes representando o valor.
     */
    public static byte[] intTo3Bytes(int value) {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) ((value >> 16) & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) (value & 0xFF);
        return bytes;
    }

    /**
     * Converte um valor inteiro para uma string binária de 24 bits,
     * preenchendo com zeros à esquerda se necessário.
     *
     * @param value O valor a converter.
     * @return String binária com 24 dígitos.
     */
    public static String intToBinaryString24(int value) {
        int masked = value & 0xFFFFFF; // Considera apenas 24 bits
        return String.format("%24s", Integer.toBinaryString(masked)).replace(' ', '0');
    }

    /**
     * Converte um valor inteiro para uma string binária de 48 bits, preenchendo com zeros à esquerda se necessário.
     *
     * @param value O valor a converter.
     * @return String binária com 48 dígitos.
     */
    public static String longToBinaryString48(long value) {
        long masked = value & 0xFFFFFFFFFFFFL; // Considera somente 48 bits
        return String.format("%48s", Long.toBinaryString(masked)).replace(' ', '0');
    }

    /**
     * Converte uma string hexadecimal em um array de bytes.
     * Espaços na string são ignorados. Se o tamanho for ímpar, um '0' é adicionado à esquerda.
     *
     * @param hex A string hexadecimal.
     * @return Um array de bytes correspondente.
     * @throws IllegalArgumentException Se a string contiver caracteres inválidos ou for nula.
     */
    public static byte[] hexStringToByteArray(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("A string hexadecimal não pode ser nula.");
        }
        hex = hex.replaceAll("\\s", "");
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Caractere hexadecimal inválido encontrado.");
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    /**
     * Converte um array de 3 bytes para um inteiro de 24 bits (com sinal).
     *
     * @param wordBytes Array de exatamente 3 bytes.
     * @return O valor inteiro correspondente com extensão de sinal para 32 bits.
     * @throws IllegalArgumentException Se o array não tiver exatamente 3 bytes.
     */
    public static int bytesToInt(byte[] wordBytes) {
        if (wordBytes == null || wordBytes.length != 3) {
            throw new IllegalArgumentException("O array deve ter exatamente 3 bytes.");
        }
        int value = ((wordBytes[0] & 0xFF) << 16)
                | ((wordBytes[1] & 0xFF) << 8)
                | (wordBytes[2] & 0xFF);
        // Estende o sinal para 32 bits
        return (value << 8) >> 8;
    }

    /**
     * Converte um array de bytes em uma string hexadecimal (sem espaços).
     * Cada byte é representado por 2 dígitos.
     *
     * @param bytes Array de bytes.
     * @return A string hexadecimal resultante.
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    /**
     * Converte um array de bytes em uma string binária.
     * Cada byte é convertido para 8 dígitos binários, com zeros à esquerda se necessário.
     *
     * @param bytes Array de bytes.
     * @return String binária representando o array, com comprimento = (número de bytes * 8).
     */
    public static String bytesToBinaryString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return sb.toString();
    }

    /**
     * Converte um valor inteiro para uma string hexadecimal de 24 bits (6 dígitos),
     * preenchendo com zeros à esquerda se necessário.
     *
     * @param value O valor a converter.
     * @return String hexadecimal com 6 dígitos.
     */
    public static String intToHexString24(int value) {
        int masked = value & 0xFFFFFF; // considera apenas os 24 bits
        return String.format("%06X", masked);
    }

    public static String longToHexString48(long value) {
        long masked = value & 0xFFFFFFFFFFFFL;
        return String.format("%012X", masked);
    }



}
