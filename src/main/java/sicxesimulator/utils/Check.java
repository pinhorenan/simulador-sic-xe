package sicxesimulator.utils;

import static sicxesimulator.utils.Constants.VALID_REGISTERS;

/**
 * Classe utilitária para verificações diversas.
 */
public abstract class Check {

    /**
     * Verifica se um token é um mnemônico válido.
     *
     * @param token Token a ser verificado.
     * @return Verdadeiro se o token for um mnemônico válido.
     */
    public static boolean isMnemonic(String token) {
        return Constants.VALID_MNEMONICS.contains(token.toUpperCase());
    }

    /**
     * Verifica se s é um literal numérico (hexa ou decimal)
     * @param token Token a ser verificado.
     * @return Verdadeiro se o token for um literal numérico.
     */
    public static boolean isNumericLiteral(String token) {
        if (token.matches("[0-9A-Fa-f]+")) {
            return true; // interpretamos como hexa/decimal
        }
        return token.matches("\\d+");
    }

    /**
     * Verifica se um token é um registrador válido.
     * @param registerName Nome do registrador.
     * @return Verdadeiro se o token for um registrador válido.
     */
    public static boolean isValidRegisterName(String registerName) {
        for (String validName : VALID_REGISTERS) {
            if (validName.equals(registerName)) return true;
        }
        return false;
    }
}
