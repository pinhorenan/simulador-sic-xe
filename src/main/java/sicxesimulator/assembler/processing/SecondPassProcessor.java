package sicxesimulator.assembler.processing;

import sicxesimulator.assembler.models.AssemblyLine;
import sicxesimulator.assembler.models.IntermediateRepresentation;
import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.assembler.models.SymbolTable;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.OpcodeMapper;

import java.util.Arrays;
import java.util.logging.Logger;

public class SecondPassProcessor {
    private static final Logger logger = Logger.getLogger(SecondPassProcessor.class.getName());

    public SecondPassProcessor() {
        // Construtor vazio
    }

    /**
     * Gera o código objeto a partir da IntermediateRepresentation.
     * Assume que os endereços são mantidos em palavras (cada palavra = 3 bytes).
     *
     * @param ir Representação intermediária gerada pela primeira passagem.
     * @return ObjectFile contendo o endereço inicial e o código objeto.
     */
    public ObjectFile generateObjectFile(IntermediateRepresentation ir) {
        int startAddress = ir.getStartAddress();
        int finalAddress = ir.getFinalAddress();

        int programBytes = (finalAddress - startAddress) * 3;
        byte[] objectCode = new byte[programBytes];

        // Para cada linha de assembly, gera o código objeto e posiciona no array final
        for (AssemblyLine line : ir.getAssemblyLines()) {
            int offset = (line.getAddress() - startAddress) * 3;
            byte[] code = generateObjectCode(line, ir.getSymbolTable());
            System.arraycopy(code, 0, objectCode, offset, code.length);
        }
        return new ObjectFile(startAddress, objectCode);
    }

    public byte[] generateObjectCode(AssemblyLine line, SymbolTable symbolTable) {
        String mnemonic = line.getMnemonic();
        String operand = line.getOperand();

        if (mnemonic.equalsIgnoreCase("WORD")) { // TODO: Pedir explicação
            int value = Integer.parseInt(operand, 16);
            return Convert.intTo3Bytes(value);
        } else if (mnemonic.equalsIgnoreCase("BYTE")) { // TODO: Pedir explicação
            byte[] data;
            if (operand.startsWith("X'") && operand.endsWith("'")) {
                String hex = operand.substring(2, operand.length() - 1);
                data = Convert.hexStringToByteArray(hex);
            } else if (operand.startsWith("C'") && operand.endsWith("'")) {
                String chars = operand.substring(2, operand.length() - 1);
                data = chars.getBytes();
            } else {
                throw new IllegalArgumentException("Formato inválido para BYTE: " + operand);
            }

            // Preenche com zeros para completar múltiplos de 3 bytes, se necessário
            int remainder = data.length % 3;
            if (remainder != 0) {
                int newLength = data.length + (3 - remainder);
                data = Arrays.copyOf(data, newLength);
            }
            return data;
        } else if (mnemonic.equalsIgnoreCase("RESW") || mnemonic.equalsIgnoreCase("RESB")) {
            int sizeBytes;
            if (mnemonic.equalsIgnoreCase("RESW")) {
                sizeBytes = Integer.parseInt(operand) * 3;
            } else {
                sizeBytes = Integer.parseInt(operand);
            }
            return new byte[sizeBytes];
        } else {
            // Instruções de formato 3 (ou ajustadas)
            boolean indexed = operand != null && operand.toUpperCase().endsWith(",X");
            String operandString = (indexed && operand != null)
                    ? operand.replace(",X", "")
                    : operand;

            int opcode = OpcodeMapper.getOpcode(mnemonic);
            int operandAddress = 0;
            int flags = 0x03; // n=1, i=1
            int disp = 0;

            if (operandString != null) {
                if (operandString.startsWith("=")) {
                    if (!symbolTable.contains(operandString)) {
                        throw new IllegalStateException("Literal não resolvido: " + operandString);
                    }
                    operandAddress = symbolTable.getAddress(operandString);
                } else if (symbolTable.contains(operandString)) {
                    operandAddress = symbolTable.getAddress(operandString);
                } else {
                    operandAddress = Integer.parseInt(operandString, 16);
                }

                // Para PC-relativo, usamos o endereço da próxima instrução (linha.address + 1)
                int programCounter = line.getAddress() + 1;
                disp = operandAddress - programCounter;

                if (disp >= -2048 && disp <= 2047) {
                    flags = 0x13; // Indica PC-relativo
                } else {
                    // Se for necessário tratar base-relativo, adicione a lógica aqui
                    throw new IllegalArgumentException("Deslocamento fora do alcance para PC-relativo: " + disp);
                }
                if (indexed) {
                    flags |= 0x80;
                }
            }
            disp = disp & 0xFFF; // Usa apenas 12 bits para o deslocamento

            byte[] code = new byte[3];
            code[0] = (byte) (opcode | (flags >> 6));
            code[1] = (byte) (((flags & 0x3F) << 2) | ((disp >> 8) & 0x0F));
            code[2] = (byte) (disp & 0xFF);
            return code;
        }
    }

    /**
     * Reinicia o estado interno do processador.
     */
    public void reset() {
        // Se houver algum estado interno que precise ser reiniciado, implemente aqui.
    }
}
