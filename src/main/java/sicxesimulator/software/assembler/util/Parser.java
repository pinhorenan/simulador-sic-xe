package sicxesimulator.software.assembler.util;

import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.common.utils.Convert;

/**
 * Classe utilitária com funções auxiliares para interpretar e converter
 * operandos, endereços, literais e formatos de instruções no montador.
 */
public abstract class Parser {

    /**
     * Interpreta uma string como endereço (decimal ou hexadecimal).
     *
     * @param operand String com o operando.
     * @return Endereço como inteiro.
     * @throws IllegalArgumentException Se o formato for inválido.
     */
    public static int parseAddress(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente para endereco.");
        }
        operand = operand.trim().toLowerCase();

        // Se começar com "0x", interpretamos como hexadecimal
        if (operand.startsWith("0x")) {
            return Integer.parseInt(operand.substring(2), 16);
        }
        // Caso contrário, interpretamos como decimal
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        }
        throw new IllegalArgumentException("Formato invalido de endereco: " + operand
                + ". Use apenas decimal (ex: 100) ou 0x para hex (ex: 0x100).");
    }

    /**
     * Interpreta um literal numérico (decimal ou hexadecimal).
     *
     * @param operand Literal numérico como string.
     * @return Valor inteiro.
     */
    public static int parseNumber(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente.");
        }
        operand = operand.trim().toLowerCase();

        // Se começar com "0x", interpretamos como hexadecimal
        if (operand.startsWith("0x")) {
            return Integer.parseInt(operand.substring(2), 16);
        }
        // Caso contrário, interpretamos como decimal
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        }
        throw new IllegalArgumentException("Formato invalido de numero: " + operand
                + ". Use decimal (ex: 100) ou 0x para hex (ex: 0x1a).");
    }

    /**
     * Converte um operando do tipo BYTE (C'...' ou X'...') para bytes.
     *
     * @param operand Operando da diretiva BYTE.
     * @return Array de bytes correspondente.
     * @throws IllegalArgumentException Se o formato for inválido.
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
            throw new IllegalArgumentException("Formato invalido para BYTE: " + operand);
        }
    }

    /**
     * Resolve o endereço de um operando.
     *
     * @param operand Operando da instrução.
     * @param symbolTable Tabela de símbolos atual.
     * @return Endereço resolvido do operando.
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
     * Determina o formato da instrução com base no mnemônico.
     *
     * @param mnemonic Mnemônico da instrução.
     * @return Formato da instrução (1, 2, 3 ou 4).
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
