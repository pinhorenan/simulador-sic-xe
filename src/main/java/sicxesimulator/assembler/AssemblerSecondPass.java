package sicxesimulator.assembler;

import sicxesimulator.models.AssemblyLine;
import sicxesimulator.models.IntermediateRepresentation;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.models.SymbolTable;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.Map;
import sicxesimulator.utils.SimulatorLogger;

public class AssemblerSecondPass {
    /**
     * Gera o código objeto a partir da IntermediateRepresentation.
     * Considera que o LC na IR está em bytes.
     *
     * @param midObject Representação intermediária gerada pela primeira passagem.
     * @return ObjectFile contendo o endereço inicial e o código objeto.
     */
    protected ObjectFile generateObjectFile(IntermediateRepresentation midObject) {
        // Endereço inicial (em bytes)
        int startAddress = midObject.getStartAddress();

        // Calcula o tamanho total do programa (em bytes)
        int programSize = midObject.getAssemblyLines()
                .stream()
                .mapToInt(this::getInstructionSize)
                .sum();

        byte[] objectCode = new byte[programSize];

        for (AssemblyLine line : midObject.getAssemblyLines()) {
            // Calcula o offset em bytes a partir do endereço inicial
            int offset = line.address() - startAddress;
            if (offset < 0 || offset >= objectCode.length) {
                SimulatorLogger.logError("Offset inválido: " + offset + " para array de tamanho " + objectCode.length, null);
                continue;
            }

            byte[] code;
            try {
                code = generateObjectCode(line, midObject.getSymbolTable());
            } catch (Exception e) {
                SimulatorLogger.logError("Erro gerando código objeto da linha: " + line, e);
                continue;
            }

            if (offset + code.length > objectCode.length) {
                SimulatorLogger.logError("Código excede tamanho do objectCode. Offset: " + offset, null);
                continue;
            }

            System.arraycopy(code, 0, objectCode, offset, code.length);
        }

        SymbolTable symbolTable = midObject.getSymbolTable();
        String programName = midObject.getProgramName();
        SimulatorLogger.logMachineCode("Código objeto gerado para o programa: " + programName);
        return new ObjectFile(startAddress, objectCode, symbolTable, programName, midObject.getRawSourceLines());
    }

    private int getInstructionSize(AssemblyLine line) {
        return InstructionSizeCalculator.calculateSize(line.mnemonic(), line.operand());
    }

    /**
     * Gera o código objeto para uma única linha de assembly.
     */
    public byte[] generateObjectCode(AssemblyLine line, SymbolTable symbolTable) {
        String mnemonic = line.mnemonic();
        String operand = line.operand();

        // Diretivas
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return Convert.intTo3Bytes(parseNumber(operand));
        } else if (mnemonic.equalsIgnoreCase("BYTE")) {
            return parseByteOperand(operand);
        } else if (mnemonic.equalsIgnoreCase("RESW") || mnemonic.equalsIgnoreCase("RESB")) {
            int count = parseNumber(operand) * (mnemonic.equalsIgnoreCase("RESW") ? 3 : 1);
            return new byte[count];
        }

        // Determine o formato da instrução a partir do mnemônico
        int format = determineInstructionFormat(mnemonic);
        return switch (format) {
            case 1 -> generateInstructionCodeFormat1(mnemonic);
            case 2 -> generateInstructionCodeFormat2(mnemonic, operand);
            case 3 -> generateInstructionCodeFormat3(mnemonic, operand, symbolTable, line);
            case 4 -> generateInstructionCodeFormat4(mnemonic, operand, symbolTable);
            default -> throw new IllegalArgumentException("Formato de instrução desconhecido para " + mnemonic);
        };
    }

    // Função auxiliar para determinar o formato a partir do mnemônico.
    private int determineInstructionFormat(String mnemonic) {
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
        return 3; // Caso padrão: formato 3.
    }

    private byte[] generateInstructionCodeFormat1(String mnemonic) {
        mnemonic = mnemonic.replace("+", "");
        int opcode = Map.mnemonicToOpcode(mnemonic);
        return new byte[]{ (byte) opcode };
    }

    private byte[] generateInstructionCodeFormat2(String mnemonic, String operand) {
        int opcode = Map.mnemonicToOpcode(mnemonic);
        String[] regs = operand.split(",");
        if (regs.length != 2) {
            throw new IllegalArgumentException("Instrução de formato 2 requer 2 registradores: " + operand);
        }
        int r1 = Map.registerNameToNumber(regs[0].trim());
        int r2 = Map.registerNameToNumber(regs[1].trim());
        byte[] code = new byte[2];
        code[0] = (byte) opcode;
        code[1] = (byte) ((r1 << 4) | (r2 & 0x0F));
        return code;
    }

    private byte[] generateInstructionCodeFormat3(String mnemonic, String operand, SymbolTable symbolTable, AssemblyLine line) {
        boolean indexed = operand != null && operand.toUpperCase().endsWith(",X");
        String operandString = indexed ? operand.replace(",X", "") : operand;
        int opcode = Map.mnemonicToOpcode(mnemonic);
        int operandAddress = resolveOperandAddress(operandString, symbolTable);
        int disp = calculateDisplacement(line, operandAddress);
        byte[] code = new byte[3];
        code[0] = (byte) (opcode | 0x03); // Define n=1, i=1
        int secondByte = 0;
        if (indexed) {
            secondByte |= 0x80;
        }
        secondByte |= 0x20; // Bit p para PC-relativo
        secondByte |= ((disp >> 8) & 0x0F);
        code[1] = (byte) secondByte;
        code[2] = (byte) (disp & 0xFF);
        return code;
    }

    private byte[] generateInstructionCodeFormat4(String mnemonic, String operand, SymbolTable symbolTable) {
        mnemonic = mnemonic.replace("+", "");
        int opcode = Map.mnemonicToOpcode(mnemonic);
        boolean indexed = operand != null && operand.toUpperCase().endsWith(",X");
        String operandString = indexed ? operand.replace(",X", "") : operand;
        int operandAddress = resolveOperandAddress(operandString, symbolTable);
        int firstByte = (opcode & 0xFC) | 0x03; // n=1, i=1
        int secondByte = 0;
        if (indexed) {
            secondByte |= 0x80;
        }
        secondByte |= 0x10; // seta e=1; b e p = 0 para endereço absoluto
        int high4 = (operandAddress >> 16) & 0x0F;
        secondByte |= high4;
        byte[] code = new byte[4];
        code[0] = (byte) firstByte;
        code[1] = (byte) secondByte;
        code[2] = (byte) ((operandAddress >> 8) & 0xFF);
        code[3] = (byte) (operandAddress & 0xFF);
        return code;
    }

    // Resolve o endereço do operando; agora, como os símbolos são armazenados em bytes, não multiplica por 3.
    private int resolveOperandAddress(String operand, SymbolTable symbolTable) {
        if (operand == null) return 0;
        if (operand.startsWith("#")) {
            return parseNumber(operand.substring(1));
        }
        if (symbolTable.contains(operand)) {
            return symbolTable.getAddress(operand);
        }
        return parseNumber(operand);
    }

    private int calculateDisplacement(AssemblyLine line, int operandByteAddr) {
        int currentInstructionByteAddr = line.address(); // LC em bytes
        int nextInstructionByteAddr = currentInstructionByteAddr + getInstructionSize(line);
        int disp = operandByteAddr - nextInstructionByteAddr;
        if (disp < -2048 || disp > 2047) {
            throw new IllegalArgumentException("Deslocamento PC-relativo inválido: " + disp);
        }
        return disp & 0xFFF;
    }

    private byte[] parseByteOperand(String operand) {
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
            throw new IllegalArgumentException("Formato inválido para BYTE: " + operand);
        }
    }

    private int parseNumber(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente.");
        }
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        }
        if (operand.matches("[0-9A-Fa-f]+")) {
            return Integer.parseInt(operand, 16);
        }
        throw new IllegalArgumentException("Formato inválido de número: " + operand);
    }
}
