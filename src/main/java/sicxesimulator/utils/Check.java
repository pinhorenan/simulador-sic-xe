package sicxesimulator.utils;

import static sicxesimulator.utils.Constants.VALID_REGISTERS;

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
     * Verifica se um token é um registrador válido.
     * @param name Nome do registrador.
     * @return Verdadeiro se o token for um registrador válido.
     */
    public static boolean isValidRegisterName(String name) {
        for (String validName : VALID_REGISTERS) {
            if (validName.equals(name)) return true;
        }
        return false;
    }
}
