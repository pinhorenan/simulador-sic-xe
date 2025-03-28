package sicxesimulator.utils;

import static sicxesimulator.utils.Constants.VALID_REGISTERS;

/**
 * Classe utilitária para validações sintáticas e semânticas básicas no contexto do SIC/XE.
 *
 * <p>Fornece métodos auxiliares para verificar se tokens são mnemônicos válidos, literais numéricos
 * ou nomes de registradores válidos.</p>
 *
 * <p>Esta classe é abstrata e não deve ser instanciada.</p>
 */
public abstract class Checker {

    /**
     * Verifica se um token representa um mnemônico válido no conjunto de instruções do SIC/XE.
     *
     * @param token Token a ser verificado.
     * @return {@code true} se o token corresponder a um mnemônico válido (case-insensitive).
     */
    public static boolean isMnemonic(String token) {
        return Constants.VALID_MNEMONICS.contains(token.toUpperCase());
    }

    /**
     * Verifica se o token representa um literal numérico.
     *
     * <p>Um literal é considerado numérico se corresponder a uma sequência válida de dígitos decimais
     * ou hexadecimais. Não são verificados prefixos como {@code 0x} ou {@code H}.</p>
     *
     * @param token Token a ser verificado.
     * @return {@code true} se o token for reconhecido como literal numérico.
     */
    public static boolean isNumericLiteral(String token) {
        if (token.matches("[0-9A-Fa-f]+")) {
            return true; // interpretamos como hexa/decimal
        }
        return token.matches("\\d+");
    }

    /**
     * Verifica se um nome de registrador é válido no conjunto de registradores do SIC/XE.
     *
     * <p>A verificação é feita com base em uma lista constante {@link Constants#VALID_REGISTERS}.</p>
     *
     * @param registerName Nome do registrador (ex: {@code "A"}, {@code "X"}, {@code "S"}).
     * @return {@code true} se for um nome válido de registrador.
     */
    public static boolean isValidRegisterName(String registerName) {
        for (String validName : VALID_REGISTERS) {
            if (validName.equals(registerName)) return true;
        }
        return false;
    }
}
