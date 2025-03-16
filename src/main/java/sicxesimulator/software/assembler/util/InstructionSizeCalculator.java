package sicxesimulator.software.assembler.util;

/**
 * Utilitário para calcular o tamanho em bytes de instruções e diretivas assembly.
 */
public abstract class InstructionSizeCalculator {

    /**
     * Calcula o tamanho da instrução com base no mnemônico e no operando.
     *
     * @param mnemonic Mnemônico da instrução ou diretiva.
     * @param operand  Operando associado (pode ser nulo para instruções sem operando).
     * @return Tamanho da instrução em bytes.
     * @throws IllegalArgumentException se o operando for necessário, mas estiver ausente.
     */
    public static int calculateSize(String mnemonic, String operand) {
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return 3;
        }
        if (mnemonic.equalsIgnoreCase("RESW")) {
            if (operand == null) {
                throw new IllegalArgumentException("Operando ausente para RESW.");
            }
            return Integer.parseInt(operand) * 3;
        }
        if (mnemonic.equalsIgnoreCase("RESB")) {
            if (operand == null) {
                throw new IllegalArgumentException("Operando ausente para RESB.");
            }
            return Integer.parseInt(operand);
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
