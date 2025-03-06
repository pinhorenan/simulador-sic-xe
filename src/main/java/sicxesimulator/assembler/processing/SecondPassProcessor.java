package sicxesimulator.assembler.processing;

import sicxesimulator.assembler.models.AssemblyLine;
import sicxesimulator.assembler.models.IntermediateRepresentation;
import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.assembler.models.SymbolTable;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.OpcodeMapper;

import java.util.logging.Logger;

public class SecondPassProcessor {
    private static final Logger logger = Logger.getLogger(SecondPassProcessor.class.getName());

    public SecondPassProcessor() {
        // Construtor vazio
    }

    public ObjectFile generateObjectFile(IntermediateRepresentation midObject) {
        int startAddress = midObject.getStartAddress();

        int programBytes = midObject.getAssemblyLines().stream()
                .mapToInt(this::getInstructionSize)
                .sum();
        byte[] objectCode = new byte[programBytes];

        for (AssemblyLine line : midObject.getAssemblyLines()) {
            int offset = (line.getAddress() - startAddress) * 3;

            if (offset < 0 || offset >= objectCode.length) {
                logger.severe("Offset inválido: " + offset + " para " + objectCode.length);
                continue;
            }

            byte[] code = generateObjectCode(line, midObject.getSymbolTable());

            if (offset + code.length > objectCode.length) {
                logger.severe("Código excede tamanho do objectCode. Offset: " + offset);
                continue;
            }

            System.arraycopy(code, 0, objectCode, offset, code.length);
        }

        return new ObjectFile(startAddress, objectCode, midObject.getSymbolTable());
    }

    private int getInstructionSize(AssemblyLine line) {
        return 3; // Supondo formato 3, ajustar conforme necessário
    }

    public byte[] generateObjectCode(AssemblyLine line, SymbolTable symbolTable) {
        String mnemonic = line.getMnemonic();
        String operand = line.getOperand();

        if (mnemonic.equalsIgnoreCase("WORD")) {
            return Convert.intTo3Bytes(parseNumber(operand));
        } else if (mnemonic.equalsIgnoreCase("BYTE")) {
            return parseByteOperand(operand);
        } else if (mnemonic.equalsIgnoreCase("RESW") || mnemonic.equalsIgnoreCase("RESB")) {
            return new byte[parseNumber(operand) * 3];
        } else {
            return generateInstructionCode(line, symbolTable);
        }
    }

    private byte[] generateInstructionCode(AssemblyLine line, SymbolTable symbolTable) {
        String mnemonic = line.getMnemonic();
        String operand = line.getOperand();
        boolean indexed = operand != null && operand.toUpperCase().endsWith(",X");
        String operandString = indexed ? operand.replace(",X", "") : operand;

        int opcode = OpcodeMapper.getOpcode(mnemonic);
        int operandAddress = resolveOperandAddress(operandString, symbolTable);
        int disp = calculateDisplacement(line, operandAddress, indexed);

        byte[] code = new byte[3];
        code[0] = (byte) (opcode | 0x03); // n=1, i=1
        code[1] = (byte) (((indexed ? 0x80 : 0x00) | ((disp >> 8) & 0x0F)));
        code[2] = (byte) (disp & 0xFF);
        return code;
    }

    private int resolveOperandAddress(String operand, SymbolTable symbolTable) {
        if (operand == null) return 0;
        if (operand.startsWith("#")) return parseNumber(operand.substring(1));
        if (symbolTable.contains(operand)) return symbolTable.getAddress(operand) * 3;
        return parseNumber(operand);
    }

    private int calculateDisplacement(AssemblyLine line, int operandAddress, boolean indexed) {
        int disp = operandAddress - (line.getAddress() + 3);
        if (indexed) {
            disp += 0x8000; // Seta o bit de indexação
        }
        if (disp < -2048 || disp > 2047) {
            throw new IllegalArgumentException("Deslocamento PC-relativo inválido: " + disp);
        }
        return disp & 0xFFF;
    }

    private byte[] parseByteOperand(String operand) {
        if (operand.startsWith("X'") && operand.endsWith("'")) {
            return Convert.hexStringToByteArray(operand.substring(2, operand.length() - 1));
        } else if (operand.startsWith("C'") && operand.endsWith("'")) {
            return operand.substring(2, operand.length() - 1).getBytes();
        } else {
            throw new IllegalArgumentException("Formato inválido para BYTE: " + operand);
        }
    }

    private int parseNumber(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente.");
        }
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        } else if (operand.matches("[0-9A-Fa-f]+")) {
            return Integer.parseInt(operand, 16);
        }
        throw new IllegalArgumentException("Formato inválido de número: " + operand);
    }

    public void reset() {
        logger.info("Resetando SecondPassProcessor.");
    }
}
