package sicxesimulator.assembler.util;

import sicxesimulator.models.SymbolTable;
import sicxesimulator.utils.Convert;

public abstract class Parser {

    /**
     * Converte um operando em um endereço numérico.
     *
     * @param operand Operando a ser convertido.
     * @return Endereço numérico.
     * @throws IllegalArgumentException se o operando estiver ausente ou com formato inválido.
     */
    public static int parseAddress(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente para endereço.");
        }
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        } else if (operand.matches("[0-9A-Fa-f]+")) {
            return Integer.parseInt(operand, 16);
        }
        throw new IllegalArgumentException("Formato inválido de endereço: " + operand);
    }

    /**
     * Converte uma string em um número inteiro (decimal ou hexadecimal).
     *
     * @param operand String contendo o número.
     * @return Número inteiro.
     * @throws IllegalArgumentException se o operando estiver ausente ou com formato inválido.
     */
    public static int parseNumber(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente.");
        }
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        }
        if (operand.matches("[0-9A-Fa-f]+")) {
            return Integer.parseInt(operand, 16);
        }
        throw new IllegalArgumentException("Formato inválido de número: " + operand);
    }

    /**
     * Parseia o operando da diretiva BYTE para um array de bytes.
     *
     * @param operand Operando da diretiva BYTE.
     * @return Array de bytes correspondente.
     * @throws IllegalArgumentException se o operando estiver ausente ou com formato inválido.
     */
    public static byte[] parseByteOperand(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente para BYTE.");
        }
        if (operand.startsWith("X'") && operand.endsWith("'")) {
            String hex = operand.substring(2, operand.length() - 1);
            return Convert.hexStringToByteArray(hex);
        } else if (operand.startsWith("C'") && operand.endsWith("'")) {
            String chars = operand.substring(2, operand.length() - 1);
            return chars.getBytes();
        } else {
            throw new IllegalArgumentException("Formato inválido para BYTE: " + operand);
        }
    }

    /**
     * Resolve o endereço do operando, considerando símbolos e valores imediatos.
     *
     * @param operand     Operando da instrução.
     * @param symbolTable Tabela de símbolos.
     * @return Endereço numérico do operando.
     */
    public static int resolveOperandAddress(String operand, SymbolTable symbolTable) {
        if (operand == null) return 0;
        if (operand.startsWith("#")) {
            return parseNumber(operand.substring(1));
        }
        String symKey = operand.toUpperCase();
        if (symbolTable.contains(symKey)) {
            return symbolTable.getSymbolAddress(symKey);
        }
        return parseNumber(operand);
    }

    /**
     * Determina o formato da instrução (1, 2, 3 ou 4) com base no mnemônico.
     *
     * @param mnemonic Mnemônico da instrução.
     * @return Formato da instrução.
     */
    public static int determineInstructionFormat(String mnemonic) {
        if (mnemonic.startsWith("+")) {
            return 4;
        }
        if (mnemonic.equalsIgnoreCase("FIX") || mnemonic.equalsIgnoreCase("FLOAT")
                || mnemonic.equalsIgnoreCase("NORM") || mnemonic.equalsIgnoreCase("SIO")
                || mnemonic.equalsIgnoreCase("HIO") || mnemonic.equalsIgnoreCase("TIO")) {
            return 1;
        }
        if (mnemonic.equalsIgnoreCase("CLEAR") || mnemonic.equalsIgnoreCase("COMPR")
                || mnemonic.equalsIgnoreCase("SUBR") || mnemonic.equalsIgnoreCase("ADDR")
                || mnemonic.equalsIgnoreCase("RMO") || mnemonic.equalsIgnoreCase("TIXR")) {
            return 2;
        }
        return 3;
    }
}
