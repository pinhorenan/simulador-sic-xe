package sicxesimulator.software.assembler.util;

import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.utils.Converter;

public abstract class Parser {

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

    public static byte[] parseByteOperand(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente para BYTE.");
        }
        if (operand.startsWith("X'") && operand.endsWith("'")) {
            String hex = operand.substring(2, operand.length() - 1);
            return Converter.hexStringToByteArray(hex);
        } else if (operand.startsWith("C'") && operand.endsWith("'")) {
            String chars = operand.substring(2, operand.length() - 1);
            return chars.getBytes();
        } else {
            throw new IllegalArgumentException("Formato invalido para BYTE: " + operand);
        }
    }

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
