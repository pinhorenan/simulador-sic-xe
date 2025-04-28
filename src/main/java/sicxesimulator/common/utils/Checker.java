package sicxesimulator.common.utils;

import java.util.regex.Pattern;

import static sicxesimulator.common.utils.Constants.VALID_REGISTERS;

/**
 * Validações sintáticas rápidas para o código SIC/XE.
 *
 * <p>Todos os métodos são <em>static</em>; a classe não deve ser instanciada.</p>
 */
public final class Checker {
    private Checker() {}   // util-class

    /* ------------------------------------------------------------- */

    /** cache de regex para literal numérico (dec ou hex) */
    private static final Pattern NUMERIC = Pattern.compile("^[0-9A-Fa-f]+$");

    /** <b>Mnemonic</b> → pertence ao conjunto válido?  */
    public static boolean isMnemonic(String token) {
        return Constants.VALID_MNEMONICS.contains(token.toUpperCase());
    }

    /** Literal numérico (decimal ou hex sem prefixo). */
    public static boolean isNumericLiteral(String token) {
        return NUMERIC.matcher(token).matches();
    }

    /** Nome de registrador válido (A,X,L,B,S,T,F,PC,SW). */
    public static boolean isValidRegisterName(String name) {
        for (String r : VALID_REGISTERS) if (r.equalsIgnoreCase(name)) return true;
        return false;
    }
}
