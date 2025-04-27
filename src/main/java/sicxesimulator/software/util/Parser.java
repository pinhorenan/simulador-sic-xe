package sicxesimulator.software.util;

import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.common.utils.Convert;

import java.util.Objects;

/**
 * Utilitário com métodos para interpretar e converter operandos, literais e
 * formatos de instruções no montador SIC/XE.
 *
 * <p>Fornece:</p>
 * <ul>
 *   <li>{@link #parseAddress(String)} – converte string decimal ou "0x" hex em inteiro;</li>
 *   <li>{@link #parseNumber(String)}  – converte literal numérico decimal/hex em inteiro;</li>
 *   <li>{@link #parseByteOperand(String)} – converte operando BYTE (C'...' ou X'...') em bytes;</li>
 *   <li>{@link #resolveOperandAddress(String, SymbolTable)} – resolve símbolos ou números;</li>
 *   <li>{@link #determineInstructionFormat(String)} – retorna formato 1–4 conforme mnemônico.</li>
 * </ul>
 *
 * <p>Exemplo:</p>
 * <pre>{@code
 * int addr = Parser.parseAddress("0x100");
 * byte[] data = Parser.parseByteOperand("C'ABC'");
 * int fmt = Parser.determineInstructionFormat("+LDA");
 * }</pre>
 */
public final class Parser {
    private Parser() { }

    /**
     * Interpreta uma string como endereço (decimal ou hexadecimal "0x").
     *
     * @param operand string de endereço (não nula)
     * @return valor inteiro do endereço
     * @throws NullPointerException     se {@code operand} for nulo
     * @throws IllegalArgumentException se o formato for inválido
     */
    public static int parseAddress(String operand) {
        Objects.requireNonNull(operand, "operand não pode ser nulo");
        String op = operand.trim().toLowerCase();
        if (op.startsWith("0x")) {
            return Integer.parseInt(op.substring(2), 16);
        }
        if (op.matches("\\d+")) {
            return Integer.parseInt(op);
        }
        throw new IllegalArgumentException(
                "Formato inválido de endereço: " + operand +
                        ". Use decimal (ex: 100) ou 0x hex (ex: 0x100).");
    }

    /**
     * Interpreta um literal numérico (decimal ou hexadecimal "0x").
     *
     * @param operand literal numérico (não nulo)
     * @return valor inteiro do literal
     * @throws NullPointerException     se {@code operand} for nulo
     * @throws IllegalArgumentException se o formato for inválido
     */
    public static int parseNumber(String operand) {
        Objects.requireNonNull(operand, "operand não pode ser nulo");
        String op = operand.trim().toLowerCase();
        if (op.startsWith("0x")) {
            return Integer.parseInt(op.substring(2), 16);
        }
        if (op.matches("\\d+")) {
            return Integer.parseInt(op);
        }
        throw new IllegalArgumentException(
                "Formato inválido de número: " + operand +
                        ". Use decimal (ex: 100) ou 0x hex (ex: 0x1a).");
    }

    /**
     * Converte um operando BYTE (C'...' ou X'...') em array de bytes.
     *
     * @param operand operando BYTE (não nulo)
     * @return vetor de bytes correspondente
     * @throws NullPointerException     se {@code operand} for nulo
     * @throws IllegalArgumentException se o formato for inválido
     */
    public static byte[] parseByteOperand(String operand) {
        Objects.requireNonNull(operand, "operand não pode ser nulo");
        if (operand.startsWith("X'") && operand.endsWith("'")) {
            String hex = operand.substring(2, operand.length() - 1);
            return Convert.hexStringToByteArray(hex);
        }
        if (operand.startsWith("C'") && operand.endsWith("'")) {
            String chars = operand.substring(2, operand.length() - 1);
            return chars.getBytes();
        }
        throw new IllegalArgumentException("Formato inválido para BYTE: " + operand);
    }

    /**
     * Resolve o endereço de um operando: se começa com '#', trata como literal;
     * senão, tenta símbolo na tabela ou literal decimal/hex.
     *
     * @param operand     operando de instrução (pode ser nulo → retorna 0)
     * @param symbolTable tabela de símbolos (não nula)
     * @return endereço inteiro resolvido
     * @throws NullPointerException     se {@code symbolTable} for nulo
     * @throws IllegalArgumentException se não for possível resolver
     */
    public static int resolveOperandAddress(String operand, SymbolTable symbolTable) {
        Objects.requireNonNull(symbolTable, "symbolTable não pode ser nulo");
        if (operand == null) {
            return 0;
        }
        String op = operand.trim();
        if (op.startsWith("#")) {
            return parseNumber(op.substring(1));
        }
        String key = op.toUpperCase();
        if (symbolTable.contains(key)) {
            return symbolTable.getSymbolAddress(key);
        }
        return parseNumber(op);
    }

    /**
     * Determina o formato da instrução (1, 2, 3 ou 4) a partir do mnemônico.
     *
     * @param mnemonic mnemônico (não nulo)
     * @return formato numérico da instrução
     * @throws NullPointerException se {@code mnemonic} for nulo
     */
    public static int determineInstructionFormat(String mnemonic) {
        Objects.requireNonNull(mnemonic, "mnemonic não pode ser nulo");
        if (mnemonic.startsWith("+")) {
            return 4;
        }
        return switch (mnemonic.toUpperCase()) {
            case "FIX", "FLOAT", "NORM", "SIO", "HIO", "TIO" -> 1;
            case "CLEAR", "COMPR", "SUBR", "ADDR", "RMO", "TIXR" -> 2;
            default -> 3;
        };
    }
}
