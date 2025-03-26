package sicxesimulator.software.assembler.util;

public abstract class InstructionSizeCalculator {

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
