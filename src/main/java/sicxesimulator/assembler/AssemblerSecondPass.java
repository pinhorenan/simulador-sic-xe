package sicxesimulator.assembler;

import sicxesimulator.models.*;
import sicxesimulator.utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AssemblerSecondPass {
    /**
     * Gera o código objeto a partir da IntermediateRepresentation.
     * Considera que o LC na IR está em bytes.
     *
     * @param midObject Representação intermediária gerada pela primeira passagem.
     * @return ObjectFile contendo o endereço inicial e o código objeto (.meta),
     *         além de gerar o arquivo textual .obj com H/T/M/E.
     */
    protected ObjectFile generateObjectFile(IntermediateRepresentation midObject) {
        // 1) Calcula o array de bytes (machineCode) normal
        int startAddress = midObject.getStartAddress();
        int programSize = midObject.getAssemblyLines()
                .stream()
                .mapToInt(this::getInstructionSize)
                .sum();

        byte[] machineCode = new byte[programSize];

        // Salva também os offsets e símbolos para gerar M records
        List<ModificationInfo> modificationList = new ArrayList<>();

        SymbolTable symbolTable = midObject.getSymbolTable();
        String programName = midObject.getProgramName();
        List<String> rawSource = midObject.getRawSourceLines();

        // Preenche machineCode
        for (AssemblyLine line : midObject.getAssemblyLines()) {
            int lineOffset = line.address() - startAddress;
            if (lineOffset < 0 || lineOffset >= machineCode.length) {
                SimulatorLogger.logError("Offset inválido: " + lineOffset, null);
                continue;
            }

            // Gera os bytes da instrução/diretiva
            byte[] code = generateObjectCode(line, symbolTable);
            if (lineOffset + code.length <= machineCode.length) {
                System.arraycopy(code, 0, machineCode, lineOffset, code.length);
            }

            // Verifica se precisamos de M record
            if (shouldGenerateMRecord(line, symbolTable)) {
                // form3 => 3 nibbles; form4 => 5 nibbles
                int format = determineInstructionFormat(line.mnemonic());
                int nibbleLen = (format == 3) ? 3 : 5;

                // offset local do campo de endereço (geralmente +1 do opcode)
                int mOffset = lineOffset + 1; // Corrigido: era "offset + 1" antes

                // remove ",X" se houver
                String operand = line.operand();
                String sym = operand.replaceAll("(?i),x", "");

                // Gera o ModificationInfo => assumimos +SYM
                modificationList.add(new ModificationInfo(mOffset, nibbleLen, "+" + sym));
            }
        }

        // Recupera as imports
        Set<String> imported = midObject.getImportedSymbols();

        // Também gera a lista de símbolos “públicos” a partir do symbolTable
        List<SymbolTable.SymbolInfo> exportedList = new ArrayList<>();
        for (SymbolTable.SymbolInfo sinfo : symbolTable.getAllSymbols().values()) {
            if (sinfo.isPublic) {
                exportedList.add(sinfo);
            }
        }

        // 2) Cria um ObjectFile binário (".meta") com informações úteis para a interface
        ObjectFile metaFile = new ObjectFile(
                startAddress,
                machineCode,
                symbolTable,
                programName,
                rawSource,
                imported,
                Collections.emptyList()
        );

        // TODO: Realocar isso aqui
        metaFile.setOrigin(ObjectFileOrigin.SINGLE_MODULE);

        // 3) Gera .obj textual (com T Records fragmentados em 30 bytes cada, M records, etc.)
        try {
            writeTextualHTME(programName + ".obj", programName, startAddress, machineCode, modificationList, exportedList, imported);
        } catch (Exception e) {
            SimulatorLogger.logError("Falha ao gravar arquivo .obj textual: " + e.getMessage(), null);
        }

        // 4) Retorna o metaFile binário
        return metaFile;
    }

    /**
     * Decide se deve gerar M record para a instrução (Formato 3/4 e não-imediato, e operand é símbolo).
     */
    private boolean shouldGenerateMRecord(AssemblyLine line, SymbolTable symtab) {
        // 1) Se não for formato 3 ou 4 => return false
        int format = determineInstructionFormat(line.mnemonic());
        if (format != 3 && format != 4) {
            return false;
        }

        // 2) Se operand for nulo ou imediato (#), return false
        String op = line.operand();
        if (op == null || op.startsWith("#")) {
            return false;
        }

        // 3) Remover sufixo ,X
        String cleaned = op.replaceAll("(?i),x", "").trim();

        // 4) Se cleaned for literal numérico => return false
        if (isNumericLiteral(cleaned)) {
            return false;
        }

        // 5) Se SymbolTable contém cleaned => true, caso contrário false
        return symtab.contains(cleaned);
    }

    // Verifica se s é um literal numérico (hexa ou decimal)
    private boolean isNumericLiteral(String s) {
        if (s.matches("[0-9A-Fa-f]+")) {
            return true; // interpretamos como hexa/decimal
        }
        return s.matches("\\d+");
    }

    // TODO: Mover esse record para fora daqui.
    /**
     * @param offset    offset no array de bytes
     * @param reference ex: "+FOO" ou "-FOO"
     */ // Classe auxiliar para armazenar informações de reloc
        record ModificationInfo(int offset, int lengthInHalfBytes, String reference) {
    }

    /**
     * Gera um .obj textual no estilo SIC/XE:
     *  - Header (H)
     *  - T (blocos de até 30 bytes)
     *  - M (para cada reloc)
     *  - E (fim)
     */
    private void writeTextualHTME(String outFileName, String programName, int startAddress, byte[] code, List<ModificationInfo> modificationList, List<SymbolTable.SymbolInfo> exportedList, Set<String> imported) throws IOException {
        // 1) Dividir em blocos de ~30 bytes (0x1E)
        List<TBlock> tblocks = buildTextRecords(startAddress, code);
        int programLength = code.length;

        StringBuilder content = new StringBuilder();


        // Ordem de registro: H -> D -> R -> T -> M -> E

        // === H record
        String header = String.format("H^%-6s^%06X^%06X",
                fitProgramName(programName),
                startAddress,
                programLength
        );
        content.append(header).append("\n");

        // === D records (EXTDEF)
        if (!exportedList.isEmpty()) {
            StringBuilder dRec = new StringBuilder("D");
            for (SymbolTable.SymbolInfo sinfo : exportedList) {
                    dRec.append("^").append(sinfo.name)
                            .append("^").append(String.format("%06X", sinfo.address));
                }
            content.append(dRec).append("\n");
        }

        // === R records (EXTREF)
        if (!imported.isEmpty()) {
            StringBuilder rRec = new StringBuilder("R");
            for (String symbol : imported) {
                rRec.append("^").append(symbol);
            }
            content.append(rRec).append("\n");
        }

        // === T records (até 30 bytes cada)
        for (TBlock block : tblocks) {
            StringBuilder hex = new StringBuilder();
            for (byte b : block.data) {
                hex.append(String.format("%02X", b & 0xFF));
            }
            int length = block.data.size();
            String textRec = String.format("T^%06X^%02X^%s",
                    block.startAddr,
                    length,
                    hex
            );
            content.append(textRec).append("\n");
        }

        // === M records
        for (ModificationInfo minfo : modificationList) {
            int address = startAddress + minfo.offset;
            // length em 2 dígitos hex
            String lenHex = String.format("%02X", minfo.lengthInHalfBytes & 0xFF);
            String mRecord = String.format("M^%06X^%s^%s",
                    address,
                    lenHex,
                    minfo.reference
            );
            content.append(mRecord).append("\n");
        }

        // === E record
        String endRec = String.format("E^%06X", startAddress);
        content.append(endRec).append("\n");

        // Salva o conteúdo acumulado num .obj
        FileUtils.writeFileInDir(Constants.SAVE_DIR, outFileName, content.toString());
    }

    /**
     * Divide o array de bytes em blocos de até 30 bytes, gerando TBlock (startAddr + list<Byte>).
     */
    private List<TBlock> buildTextRecords(int start, byte[] code) {
        List<TBlock> blocks = new ArrayList<>();
        final int MAX_BYTES = 0x1E; // 30 decimal

        int index = 0;
        while (index < code.length) {
            int blockLen = Math.min(MAX_BYTES, code.length - index);
            TBlock block = new TBlock(start + index);
            for (int i = 0; i < blockLen; i++) {
                block.data.add(code[index + i]);
            }
            blocks.add(block);
            index += blockLen;
        }
        return blocks;
    }

    static class TBlock {
        int startAddr;
        List<Byte> data = new ArrayList<>();

        TBlock(int start) {
            this.startAddr = start;
        }
    }

    // Ajusta o nome do programa para caber em 6 chars
    private String fitProgramName(String name) {
        if (name == null) return "NONAME";
        if (name.length() > 6) return name.substring(0, 6);
        return name;
    }

    // Determina o tamanho (em bytes) de cada linha de assembly
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
        boolean indexed = (operand != null && operand.toUpperCase().endsWith(",X"));
        // Remover sufixo ,X se houver
        String operandString = indexed ? operand.replace(",X", "") : operand;

        // Detecta modo imediato (#)
        boolean isImmediate = (operandString != null && operandString.startsWith("#"));

        // Remove '#' do começo, para resolver o endereço / parseNumber
        if (isImmediate) {
            operandString = operandString.substring(1);
        }

        int opcode = Map.mnemonicToOpcode(mnemonic);

        // Resolve address (ou parseNumber) – se for imediato, parseNumber do valor
        int operandAddress = resolveOperandAddress(operandString, symbolTable);

        // Calcula displacement PC-relativo
        int disp = calculateDisplacement(line, operandAddress);

        // Montagem do byte[3]
        // Bits n e i:
        // - immediate => n=0, i=1
        // - caso contrário => n=1, i=1 (endereçamento direto)
        int nBit = isImmediate ? 0 : 1;
        int iBit = 1;
        // opcode (6 bits altos) + nBit e iBit
        byte firstByte = (byte)((opcode & 0xFC) | (nBit << 1) | iBit);

        byte secondByte = 0;
        if (indexed) {
            secondByte |= 0x80; // x=1
        }
        secondByte |= 0x20;    // p=1 (PC-relativo)
        // e=0 no formato 3
        // guardamos os 4 bits altos do disp
        secondByte |= ((disp >> 8) & 0x0F);

        byte[] code = new byte[3];
        code[0] = firstByte;
        code[1] = secondByte;
        code[2] = (byte)(disp & 0xFF);

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
        secondByte |= 0x10; // seta e=1; b e p = 0
        int high4 = (operandAddress >> 16) & 0x0F;
        secondByte |= high4;
        byte[] code = new byte[4];
        code[0] = (byte) firstByte;
        code[1] = (byte) secondByte;
        code[2] = (byte) ((operandAddress >> 8) & 0xFF);
        code[3] = (byte) (operandAddress & 0xFF);
        return code;
    }

    /**
     * Resolve o endereço do operando; se for símbolo presente na SymbolTable, retorna seu address.
     * Se for imediato (#), parsea como número. Caso contrário, se for literal decimal/hex, parsea também.
     */
    private int resolveOperandAddress(String operand, SymbolTable symbolTable) {
        if (operand == null) return 0;
        if (operand.startsWith("#")) {
            return parseNumber(operand.substring(1));
        }
        // Força o operando a maiúsculas para garantir consistência
        String symKey = operand.toUpperCase();
        if (symbolTable.contains(symKey)) {
            return symbolTable.getAddress(symKey);
        }
        // Tenta parsear como número (caso não seja símbolo)
        return parseNumber(operand);
    }



    private int calculateDisplacement(AssemblyLine line, int operandByteAddr) {
        int currentInstructionByteAddr = line.address();
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
