package sicxesimulator.utils;

public abstract class Convert {
    /**
     * Converte um inteiro em um array de 3 bytes (big-endian).
     */
    public static byte[] intTo3Bytes(int value) {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) ((value >> 16) & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) (value & 0xFF);
        return bytes;
    }

    /**
     * Converte uma string hexadecimal em um array de bytes.
     */
    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[(len + 1) / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = (i + 1 < len) ? Character.digit(hex.charAt(i + 1), 16) : 0;
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    public static int bytesToInt(byte[] wordBytes) {
        if (wordBytes.length != 3) {
            throw new IllegalArgumentException("O array deve ter exatamente 3 bytes.");
        }

        // Combina os bytes em um inteiro de 24 bits (com sinal)
        int value = ((wordBytes[0] & 0xFF) << 16) | // Byte 0 (mais significativo)
                ((wordBytes[1] & 0xFF) << 8)  | // Byte 1
                (wordBytes[2] & 0xFF);          // Byte 2 (menos significativo)

        // Faz o sign extend para 32 bits (Java usa inteiros com sinal)
        return (value << 8) >> 8;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}