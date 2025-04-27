package sicxesimulator.software.assembler;

import sicxesimulator.common.utils.Logger;
import sicxesimulator.common.utils.Convert;
import sicxesimulator.common.utils.Checker;
import sicxesimulator.common.utils.Mapper;
import sicxesimulator.common.utils.FileUtils;
import sicxesimulator.common.utils.Constants;
import sicxesimulator.software.data.AssemblyLine;
import sicxesimulator.software.data.IntermediateRepresentation;
import sicxesimulator.software.util.InstructionSizeCalculator;
import sicxesimulator.software.util.Parser;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.software.data.Symbol;

import java.io.IOException;
import java.util.*;

import static sicxesimulator.software.data.ObjectFile.ObjectFileOrigin.SINGLE_MODULE;

/**
 * Responsável por realizar a segunda passagem do montador SIC/XE.
 * Converte a representação intermediária em código objeto, gera registros
 * Text (T), Modification (M) e escreve o arquivo objeto em formato textual (.obj).
 */
public class AssemblerSecondPass {

    /**
     * Gera o {@link ObjectFile} final a partir da representação intermediária.
     *
     * @param intermediate representação produzida na primeira passagem do montador
     * @return instância de {@link ObjectFile} contendo o código de máquina,
     *         símbolos importados/exportados e metadados para vinculação
     */
    public ObjectFile generateObjectFile(IntermediateRepresentation intermediate) {
        int startAddress = intermediate.startAddress();
        int programSize = intermediate.assemblyLines().stream()
                .mapToInt(this::getInstructionSize)
                .sum();
        byte[] machineCode = new byte[programSize];
        List<ModificationInfo> modificationList = new ArrayList<>();

        SymbolTable symbolTable = intermediate.symbolTable();
        Set<String> importedSymbols = new HashSet<>(intermediate.importedSymbols());
        String programName = intermediate.programName();
        List<String> rawSource = intermediate.rawSourceLines();

        // Preenche o array de bytes e coleta registros M
        for (AssemblyLine line : intermediate.assemblyLines()) {
            int offset = line.address() - startAddress;
            if (offset < 0 || offset >= machineCode.length) {
                Logger.logError("Endereço fora dos limites: " + line.address(), null);
                continue;
            }
            byte[] code = generateObjectCode(line, symbolTable, importedSymbols);
            int len = Math.min(code.length, machineCode.length - offset);
            System.arraycopy(code, 0, machineCode, offset, len);

            if (shouldGenerateMRecord(line)) {
                int fmt = Parser.determineInstructionFormat(line.mnemonic());
                int nibbleLen = (fmt == 3) ? 3 : 5;
                String sym = line.operand().replaceAll("(?i),x", "").trim();
                modificationList.add(new ModificationInfo(offset + 1, nibbleLen, "+" + sym));
            }
        }

        // Cria o ObjectFile binário (.meta)
        ObjectFile objFile = new ObjectFile(
                startAddress,
                machineCode,
                symbolTable,
                programName,
                rawSource,
                importedSymbols,
                Collections.emptyList()
        );
        objFile.setOrigin(SINGLE_MODULE);

        // Escreve o .obj textual
        try {
            writeTextualObjectFile(
                    programName + ".obj",
                    programName,
                    startAddress,
                    machineCode,
                    modificationList,
                    getExportedSymbols(symbolTable),
                    importedSymbols
            );
        } catch (IOException e) {
            Logger.logError("Erro ao gravar arquivo .obj", e);
        }
        return objFile;
    }

    /**
     * Extrai a lista de símbolos públicos (exportados) da tabela de símbolos.
     *
     * @param table tabela de símbolos com definições locais e globais
     * @return lista de {@link Symbol} cuja flag isPublic está definida
     */
    private List<Symbol> getExportedSymbols(SymbolTable table) {
        List<Symbol> list = new ArrayList<>();
        for (Symbol sym : table.getAllSymbols().values()) {
            if (sym.isPublic) {
                list.add(sym);
            }
        }
        return list;
    }

    /**
     * Gera o código objeto (bytes) para uma única linha de assembly,
     * tratando diretivas e instruções nos formatos 1–4.
     *
     * @param line       linha de assembly contendo mnemônico, operando e endereço
     * @param table      tabela de símbolos para resolução de endereços
     * @param imported   conjunto de símbolos importados (externos ao módulo)
     * @return array de bytes representando o opcode e operandos montados
     * @throws IllegalArgumentException se o formato ou operandos forem inválidos
     */
    public byte[] generateObjectCode(AssemblyLine line, SymbolTable table, Set<String> imported) {
        String mnem = line.mnemonic(), op = line.operand();
        if (mnem.equalsIgnoreCase("WORD")) {
            return Convert.intTo3Bytes(Parser.parseNumber(op));
        }
        if (mnem.equalsIgnoreCase("BYTE")) {
            return Parser.parseByteOperand(op);
        }
        if (mnem.equalsIgnoreCase("RESW") || mnem.equalsIgnoreCase("RESB")) {
            int count = Parser.parseNumber(op) * (mnem.equalsIgnoreCase("RESW") ? 3 : 1);
            return new byte[count];
        }
        int fmt = Parser.determineInstructionFormat(mnem);
        return switch (fmt) {
            case 1 -> format1(mnem);
            case 2 -> format2(mnem, op);
            case 3 -> format3(mnem, op, table, line, imported);
            case 4 -> format4(mnem, op, table);
            default -> throw new IllegalArgumentException("Formato desconhecido: " + mnem);
        };
    }

    /**
     * Gera o código objeto para uma linha de assembly sem símbolos importados.
     *
     * @param line  linha de assembly a ser montada
     * @param table tabela de símbolos para resolução de endereços
     * @return array de bytes com o código objeto da instrução
     */
    public byte[] generateObjectCode(AssemblyLine line, SymbolTable table) {
        return generateObjectCode(line, table, Collections.emptySet());
    }

    /**
     * Monta uma instrução de formato 1 (opcode de 1 byte).
     *
     * @param mnem mnemônico da instrução (pode conter '+')
     * @return vetor de 1 byte contendo o opcode
     */
    private byte[] format1(String mnem) {
        int opcode = Mapper.mnemonicToOpcode(mnem.replace("+",""));
        return new byte[]{(byte) opcode};
    }

    /**
     * Monta uma instrução de formato 2 (opcode + registradores).
     *
     * @param mnem mnemônico da instrução
     * @param op   string com dois registradores separados por vírgula
     * @return vetor de 2 bytes [opcode, r1<<4|r2]
     * @throws IllegalArgumentException se não houver exatamente dois registradores
     */
    private byte[] format2(String mnem, String op) {
        int opcode = Mapper.mnemonicToOpcode(mnem);
        String[] regs = op.split(",");
        if (regs.length != 2) throw new IllegalArgumentException("Formato 2 precisa de 2 regs: " + op);
        int r1 = Mapper.registerNameToNumber(regs[0].trim());
        int r2 = Mapper.registerNameToNumber(regs[1].trim());
        return new byte[]{(byte) opcode, (byte) ((r1<<4)|(r2&0xF))};
    }

    /**
     * Monta uma instrução de formato 3 (PC-relativo ou imediato),
     * ajustando bits n, i, x, p conforme o operando.
     *
     * @param mnem     mnemônico da instrução (sem '+')
     * @param operand  operando, que pode indicar imediato ('#') ou indexação (',X')
     * @param table    tabela de símbolos para lookup de endereços
     * @param line     metadados da linha (endereço atual)
     * @param imported símbolos importados para detectar referências externas
     * @return vetor de 3 bytes representando a instrução formatada
     * @throws IllegalArgumentException se o deslocamento ou literal estiverem fora de alcance
     */
    private byte[] format3(String mnem, String operand, SymbolTable table, AssemblyLine line, Set<String> imported) {
        String opStr = operand==null?"":operand.trim();
        boolean idx = opStr.toUpperCase().endsWith(",X");
        if (idx) opStr = opStr.substring(0, opStr.length()-2).trim();
        boolean imm = opStr.startsWith("#");
        int opcode = Mapper.mnemonicToOpcode(mnem);
        int n=1, i=1, disp;
        if (imm) {
            n=0;
            int lit = Parser.parseNumber(opStr.substring(1));
            if (lit<0||lit>0xFFF) throw new IllegalArgumentException("Literal fora de 12 bits: "+lit);
            disp = lit;
        } else {
            if (opStr.isEmpty()||imported.contains(opStr.toUpperCase())) disp=0;
            else {
                int addr = Parser.resolveOperandAddress(opStr, table);
                disp = calculateDisplacement(line, addr);
            }
        }
        byte b1 = (byte)((opcode&0xFC)|(n<<1)|i);
        byte b2 = (byte)((idx?0x80:0)|(imm?0:0x20)|((disp>>>8)&0xF));
        byte b3 = (byte)(disp&0xFF);
        return new byte[]{b1,b2,b3};
    }

    /**
     * Monta uma instrução de formato 4 (endereçamento direto de 20 bits),
     * incluindo o bit e=1 e indexação se aplicável.
     *
     * @param mnem    mnemônico da instrução (prefixado com '+')
     * @param operand operando que pode terminar com ',X' para indexação
     * @param table   tabela de símbolos para resolver endereços absolutos
     * @return vetor de 4 bytes representando a instrução de formato 4
     */
    private byte[] format4(String mnem, String operand, SymbolTable table) {
        String m = mnem.replace("+","");
        int opcode = Mapper.mnemonicToOpcode(m);
        String opStr = operand==null?"":operand.trim();
        boolean idx = opStr.toUpperCase().endsWith(",X");
        if (idx) opStr = opStr.substring(0,opStr.length()-2).trim();
        int addr = Parser.resolveOperandAddress(opStr, table);
        byte b1 = (byte)((opcode&0xFC)|0x3);
        byte b2 = (byte)((idx?0x80:0)|0x10|((addr>>16)&0xF));
        return new byte[]{ b1, b2, (byte)((addr>>8)&0xFF), (byte)(addr&0xFF) };
    }

    /**
     * Escreve o arquivo objeto em formato textual (.obj), incluindo:
     * registros H, D, R, T, M e E, conforme o layout SIC/XE.
     *
     * @param fileName nome do arquivo de saída (com extensão .obj)
     * @param progName nome do programa (para o header H)
     * @param start    endereço inicial do programa (record E e T)
     * @param code     array de bytes do código de máquina completo
     * @param mlist    lista de registros de modificação (M-records)
     * @param exports  símbolos exportados para o D-record
     * @param imports  símbolos importados para o R-record
     * @throws IOException em caso de falha ao gravar o arquivo em disco
     */
    private void writeTextualObjectFile(String fileName, String progName, int start, byte[] code, List<ModificationInfo> mlist, List<Symbol> exports, Set<String> imports) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(String.format("H^%-6s^%06X^%06X%n", fitName(progName), start, code.length));
        // D-records
        if (!exports.isEmpty()) {
            sb.append("D");
            for (Symbol s: exports) {
                sb.append("^").append(s.name)
                        .append("^").append(String.format("%06X", s.address));
            }
            sb.append("\n");
        }
        // R-records
        if (!imports.isEmpty()) {
            sb.append("R");
            for (String imp: imports) sb.append("^").append(imp);
            sb.append("\n");
        }
        // T-records
        for (TBlock blk: buildTextRecords(start, code)) {
            StringBuilder hex = new StringBuilder();
            for (byte b: blk.data) hex.append(String.format("%02X", b));
            sb.append(String.format("T^%06X^%02X^%s%n", blk.startAddr, blk.data.size(), hex));
        }
        // M-records
        for (ModificationInfo mi: mlist) {
            sb.append(String.format("M^%06X^%02X^%s%n",
                    start + mi.offset, mi.lengthInHalfBytes, mi.reference));
        }
        // End record
        sb.append(String.format("E^%06X%n", start));

        FileUtils.writeFileInDir(Constants.SAVE_DIR, fileName, sb.toString());
    }

    /**
     * Agrupa o array de bytes do programa em blocos de até 30 bytes
     * para composição dos registros T.
     *
     * @param start endereço base do primeiro byte de cada bloco
     * @param code  vetor completo de bytes do código objeto
     * @return lista de blocos {@link TBlock} prontos para gerar T-records
     */
    private List<TBlock> buildTextRecords(int start, byte[] code) {
        final int MAX = 30;
        List<TBlock> lst = new ArrayList<>();
        for (int i=0; i<code.length; i+=MAX) {
            int len = Math.min(MAX, code.length - i);
            TBlock b = new TBlock(start + i);
            for (int j=0; j<len; j++) b.data.add(code[i+j]);
            lst.add(b);
        }
        return lst;
    }

    /**
     * Ajusta um nome de programa para ter no máximo 6 caracteres,
     * truncando ou substituindo por 'NONAME' em caso de nulo.
     *
     * @param n nome original do programa
     * @return nome ajustado para até 6 caracteres
     */
    private String fitName(String n) {
        if (n==null) return "NONAME";
        return n.length()>6? n.substring(0,6): n;
    }

    /**
     * Determina se uma linha de assembly deve gerar
     * um registro de modificação (M-record) no .obj.
     *
     * @param line linha de código analisada
     * @return true se for instrução formato 3 ou 4 e referenciar símbolo externo
     */
    private boolean shouldGenerateMRecord(AssemblyLine line) {
        int fmt = Parser.determineInstructionFormat(line.mnemonic());
        String op = line.operand();
        if ((fmt!=3 && fmt!=4) || op==null || op.trim().isEmpty() || op.startsWith("#"))
            return false;
        return !Checker.isNumericLiteral(op.replaceAll("(?i),x","").trim());
    }

    /**
     * Calcula o tamanho em bytes de uma instrução ou diretiva,
     * conforme definido em {@link InstructionSizeCalculator}.
     *
     * @param line linha de código com mnemônico e operando
     * @return tamanho em bytes da instrução/diretiva
     */
    private int getInstructionSize(AssemblyLine line) {
        return InstructionSizeCalculator.calculateSize(line.mnemonic(), line.operand());
    }

    /**
     * Calcula o deslocamento PC-relativo (12 bits com sinal)
     * entre a instrução atual e o endereço do operando.
     *
     * @param line linha com o endereço atual da instrução
     * @param addr endereço absoluto do operando
     * @return valor de deslocamento de 12 bits (mascarado)
     * @throws IllegalArgumentException se o deslocamento estiver fora do intervalo [-2048,2047]
     */
    private int calculateDisplacement(AssemblyLine line, int addr) {
        int cur = line.address();
        int next = cur + getInstructionSize(line);
        int disp = addr - next;
        if (disp < -2048 || disp > 2047)
            throw new IllegalArgumentException("Deslocamento inválido: " + disp);
        return disp & 0xFFF;
    }

    /**
     * Registro de modificação para registros M,
     * contendo deslocamento, tamanho em meio-nibbles e referência ao símbolo.
     *
     * @param offset           deslocamento relativo em bytes dentro do código
     * @param lengthInHalfBytes tamanho do campo em "nibbles" (3 para formato 3, 5 para 4)
     * @param reference        símbolo referenciado (prefixado com '+' se PC-relativo)
     */
    private record ModificationInfo(int offset, int lengthInHalfBytes, String reference) {}

    /**
     * Bloco de dados para registros T no arquivo objeto,
     * contendo endereço inicial e bytes do bloco.
     */
    private static class TBlock {
        /** Endereço do primeiro byte do bloco de texto. */
        final int startAddr;
        /** Lista de bytes a serem emitidos no T-record. */
        final List<Byte> data = new ArrayList<>();

        /**
         * Cria um novo bloco de texto para registros T.
         *
         * @param startAddr endereço do primeiro byte do bloco
         */
        TBlock(int startAddr) {
            this.startAddr = startAddr;
        }
    }

}
