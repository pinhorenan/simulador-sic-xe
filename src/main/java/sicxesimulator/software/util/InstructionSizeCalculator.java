package sicxesimulator.software.util;

import java.util.Objects;

/**
 * Utilitário para calcular o tamanho em bytes de instruções e diretivas SIC/XE.
 *
 * <p>Suporta diretivas WORD, RESW, RESB e BYTE, além de instruções nos
 * formatos 1–4 conforme mnemônico.</p>
 *
 * <p>Exemplo de uso:</p>
 * <pre>{@code
 * int size1 = InstructionSizeCalculator.calculateSize("WORD", null);    // 3 bytes
 * int size2 = InstructionSizeCalculator.calculateSize("RESW", "5");     // 5*3 = 15
 * int size3 = InstructionSizeCalculator.calculateSize("LDA", "#10");    // formato 3 → 3 bytes
 * }</pre>
 */
public final class InstructionSizeCalculator {
    private InstructionSizeCalculator() { }

    /**
     * Calcula o tamanho, em bytes, de uma instrução ou diretiva.
     *
     * @param mnemonic mnemônico da instrução ou diretiva (não nulo)
     * @param operand  operando associado (pode ser nulo para WORD ou instruções)
     * @return tamanho em bytes
     * @throws NullPointerException     se {@code mnemonic} for nulo
     * @throws IllegalArgumentException se o operando for inválido ou estiver ausente quando obrigatório
     */
    public static int calculateSize(String mnemonic, String operand) {
        Objects.requireNonNull(mnemonic, "mnemonic não pode ser nulo");
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
            } else {
                throw new IllegalArgumentException("Formato inválido de BYTE: " + operand);
            }
        }
        // Para instruções, o tamanho é o próprio formato (1,2,3 ou 4)
        return Parser.determineInstructionFormat(mnemonic);
    }
}
