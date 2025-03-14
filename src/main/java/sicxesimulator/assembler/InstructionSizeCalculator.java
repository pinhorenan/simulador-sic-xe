package sicxesimulator.assembler;

public class InstructionSizeCalculator {

    /**
     * Calcula o tamanho da instrução baseado no mnemônico e operando.
      * @param mnemonic Mnemônico da instrução
     * @param operand   Operando da instrução
     * @return          Tamanho da instrução em bytes
     */
    public static int calculateSize(String mnemonic, String operand) {
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return 3; // WORD ocupa 3 bytes
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

        // Para instruções, o formato é determinado pelo mnemônico, e o tamanho em bytes é igual ao formato.
        return determineInstructionFormat(mnemonic);
    }

    /**
     * Determina o formato da instrução baseado no mnemônico.
     * @param mnemonic Mnemônico da instrução
     * @return         O formato da instrução, podendo ser 1, 2, 3 ou 4.
     */
    private static int determineInstructionFormat(String mnemonic) {
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
        // Caso padrão: formato 3
        return 3;
    }
}
