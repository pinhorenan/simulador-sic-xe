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
     * Cada instrução é assumida em formato 3 (3 bytes),
     * e line.getAddress() está em palavras (1 palavra = 3 bytes).
     *
     * @param midObject Representação intermediária gerada pela primeira passagem.
     * @return ObjectFile contendo o endereço inicial e o código objeto.
     */
    protected ObjectFile generateObjectFile(IntermediateRepresentation midObject) {
        // Endereço inicial em palavras
        int startAddress = midObject.getStartAddress();

        // Calcula o tamanho total em bytes, somando 3 bytes por instrução (formato 3)
        int programSize = midObject.getAssemblyLines()
                .stream()
                .mapToInt(this::getInstructionSize)
                .sum();

        byte[] objectCode = new byte[programSize];

        for (AssemblyLine line : midObject.getAssemblyLines()) {
            // Converte line.getAddress() (palavras) para bytes e subtrai startAddress (também em palavras)
            int offset = (line.address() - startAddress) * 3;

            // Verifica se offset é válido no array
            if (offset < 0 || offset >= objectCode.length) {
                SimulatorLogger.logError("Offset inválido: " + offset + "para array de tamanho " + objectCode.length, null);
                continue;
            }

            // Gera o código objeto para a instrução, tratando exceções
            byte[] code;
            try {
                code = generateObjectCode(line, midObject.getSymbolTable());
            } catch (Exception e) {
                SimulatorLogger.logError("Erro gerando código objeto da linha: " + line, e);
                continue;
            }

            // Verifica se cabe no array
            if (offset + code.length > objectCode.length) {
                SimulatorLogger.logError("Código excede tamanho do objectCode. Offset: " + offset, null);
                continue;
            }

            // Copia o código gerado para a posição correta
            System.arraycopy(code, 0, objectCode, offset, code.length);
        }

        SymbolTable symbolTable = midObject.getSymbolTable();
        String programName = midObject.getProgramName();

        SimulatorLogger.logMachineCode("Código objeto gerado para o programa: " + programName);

        // Retorna o objeto com o startAddress (em palavras) e o array de bytes
        return new ObjectFile(startAddress, objectCode, symbolTable, programName, midObject.getRawSourceLines());
    }

    /**
     * Retorna 3 bytes por instrução (formato 3), pode ser ajustado se suportar formato 2 ou 4.
     */
    @SuppressWarnings("SameReturnValue")
    private int getInstructionSize(AssemblyLine line) {
        // DA PRA IMPLEMENTA O DE 4 DPS
        return 3;
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
            // Reservado: gera bytes vazios
            int count = parseNumber(operand) * (mnemonic.equalsIgnoreCase("RESW") ? 3 : 1);
            return new byte[count];
        }

        // Caso contrário, é instrução de formato 3
        return generateInstructionCode(line, symbolTable);
    }

    /**
     * Gera 3 bytes para instruções de formato 3.
     */
    private byte[] generateInstructionCode(AssemblyLine line, SymbolTable symbolTable) {
        String mnemonic = line.mnemonic();
        String operand = line.operand();

        // Detecta se é indexado (verifica se termina com ",X")
        boolean indexed = operand != null && operand.toUpperCase().endsWith(",X");
        String operandString = indexed ? operand.replace(",X", "") : operand;

        // Obtém o opcode a partir do mnemônico
        int opcode = Map.mnemonicToOpcode(mnemonic);

        // Resolve o endereço do operando (retorna em bytes)
        int operandAddress = resolveOperandAddress(operandString, symbolTable);

        // Calcula o deslocamento para PC-relativo
        int disp = calculateDisplacement(line, operandAddress);

        byte[] code = new byte[3];
        // Byte 0: opcode com n=1 e i=1 (ou seja, adiciona 0x03)
        code[0] = (byte) (opcode | 0x03);

        // Byte 1: constrói os flags e os 4 bits altos do deslocamento:
        int secondByte = 0;
        if (indexed) {
            secondByte |= 0x80; // seta o bit x para indexado
        }
        secondByte |= 0x20; // seta o bit p para PC-relativo
        secondByte |= ((disp >> 8) & 0x0F); // insere os 4 bits altos do deslocamento
        code[1] = (byte) secondByte;

        // Byte 2: os 8 bits inferiores do deslocamento
        code[2] = (byte) (disp & 0xFF);

        return code;
    }

    /**
     * Resolve o endereço do operando, retornando valor em bytes.
     * Se for símbolo, multiplica por 3 para converter de palavras para bytes.
     */
    private int resolveOperandAddress(String operand, SymbolTable symbolTable) {
        if (operand == null) return 0;

        // Imediato: ex. "#45"
        if (operand.startsWith("#")) {
            return parseNumber(operand.substring(1));
        }

        // Se for símbolo
        if (symbolTable.contains(operand)) {
            // Os endereços no symbol table estão em palavras, converte para bytes
            return symbolTable.getAddress(operand) * 3;
        }

        // Senão, tenta parsear como número decimal ou hexadecimal
        return parseNumber(operand);
    }

    /**
     * Calcula o deslocamento PC-relativo para o formato 3:
     * PC da próxima instrução = (line.getAddress() * 3) + 3 bytes.
     */
    private int calculateDisplacement(AssemblyLine line, int operandByteAddr) {
        int currentInstructionByteAddr = line.address() * 3;
        int nextInstructionByteAddr = currentInstructionByteAddr + 3;
        int disp = operandByteAddr - nextInstructionByteAddr;
        if (disp < -2048 || disp > 2047) {
            throw new IllegalArgumentException("Deslocamento PC-relativo inválido: " + disp);
        }
        return disp & 0xFFF;
    }

    /**
     * Converte diretiva BYTE no formato X'...' ou C'...'
     */
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

    /**
     * Converte uma string numérica (decimal ou hexadecimal) em int.
     */
    private int parseNumber(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente.");
        }
        if (operand.matches("\\d+")) { // Decimal
            return Integer.parseInt(operand);
        }
        if (operand.matches("[0-9A-Fa-f]+")) { // Hexadecimal
            return Integer.parseInt(operand, 16);
        }
        throw new IllegalArgumentException("Formato inválido de número: " + operand);
    }
}
