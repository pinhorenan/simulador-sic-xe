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

    /**
     * Gera o código objeto a partir da IntermediateRepresentation.
     * Cada instrução é assumida em formato 3 (3 bytes),
     * e line.getAddress() está em palavras (1 palavra = 3 bytes).
     *
     * @param midObject Representação intermediária gerada pela primeira passagem.
     * @return ObjectFile contendo o endereço inicial e o código objeto.
     */
    public ObjectFile generateObjectFile(IntermediateRepresentation midObject) {
        // Endereço inicial em palavras
        int startAddress = midObject.getStartAddress();

        // Calcula o tamanho total em bytes, somando 3 bytes por instrução (formato 3)
        int programBytes = midObject.getAssemblyLines()
                .stream()
                .mapToInt(this::getInstructionSize)
                .sum();

        byte[] objectCode = new byte[programBytes];

        for (AssemblyLine line : midObject.getAssemblyLines()) {
            // Converte line.getAddress() (palavras) para bytes e subtrai startAddress (também em palavras):
            int offset = (line.getAddress() - startAddress) * 3;

            // Verifica se offset é válido no array
            if (offset < 0 || offset >= objectCode.length) {
                logger.severe("Offset inválido: " + offset + " para array de tamanho " + objectCode.length);
                continue;
            }

            // Gera o código objeto para a instrução
            byte[] code = generateObjectCode(line, midObject.getSymbolTable());

            // Verifica se cabe no array
            if (offset + code.length > objectCode.length) {
                logger.severe("Código excede tamanho do objectCode. Offset: " + offset);
                continue;
            }

            // Copia o código gerado para a posição correta
            System.arraycopy(code, 0, objectCode, offset, code.length);
        }

        // Retorna o objeto com o startAddress (em palavras) e o array de bytes
        return new ObjectFile(startAddress, objectCode, midObject.getSymbolTable());
    }

    /**
     * Retorna 3 bytes por instrução (formato 3), pode ser ajustado se suportar formato 2 ou 4.
     */
    private int getInstructionSize(AssemblyLine line) {
        // Você poderia refinar caso houvesse suporte a formato 1/2/4
        return 3;
    }

    /**
     * Gera o código objeto para uma única linha de assembly.
     */
    public byte[] generateObjectCode(AssemblyLine line, SymbolTable symbolTable) {
        String mnemonic = line.getMnemonic();
        String operand = line.getOperand();

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
        String mnemonic = line.getMnemonic();
        String operand = line.getOperand();

        // Detecta se é indexado (usaremos bit 7 do segundo byte)
        boolean indexed = operand != null && operand.toUpperCase().endsWith(",X");

        // Remove ",X" para resolver o símbolo / número corretamente
        String operandString = indexed ? operand.replace(",X", "") : operand;

        // Busca o opcode da instrução
        int opcode = OpcodeMapper.getOpcode(mnemonic);

        // Resolve o endereço do operando em bytes
        int operandAddress = resolveOperandAddress(operandString, symbolTable);

        // Calcula o deslocamento PC-relativo
        int disp = calculateDisplacement(line, operandAddress);

        // Monta os 3 bytes
        // Byte 0: opcode (6 bits) + n=1,i=1 (2 bits) => normal: opcode | 0x03
        byte[] code = new byte[3];
        code[0] = (byte) (opcode | 0x03); // n=1, i=1 fixo

        // Byte 1: bit de index (7) + high nibble do disp (bits 8-11)
        int highNibble = (disp >> 8) & 0x0F;
        if (indexed) {
            // Seta bit 7 do byte 1
            highNibble |= 0x80;
        }
        code[1] = (byte) highNibble;

        // Byte 2: bits 0-7 do disp
        code[2] = (byte) (disp & 0xFF);

        return code;
    }

    /**
     * Resolve o endereço do operando, retornando valor em bytes.
     * Se for símbolo, multiplica por 3 para converter de palavras para bytes.
     */
    private int resolveOperandAddress(String operand, SymbolTable symbolTable) {
        if (operand == null) return 0;

        // Imediato (#45)
        if (operand.startsWith("#")) {
            return parseNumber(operand.substring(1));
        }

        // Se for símbolo
        if (symbolTable.contains(operand)) {
            // line.getAddress() e symbolTable.getAddress() armazenam endereços em palavras
            // Precisamos de bytes => multiply por 3
            return symbolTable.getAddress(operand) * 3;
        }

        // Senão, parsea como decimal ou hex
        return parseNumber(operand);
    }

    /**
     * Calcula o deslocamento PC-relativo para formato 3:
     * PC da próxima instrução = (line.getAddress() * 3) + 3 bytes
     */
    private int calculateDisplacement(AssemblyLine line, int operandByteAddr) {
        int currentInstructionByteAddr = line.getAddress() * 3;
        int nextInstructionByteAddr = currentInstructionByteAddr + 3; // 3 bytes da instrução

        int disp = operandByteAddr - nextInstructionByteAddr;

        // Verifica alcance de 12 bits (signed)
        if (disp < -2048 || disp > 2047) {
            throw new IllegalArgumentException("Deslocamento PC-relativo inválido: " + disp);
        }
        // Mantém 12 bits
        return disp & 0xFFF;
    }

    /**
     * Converte diretiva BYTE no caso X'...' ou C'...'
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
     * Converte string numérica (decimal ou hex) em int.
     */
    private int parseNumber(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente.");
        }
        // Decimal
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        }
        // Hex
        if (operand.matches("[0-9A-Fa-f]+")) {
            return Integer.parseInt(operand, 16);
        }
        throw new IllegalArgumentException("Formato inválido de número: " + operand);
    }

    /**
     * Reinicializa estado, se houver.
     */
    public void reset() {
        logger.info("Resetando SecondPassProcessor.");
    }
}
