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

}
