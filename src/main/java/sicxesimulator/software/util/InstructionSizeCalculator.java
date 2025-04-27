package sicxesimulator.software.util;

/**
 * Utilitário responsável por calcular o tamanho de uma instrução ou diretiva.
 * <p>
 * Considera as diretivas (WORD, BYTE, RESW, RESB) e determina o formato de
 * instruções (1, 2, 3 ou 4) com base no mnemônico.
 */
public abstract class InstructionSizeCalculator {

    /**
     * Calcula o tamanho, em bytes, de uma instrução ou diretiva.
     *
     * @param mnemonic Mnemônico da instrução.
     * @param operand Operando associado (pode ser null em algumas diretivas).
     * @return Tamanho da instrução/diretiva em bytes.
     * @throws IllegalArgumentException Se o operando for inválido ou ausente.
     */
    public static int calculateSize(String mnemonic, String operand) {
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return 3;
        }
        if (mnemonic.equalsIgnoreCase("RESW")) {
            if (operand == null) {
                throw new IllegalArgumentException("Operando ausente para RESW.");
            }
            return Parser.parseNumber(operand) * 3;
        }
        if (mnemonic.equalsIgnoreCase("RESB")) {
            if (operand == null) {
                throw new IllegalArgumentException("Operando ausente para RESB.");
            }
            return Parser.parseNumber(operand);
        }
        if (mnemonic.equalsIgnoreCase("BYTE")) {
            if (operand == null) {
                throw new IllegalArgumentException("Operando ausente para BYTE.");
            }
            if (operand.startsWith("C'") && operand.endsWith("'")) {
                return operand.length() - 3;
            } else if (operand.startsWith("X'") && operand.endsWith("'")) {
                return (operand.length() - 3) / 2;
            }
        }
        // Para instruções, o tamanho é definido pelo formato
        return Parser.determineInstructionFormat(mnemonic);
    }

}
