package sicxesimulator.software.assembler;

import sicxesimulator.software.assembler.data.AssemblyLine;
import sicxesimulator.software.assembler.data.IntermediateRepresentation;
import sicxesimulator.software.assembler.util.InstructionSizeCalculator;
import sicxesimulator.software.assembler.util.Parser;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.Symbol;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.utils.*;

import java.io.IOException;
import java.util.*;

import static sicxesimulator.software.data.ObjectFile.ObjectFileOrigin.SINGLE_MODULE;

/**
 * Responsável por realizar a segunda passagem do montador SIC/XE.
 *
 * Converte a representação intermediária (gerada pela primeira passagem)
 * em um objeto final com código de máquina, símbolos exportados/importados,
 * e metadados para vinculação.
 */
public class AssemblerSecondPass {

    /**
     * Gera o objeto final a partir da representação intermediária.
     *
     * @param midObject Representação intermediária contendo as informações montadas.
     * @return Objeto {@link ObjectFile} contendo o código de máquina, símbolos e registros.
     */
    public ObjectFile generateObjectFile(IntermediateRepresentation midObject) {
        int startAddress = midObject.startAddress();
        int programSize = midObject.assemblyLines().stream().mapToInt(this::getInstructionSize).sum();
        byte[] machineCode = new byte[programSize];
        List<ModificationInfo> modificationList = new ArrayList<>();

        SymbolTable symbolTable = midObject.symbolTable();
        Set<String> importedSymbols = new HashSet<>(midObject.importedSymbols());
        String programName = midObject.programName();
        List<String> rawSource = midObject.rawSourceLines();

        // Preenche o array de bytes com o código objeto e gera os M records quando necessário
        for (AssemblyLine line : midObject.assemblyLines()) {
            int lineOffset = line.address() - startAddress;
            if (lineOffset < 0 || lineOffset >= machineCode.length) {
                Logger.logError("Fora dos limites de endereco: " + line.address(), null);
                continue;
            }

            byte[] code = generateObjectCode(line, symbolTable, importedSymbols);
            if (lineOffset + code.length <= machineCode.length) {
                System.arraycopy(code, 0, machineCode, lineOffset, code.length);
            }

            if (shouldGenerateMRecord(line)) {
                int format = Parser.determineInstructionFormat(line.mnemonic());
                int nibbleLen = (format == 3) ? 3 : 5;
                int mOffset = lineOffset + 1; // O campo de endereço geralmente inicia após o opcode
                String operand = line.operand();
                // Aqui, a referência é o símbolo (com o sinal +, pois é PC-relativo)
                String sym = operand.replaceAll("(?i),x", "").trim();
                modificationList.add(new ModificationInfo(mOffset, nibbleLen, "+" + sym));
            }
        }

        Set<String> imported = midObject.importedSymbols();
        List<Symbol> exportedList = new ArrayList<>();
        for (Symbol symbol : symbolTable.getAllSymbols().values()) {
            if (symbol.isPublic) {
                exportedList.add(symbol);
            }
        }

        ObjectFile metaFile = new ObjectFile(
                startAddress,
                machineCode,
                symbolTable,
                programName,
                rawSource,
                imported,
                Collections.emptyList()
        );
        metaFile.setOrigin(SINGLE_MODULE);

        try {
            writeTextualObjectFile(programName + ".obj", programName, startAddress, machineCode, modificationList, exportedList, imported);
        } catch (Exception e) {
            Logger.logError("Erro ao gravar arquivo .obj", e);
        }
        return metaFile;
    }

    /**
     * Constrói os registros de texto (T) para o arquivo objeto.
     *
     * @param start Endereço inicial do programa.
     * @param code Array de bytes do código de máquina.
     * @return Lista de blocos {@link TBlock} com dados agrupados.
     */
    private List<TBlock> buildTextRecords(int start, byte[] code) {
        List<TBlock> blocks = new ArrayList<>();
        final int MAX_BYTES = 30;
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

    /**
     * Ajusta o nome do programa para 6 caracteres, truncando se necessário.
     *
     * @param name Nome original do programa.
     * @return Nome com até 6 caracteres.
     */
    private String fitProgramName(String name) {
        if (name == null) return "NONAME";
        return (name.length() > 6) ? name.substring(0, 6) : name;
    }

    /**
     * Gera o código de máquina para uma linha de assembly.
     *
     * @param line Linha a ser convertida.
     * @param symbolTable Tabela de símbolos.
     * @param importedSymbols Conjunto de símbolos importados (externos).
     * @return Array de bytes representando o código objeto.
     */
    public byte[] generateObjectCode(AssemblyLine line, SymbolTable symbolTable, Set<String> importedSymbols) {
        String mnemonic = line.mnemonic();
        String operand = line.operand();

        // Diretivas WORD, BYTE, RESW e RESB
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return Convert.intTo3Bytes(Parser.parseNumber(operand));
        } else if (mnemonic.equalsIgnoreCase("BYTE")) {
            return Parser.parseByteOperand(operand);
        } else if (mnemonic.equalsIgnoreCase("RESW") || mnemonic.equalsIgnoreCase("RESB")) {
            int count = Parser.parseNumber(operand) * (mnemonic.equalsIgnoreCase("RESW") ? 3 : 1);
            return new byte[count];
        }

        int format = Parser.determineInstructionFormat(mnemonic);
        return switch (format) {
            case 1 -> generateInstructionCodeFormat1(mnemonic);
            case 2 -> generateInstructionCodeFormat2(mnemonic, operand);
            case 3 -> generateInstructionCodeFormat3(mnemonic, operand, symbolTable, line, importedSymbols);
            case 4 -> generateInstructionCodeFormat4(mnemonic, operand, symbolTable);
            default -> throw new IllegalArgumentException("Formato de instrucao desconhecido para " + mnemonic);
        };
    }

    /**
     * Sobrecarga para testes: gera código objeto sem símbolos importados.
     *
     * @param line Linha de assembly.
     * @param symbolTable Tabela de símbolos.
     * @return Código de máquina correspondente.
     */
    public byte[] generateObjectCode(AssemblyLine line, SymbolTable symbolTable) {
        return generateObjectCode(line, symbolTable, new HashSet<>());
    }

    /**
     * Grava o arquivo de saída em formato textual (.obj).
     *
     * @param outFileName Nome do arquivo de saída.
     * @param programName Nome do programa.
     * @param startAddress Endereço inicial do programa.
     * @param code Código objeto.
     * @param modificationList Lista de registros de modificação (M).
     * @param exportedList Lista de símbolos exportados.
     * @param imported Conjunto de símbolos importados.
     * @throws IOException Em caso de erro de escrita.
     */
    private void writeTextualObjectFile(String outFileName, String programName, int startAddress, byte[] code, List<ModificationInfo> modificationList, List<Symbol> exportedList, Set<String> imported) throws IOException {
        List<TBlock> tblocks = buildTextRecords(startAddress, code);
        int programLength = code.length;
        StringBuilder content = new StringBuilder();

        // Registro H
        String hRec = String.format("H^%-6s^%06X^%06X", fitProgramName(programName), startAddress, programLength);
        content.append(hRec).append("\n");

        // Registros D
        if (!exportedList.isEmpty()) {
            StringBuilder dRec = new StringBuilder("D");
            for (Symbol symbol : exportedList) {
                dRec.append("^").append(symbol.name)
                        .append("^").append(String.format("%06X", symbol.address));
            }
            content.append(dRec).append("\n");
        }

        // Registros R
        if (!imported.isEmpty()) {
            StringBuilder rRec = new StringBuilder("R");
            for (String symbol : imported) {
                rRec.append("^").append(symbol);
            }
            content.append(rRec).append("\n");
        }

        // Registros T
        for (TBlock block : tblocks) {
            StringBuilder hex = new StringBuilder();
            for (byte b : block.data) {
                hex.append(String.format("%02X", b & 0xFF));
            }
            int length = block.data.size();
            String tRec = String.format("T^%06X^%02X^%s", block.startAddr, length, hex);
            content.append(tRec).append("\n");
        }

        // Registros M
        for (ModificationInfo minfo : modificationList) {
            int address = startAddress + minfo.offset;
            String lenHex = String.format("%02X", minfo.lengthInHalfBytes & 0xFF);
            String mRec = String.format("M^%06X^%s^%s", address, lenHex, minfo.reference);
            content.append(mRec).append("\n");
        }

        // Registro E
        String eRec = String.format("E^%06X", startAddress);
        content.append(eRec).append("\n");

        FileUtils.writeFileInDir(Constants.SAVE_DIR, outFileName, content.toString());
    }

    /**
     * Gera o código para instruções de formato 1. Apenas por detalhe, já que nosso simulador não suporta.
     *
     * @param mnemonic Mnemônico da instrução.
     * @return Byte do opcode.
     */
    private byte[] generateInstructionCodeFormat1(String mnemonic) {
        mnemonic = mnemonic.replace("+", "");
        int opcode = Mapper.mnemonicToOpcode(mnemonic);
        return new byte[]{ (byte) opcode };
    }

    /**
     * Gera o código para instruções de formato 2.
     *
     * @param mnemonic Mnemônico.
     * @param operand Operandos (registradores).
     * @return Array de bytes com opcode e registradores.
     */
    private byte[] generateInstructionCodeFormat2(String mnemonic, String operand) {
        int opcode = Mapper.mnemonicToOpcode(mnemonic);
        String[] regs = operand.split(",");
        if (regs.length != 2) {
            throw new IllegalArgumentException("Instrucao de formato 2 requer 2 registradores: " + operand);
        }
        int r1 = Mapper.registerNameToNumber(regs[0].trim());
        int r2 = Mapper.registerNameToNumber(regs[1].trim());
        return new byte[]{ (byte) opcode, (byte) ((r1 << 4) | (r2 & 0x0F)) };
    }

    /**
     * Gera código para instruções de formato 3, com suporte a indexação e imediato.
     *
     * @param mnemonic Mnemônico da instrução.
     * @param operand Operando.
     * @param symbolTable Tabela de símbolos.
     * @param line Linha original de assembly.
     * @param importedSymbols Conjunto de símbolos importados.
     * @return Bytes da instrução.
     */
    private byte[] generateInstructionCodeFormat3(String mnemonic, String operand, SymbolTable symbolTable, AssemblyLine line, Set<String> importedSymbols) {
        // Se o operando for null, tratamos como string vazia para evitar NPE
        String operandString = (operand == null) ? "" : operand.trim();
        // Verifica se há indexação (caso a string não esteja vazia)
        boolean indexed = !operandString.isEmpty() && operandString.toUpperCase().endsWith(",X");
        if (indexed) {
            // Remove a parte “,X” e elimina espaços em branco
            operandString = operandString.substring(0, operandString.length() - 2).trim();
        }
        // Determina se é imediata
        boolean isImmediate = operandString.startsWith("#");

        int opcode = Mapper.mnemonicToOpcode(mnemonic);
        int nBit, iBit;
        int disp;

        if (isImmediate) {
            // Para imediato, remove o '#' e converte para número
            operandString = operandString.substring(1).trim();
            int literal = Parser.parseNumber(operandString);
            if (literal < 0 || literal > 0xFFF) {
                throw new IllegalArgumentException("Literal imediato fora de 12 bits: " + literal);
            }
            nBit = 0;
            iBit = 1;
            disp = literal;
        } else {
            // Instrução não imediata
            nBit = 1;
            iBit = 1;
            // Se o operando estiver vazio ou for considerado externo, deixa o campo de deslocamento zerado (placeholder)
            if (operandString.isEmpty() || isExternal(operandString, importedSymbols)) {
                disp = 0;
            } else {
                int operandAddress = Parser.resolveOperandAddress(operandString, symbolTable);
                disp = calculateDisplacement(line, operandAddress);
            }
        }

        byte firstByte = (byte) ((opcode & 0xFC) | (nBit << 1) | iBit);
        byte secondByte = 0;
        if (indexed) {
            secondByte |= (byte) 0x80; // bit x = 1
        }
        if (!isImmediate) {
            secondByte |= 0x20; // p = 1 para PC-relativo
        }
        secondByte |= (byte) ((disp >> 8) & 0x0F);
        byte thirdByte = (byte) (disp & 0xFF);

        return new byte[]{ firstByte, secondByte, thirdByte };
    }

    /**
     * Gera código para instruções de formato 4 (endereçamento direto de 20 bits).
     *
     * @param mnemonic Mnemônico.
     * @param operand Operando.
     * @param symbolTable Tabela de símbolos.
     * @return Array de 4 bytes representando a instrução.
     */
    private byte[] generateInstructionCodeFormat4(String mnemonic, String operand, SymbolTable symbolTable) {
        mnemonic = mnemonic.replace("+", "");
        int opcode = Mapper.mnemonicToOpcode(mnemonic);
        String operandString = (operand == null) ? "" : operand.trim();
        boolean indexed = !operandString.isEmpty() && operandString.toUpperCase().endsWith(",X");
        if (indexed) {
            operandString = operandString.substring(0, operandString.length() - 2).trim();
        }
        int operandAddress = Parser.resolveOperandAddress(operandString, symbolTable);
        int firstByte = (opcode & 0xFC) | 0x03;
        int secondByte = (indexed ? 0x80 : 0) | 0x10 | ((operandAddress >> 16) & 0x0F);
        return new byte[]{
                (byte) firstByte,
                (byte) secondByte,
                (byte) ((operandAddress >> 8) & 0xFF),
                (byte) (operandAddress & 0xFF)
        };
    }

    /**
     * Verifica se o operando refere-se a um símbolo externo.
     *
     * @param operandString Nome do operando.
     * @param importedSymbols Conjunto de símbolos importados.
     * @return true se é externo; false caso contrário.
     */
    private boolean isExternal(String operandString, Set<String> importedSymbols) {
        String sym = operandString.toUpperCase();
        return importedSymbols.contains(sym);
    }

    /**
     * Determina se a linha de instrução precisa gerar um registro de modificação.
     *
     * @param line Linha a ser analisada.
     * @return true se deve gerar M record; false caso contrário.
     */
    private boolean shouldGenerateMRecord(AssemblyLine line) {
        int format = Parser.determineInstructionFormat(line.mnemonic());
        if (format != 3 && format != 4) {
            return false;
        }
        String op = line.operand();
        if (op == null || op.trim().isEmpty() || op.startsWith("#")) {
            return false;
        }
        String cleaned = op.replaceAll("(?i),x", "").trim();
        // Geramos o M record se o operando não for um literal numérico
        return !Checker.isNumericLiteral(cleaned);
    }

    /**
     * Retorna o tamanho da instrução com base no mnemônico e operando.
     *
     * @param line Linha de assembly.
     * @return Tamanho em bytes.
     */
    private int getInstructionSize(AssemblyLine line) {
        return InstructionSizeCalculator.calculateSize(line.mnemonic(), line.operand());
    }

    /**
     * Calcula o deslocamento PC-relativo entre a instrução e o operando.
     *
     * @param line Linha atual.
     * @param operandByteAddr Endereço do operando.
     * @return Deslocamento em 12 bits.
     */
    private int calculateDisplacement(AssemblyLine line, int operandByteAddr) {
        int currentInstructionByteAddr = line.address();
        int nextInstructionByteAddr = currentInstructionByteAddr + getInstructionSize(line);
        int disp = operandByteAddr - nextInstructionByteAddr;
        if (disp < -2048 || disp > 2047) {
            throw new IllegalArgumentException("Deslocamento PC-relativo invalido: " + disp);
        }
        return disp & 0xFFF;
    }

    /**
     * Informações de modificação para registros M do arquivo objeto.
     *
     * @param offset Posição relativa no código.
     * @param lengthInHalfBytes Tamanho do campo em "nibbles".
     * @param reference Nome do símbolo de referência (com sinal).
     */
    record ModificationInfo(int offset, int lengthInHalfBytes, String reference) {}

    /**
     * Bloco de dados usado para registros T (text) no arquivo objeto.
     */
    static class TBlock {
        int startAddr;
        List<Byte> data = new ArrayList<>();
        TBlock(int start) {
            this.startAddr = start;
        }
    }
}
