package sicxesimulator.common.utils;

/**
 * Conversões utilitárias (inteiro ⇆ bytes, binário, hex).
 *
 * <p>Todos os métodos são <em>pure functions</em>; a classe é não-instanciável.</p>
 */
public final class Convert {
    private Convert() {}

    /* --------------------------------------------------------- */
    /* 3 bytes (WORD SIC)                                        */
    /* --------------------------------------------------------- */

    public static byte[] intTo3Bytes(int value) {
        return new byte[] {
                (byte)((value >>> 16) & 0xFF),
                (byte)((value >>> 8 ) & 0xFF),
                (byte)( value         & 0xFF)
        };
    }

    public static int bytesToInt(byte[] word) {
        if (word == null || word.length != 3)
            throw new IllegalArgumentException("Array deve ter 3 bytes.");
        int v = ((word[0] & 0xFF) << 16) |
                ((word[1] & 0xFF) << 8 ) |
                (word[2] & 0xFF);
        return (v << 8) >> 8;                 // sign-extend 24→32 bits
    }

    /* --------------------------------------------------------- */
    /* Strings de bits / hex                                     */
    /* --------------------------------------------------------- */

    public static String intToBinaryString24(int value) {
        return String.format("%24s", Integer.toBinaryString(value & 0xFFFFFF))
                .replace(' ', '0');
    }

    public static String longToBinaryString48(long value) {
        return String.format("%48s", Long.toBinaryString(value & 0xFFFFFFFFFFFFL))
                .replace(' ', '0');
    }

    public static String intToHexString24(int value) {
        return String.format("%06X", value & 0xFFFFFF);
    }

    public static String longToHexString48(long value) {
        return String.format("%012X", value & 0xFFFFFFFFFFFFL);
    }

    /* --------------------------------------------------------- */

    public static byte[] hexStringToByteArray(String hex) {
        if (hex == null) throw new IllegalArgumentException("hex nulo");
        hex = hex.replaceAll("\\s", "");
        if ((hex.length() & 1) != 0) hex = '0' + hex;  // garante par
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i  ), 16);
            int lo = Character.digit(hex.charAt(i+1), 16);
            if (hi < 0 || lo < 0) throw new IllegalArgumentException("hex inválido");
            out[i>>1] = (byte)((hi << 4) | lo);
        }
        return out;
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        var sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    public static String bytesToBinaryString(byte[] bytes) {
        if (bytes == null) return "";
        var sb = new StringBuilder(bytes.length * 8);
        for (byte b : bytes)
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF))
                    .replace(' ', '0'));
        return sb.toString();
    }
}
