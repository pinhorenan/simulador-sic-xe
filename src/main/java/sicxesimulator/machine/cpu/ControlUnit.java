package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;
import java.util.Arrays;

/**
 * A ControlUnit gerencia o ciclo de execução da máquina SIC/XE, realizando as fases de fetch, decode e execute,
 * além de atualizar os registradores e o PC conforme as instruções são executadas.
 */
public class ControlUnit {
    // Referência à memória
    private Memory memory;
    // Conjunto de instruções a serem executadas
    private final InstructionSet instructionSet;

    // Registradores do SIC/XE
    private final Register A, X, L, B, S, T, F;
    private final Register PC;
    private final Register SW;
    private final Register[] registers;

    // Variáveis do ciclo de instrução
    private int baseAddress;       // Endereço onde o programa foi carregado (em bytes)
    private int currentOpcode;     // Opcode atual lido da memória
    private int instructionFormat; // Formato da instrução: 1, 2, 3 ou 4
    private int[] operands;        // Operandos decodificados
    private boolean indexed;       // Modo indexado
    private boolean extended;      // Flag "e" (indica formato 4)

    // Variáveis de controle
    private String lastExecutionLog = "";  // Log da última instrução executada
    private boolean halted;                // Indica se a execução foi encerrada

    /**
     * Construtor da ControlUnit.
     *
     * @param memory Instância de Memory utilizada pela máquina.
     */
    public ControlUnit(Memory memory) {
        this.memory = memory;
        A = new Register("A");
        X = new Register("X");
        L = new Register("L");
        B = new Register("B");
        S = new Register("S");
        T = new Register("T");
        F = new Register("F");
        PC = new Register("PC");
        SW = new Register("SW");
        registers = new Register[]{A, X, L, B, S, T, F, PC, SW};
        // O InstructionSet recebe a memória e a referência desta ControlUnit.
        this.instructionSet = new InstructionSet(memory, this);
        halted = false;
    }

    /**
     * Limpa todos os registradores.
     */
    public void clearRegisters() {
        for (Register r : registers) {
            r.clearRegister();
        }
    }

    /**
     * Retorna uma cópia dos registradores atuais.
     *
     * @return Array com os registradores.
     */
    public Register[] getCurrentRegisters() {
        return Arrays.copyOf(registers, registers.length);
    }

    /**
     * Mapeia um ID de registrador para o objeto Register correspondente.
     * IDs: 0 -> A, 1 -> X, 2 -> L, 3 -> B, 4 -> S, 5 -> T, 6 -> F, 8 -> PC, 9 -> SW.
     *
     * @param id Identificador do registrador.
     * @return Registrador correspondente.
     */
    private Register getRegisterById(int id) {
        return switch (id) {
            case 0 -> A;
            case 1 -> X;
            case 2 -> L;
            case 3 -> B;
            case 4 -> S;
            case 5 -> T;
            case 6 -> F;
            case 8 -> PC;
            case 9 -> SW;
            default -> throw new IllegalArgumentException("ID de registrador inválido: " + id);
        };
    }

    // Métodos para manipulação do PC e endereço base

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public void setPC(int value) {
        PC.setValue(value);
    }

    private void incrementPC(int instructionSize) {
        setPC(PC.getIntValue() + instructionSize);
    }

    // Métodos de manipulação do Condition Code

    private int getConditionCode() {
        return SW.getIntValue() & 0x03;
    }

    private void updateConditionCode(int result) {
        int conditionCode = (result == 0) ? 0 : (result < 0 ? 1 : 2);
        setConditionCode(conditionCode);
    }

    private void setConditionCode(int conditionCode) {
        int currentSW = SW.getIntValue();
        SW.setValue((currentSW & 0xFFFFFC) | (conditionCode & 0x03));
    }

    // ----- CICLO DE INSTRUÇÃO: FETCH, DECODE, EXECUTE -----

    /**
     * Fetch: Lê o opcode da memória no endereço apontado pelo PC.
     */
    public void fetch() {
        currentOpcode = memory.readByte(PC.getIntValue()) & 0xFF;
    }

    /**
     * Decode: Determina o formato da instrução e extrai os operandos.
     * Incrementa o PC conforme o tamanho da instrução decodificada.
     */
    public void decode() {
        instructionFormat = determineInstructionFormat(currentOpcode);
        operands = new int[0];
        indexed = false;
        extended = false;

        switch (instructionFormat) {
            case 1 -> decodeFormat1();
            case 2 -> decodeFormat2();
            case 3, 4 -> decodeFormat3or4();
            default -> throw new IllegalStateException("Formato de instrução inválido: " + instructionFormat);
        }
        incrementPC(getInstructionSize());
    }

    /**
     * Determina o formato da instrução com base no opcode e na flag "e" do segundo byte, se necessário.
     *
     * @param opcode Opcode da instrução.
     * @return Formato da instrução (1, 2, 3 ou 4).
     */
    private int determineInstructionFormat(int opcode) {
        return switch (opcode) {
            case 0x4C -> 3; // RSUB (Formato 3)
            case 0x90, 0x04 -> 2; // ADDR, CLEAR (Formato 2). Nota: 0x04 pode ser LDX se não for formato 2.
            default -> {
                int flags = memory.readByte(PC.getIntValue() + 1) & 0xFF;
                if ((flags & 0x01) != 0) yield 4;
                else yield 3;
            }
        };
    }

    /**
     * Decodifica instruções de formato 1: 1 byte sem operandos.
     */
    private void decodeFormat1() {
        operands = new int[0];
    }

    /**
     * Decodifica instruções de formato 2: 2 bytes, onde os dois nibbles do segundo byte são os operandos.
     */
    private void decodeFormat2() {
        int byte2 = memory.readByte(PC.getIntValue() + 1) & 0xFF;
        operands = new int[]{(byte2 >> 4) & 0xF, byte2 & 0xF};
    }

    /**
     * Decodifica instruções dos formatos 3 e 4.
     * No formato 3, a instrução tem 3 bytes; no formato 4, 4 bytes.
     * O deslocamento ou endereço é extraído dos bytes seguintes ao opcode.
     */
    private void decodeFormat3or4() {
        int byte1 = memory.readByte(PC.getIntValue() + 1) & 0xFF;
        extended = (byte1 & 0x01) != 0; // Flag "e"
        indexed = (byte1 & 0x10) != 0;  // Flag "x"
        int lowNibble = byte1 & 0x0F;
        int byte2 = memory.readByte(PC.getIntValue() + 2) & 0xFF;
        int addressField;
        if (extended) {
            int byte3 = memory.readByte(PC.getIntValue() + 3) & 0xFF;
            addressField = (lowNibble << 16) | (byte2 << 8) | byte3;
        } else {
            addressField = (lowNibble << 8) | byte2;
        }
        operands = new int[]{addressField, byte1};
    }

    /**
     * Retorna o tamanho da instrução atual com base no formato.
     *
     * @return Tamanho da instrução em bytes.
     */
    private int getInstructionSize() {
        return switch (instructionFormat) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            default -> throw new IllegalStateException("Formato inválido");
        };
    }

    // MÉTODOS AUXILIARES (NOVIDADE: isIndexed e getInstructionFormat)

    /**
     * Retorna se a instrução atual utiliza endereçamento indexado.
     *
     * @return true se indexado; false caso contrário.
     */
    public boolean isIndexed() {
        return indexed;
    }

    /**
     * Retorna o formato da instrução atualmente decodificada.
     *
     * @return Formato da instrução (1, 2, 3 ou 4).
     */
    public int getInstructionFormat() {
        return instructionFormat;
    }

    // Métodos de execução

    /**
     * Executa a instrução decodificada, atualizando registradores, PC e memória conforme necessário.
     * Registra um log da operação para fins de depuração.
     */
    public void execute() {
        String logMessage;
        switch (currentOpcode) {
            case 0x18 -> { // ADD
                int operand = operands[0];
                int result = instructionSet.executeADD(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("ADD: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resultado=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x90 -> { // ADDR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeADDR(r1Val, r2Val);
                logMessage = String.format("ADDR: R%d=%04X, R%d=%04X => Resultado=%04X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }
            case 0x40 -> { // AND
                int operand = operands[0];
                int result = instructionSet.executeAND(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("AND: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resultado=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x04 -> { // CLEAR ou LDX
                if (getInstructionFormat() == 2) { // CLEAR
                    int reg = operands[0];
                    int oldVal = getRegisterById(reg).getIntValue();
                    int newVal = instructionSet.executeCLEAR();
                    logMessage = String.format("CLEAR: R%d antigo=%04X => novo=%04X", reg, oldVal, newVal);
                    getRegisterById(reg).setValue(newVal);
                } else { // LDX
                    int operand = operands[0];
                    int result = instructionSet.executeLDX(operand, isIndexed(), X.getIntValue());
                    logMessage = String.format("LDX: X antigo=%04X, Operando=%04X, Indexed=%b => novo X=%04X",
                            X.getIntValue(), operand, isIndexed(), result);
                    X.setValue(result);
                }
            }
            case 0x28 -> { // COMP
                int operand = operands[0];
                int comparison = instructionSet.executeCOMP(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("COMP: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Comparação=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), comparison);
                updateConditionCode(comparison);
            }
            case 0xA0 -> { // COMPR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int comparison = instructionSet.executeCOMPR(r1Val, r2Val);
                logMessage = String.format("COMPR: R%d=%04X, R%d=%04X => Comparação=%04X",
                        regs[0], r1Val, regs[1], r2Val, comparison);
                updateConditionCode(comparison);
            }
            case 0x24 -> { // DIV
                int operand = operands[0];
                int result = instructionSet.executeDIV(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("DIV: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resultado=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x9C -> { // DIVR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeDIVR(r1Val, r2Val);
                logMessage = String.format("DIVR: R%d=%04X, R%d=%04X => Resultado=%04X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }
            case 0x3C -> { // J
                int newPC = instructionSet.executeJ(operands[0], isIndexed(), X.getIntValue());
                logMessage = String.format("J: Pulando para o endereço %04X", newPC);
                PC.setValue(newPC);
            }
            case 0x30 -> { // JEQ
                int newPC = instructionSet.executeCONDITIONAL_JUMP(0, operands[0], getConditionCode());
                if (newPC != -1) {
                    logMessage = String.format("JEQ: Condição satisfeita, pulando para %04X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JEQ: Condição não satisfeita, sem pulo.";
                }
            }
            case 0x34 -> { // JGT
                int newPC = instructionSet.executeCONDITIONAL_JUMP(2, operands[0], getConditionCode());
                if (newPC != -1) {
                    logMessage = String.format("JGT: Condição satisfeita, pulando para %04X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JGT: Condição não satisfeita, sem pulo.";
                }
            }
            case 0x38 -> { // JLT
                int newPC = instructionSet.executeCONDITIONAL_JUMP(1, operands[0], getConditionCode());
                if (newPC != -1) {
                    logMessage = String.format("JLT: Condição satisfeita, pulando para %04X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JLT: Condição não satisfeita, sem pulo.";
                }
            }
            case 0x48 -> { // JSUB
                int returnAddress = PC.getIntValue() + getInstructionSize();
                L.setValue(returnAddress);
                int newPC = instructionSet.executeJSUB(operands[0], isIndexed(), X.getIntValue());
                logMessage = String.format("JSUB: Endereço de retorno = %04X, pulando para %04X", returnAddress, newPC);
                PC.setValue(newPC);
            }
            case 0x00 -> { // LDA
                int operand = operands[0];
                int result = instructionSet.executeLDA(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDA: Carregando do endereço %04X => A = %04X", operand, result);
                A.setValue(result);
            }
            case 0x68 -> { // LDB
                int operand = operands[0];
                int result = instructionSet.executeLDB(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDB: Carregando do endereço %04X => B = %04X", operand, result);
                B.setValue(result);
            }
            case 0x50 -> { // LDCH
                int operand = operands[0];
                int result = instructionSet.executeLDCH(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDCH: Carregando do endereço %04X => A = %04X", operand, result);
                A.setValue(result);
            }
            case 0x08 -> { // LDL
                int operand = operands[0];
                int result = instructionSet.executeLDL(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDL: Carregando do endereço %04X => L = %04X", operand, result);
                L.setValue(result);
            }
            case 0x6C -> { // LDS
                int operand = operands[0];
                int result = instructionSet.executeLDS(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDS: Carregando do endereço %04X => S = %04X", operand, result);
                S.setValue(result);
            }
            case 0x74 -> { // LDT
                int operand = operands[0];
                int result = instructionSet.executeLDT(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDT: Carregando do endereço %04X => T = %04X", operand, result);
                T.setValue(result);
            }
            case 0x20 -> { // MUL
                int operand = operands[0];
                int result = instructionSet.executeMUL(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("MUL: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resultado=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x98 -> { // MULR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeMULR(r1Val, r2Val);
                logMessage = String.format("MULR: R%d=%04X, R%d=%04X => Resultado=%04X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }
            case 0x44 -> { // OR
                int operand = operands[0];
                int result = instructionSet.executeOR(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("OR: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resultado=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0xAC -> { // RMO (Formato 2)
                int[] regs = operands;
                int sourceVal = getRegisterById(regs[0]).getIntValue();
                logMessage = String.format("RMO: Copiando R%d (%04X) para R%d", regs[0], sourceVal, regs[1]);
                getRegisterById(regs[1]).setValue(sourceVal);
            }
            case 0xA4 -> { // SHIFTL (Formato 2)
                int[] op = operands; // [reg, count]
                int reg = op[0];
                int count = op[1];
                int oldVal = getRegisterById(reg).getIntValue();
                int shifted = instructionSet.executeSHIFTL(oldVal, count);
                logMessage = String.format("SHIFTL: R%d: %04X << %d = %04X", reg, oldVal, count, shifted);
                getRegisterById(reg).setValue(shifted);
                updateConditionCode(shifted);
            }
            case 0xA8 -> { // SHIFTR (Formato 2)
                int[] op = operands; // [reg, count]
                int reg = op[0];
                int count = op[1];
                int oldVal = getRegisterById(reg).getIntValue();
                int shifted = instructionSet.executeSHIFTR(oldVal, count);
                logMessage = String.format("SHIFTR: R%d: %04X >> %d = %04X", reg, oldVal, count, shifted);
                getRegisterById(reg).setValue(shifted);
                updateConditionCode(shifted);
            }
            case 0x0C -> { // STA (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                // Verifica alinhamento do endereço
                if (effectiveAddress % 3 != 0) {
                    throw new IllegalArgumentException("Endereço não alinhado para STA: " + effectiveAddress);
                }
                int staValue = instructionSet.executeSTA(A.getIntValue());
                memory.writeWord(effectiveAddress, intTo3Bytes(staValue));
                logMessage = String.format("STA: Escrevendo A (%06X) para memória[%06X]", A.getIntValue(), effectiveAddress);
            }
            case 0x78 -> { // STB (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                if (effectiveAddress % 3 != 0) {
                    throw new IllegalArgumentException("Endereço não alinhado para STB: " + effectiveAddress);
                }
                int stbValue = instructionSet.executeSTB(B.getIntValue());
                memory.writeWord(effectiveAddress, intTo3Bytes(stbValue));
                logMessage = String.format("STB: Escrevendo B (%06X) para memória[%06X]", B.getIntValue(), effectiveAddress);
            }
            case 0x54 -> { // STCH (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                int stchValue = instructionSet.executeSTCH(A.getIntValue());
                memory.writeByte(effectiveAddress, stchValue);
                logMessage = String.format("STCH: Escrevendo byte de A (%02X) para memória[%06X]", stchValue, effectiveAddress);
            }
            case 0x14 -> { // STL (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                if (effectiveAddress % 3 != 0) {
                    throw new IllegalArgumentException("Endereço não alinhado para STL: " + effectiveAddress);
                }
                int stlValue = instructionSet.executeSTL(L.getIntValue());
                memory.writeWord(effectiveAddress, intTo3Bytes(stlValue));
                logMessage = String.format("STL: Escrevendo L (%06X) para memória[%06X]", L.getIntValue(), effectiveAddress);
            }
            case 0x7C -> { // STS (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                if (effectiveAddress % 3 != 0) {
                    throw new IllegalArgumentException("Endereço não alinhado para STS: " + effectiveAddress);
                }
                int stsValue = instructionSet.executeSTS(S.getIntValue());
                memory.writeWord(effectiveAddress, intTo3Bytes(stsValue));
                logMessage = String.format("STS: Escrevendo S (%06X) para memória[%06X]", S.getIntValue(), effectiveAddress);
            }
            case 0x84 -> { // STT (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                if (effectiveAddress % 3 != 0) {
                    throw new IllegalArgumentException("Endereço não alinhado para STT: " + effectiveAddress);
                }
                int sttValue = instructionSet.executeSTT(T.getIntValue());
                memory.writeWord(effectiveAddress, intTo3Bytes(sttValue));
                logMessage = String.format("STT: Escrevendo T (%06X) para memória[%06X]", T.getIntValue(), effectiveAddress);
            }
            case 0x10 -> { // STX (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                if (effectiveAddress % 3 != 0) {
                    throw new IllegalArgumentException("Endereço não alinhado para STX: " + effectiveAddress);
                }
                int stxValue = instructionSet.executeSTX(X.getIntValue());
                memory.writeWord(effectiveAddress, intTo3Bytes(stxValue));
                logMessage = String.format("STX: Escrevendo X (%06X) para memória[%06X]", X.getIntValue(), effectiveAddress);
            }
            case 0x1C -> { // SUB
                int operand = operands[0];
                int result = instructionSet.executeSUB(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("SUB: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resultado=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x94 -> { // SUBR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeSUBR(r1Val, r2Val);
                logMessage = String.format("SUBR: R%d=%04X, R%d=%04X => Resultado=%04X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }
            case 0x2C -> { // TIX
                int operand = operands[0];
                int result = instructionSet.executeTIX(X.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("TIX: X=%04X, Operando=%04X, Indexed=%b => Resultado=%04X",
                        X.getIntValue(), operand, isIndexed(), result);
                X.setValue(X.getIntValue() + 1); // Incrementa X
                updateConditionCode(result);
            }
            case 0xB8 -> { // TIXR (Formato 2)
                int[] regs = operands;
                int rVal = getRegisterById(regs[0]).getIntValue();
                X.setValue(X.getIntValue() + 1);
                int comparison = instructionSet.executeTIXR(X.getIntValue(), rVal);
                logMessage = String.format("TIXR: R%d=%04X, X (incrementado)=%04X => Comparação=%04X",
                        regs[0], rVal, X.getIntValue(), comparison);
                updateConditionCode(comparison);
            }
            default -> throw new IllegalStateException(String.format("Instrução não suportada: %02X", currentOpcode));
        }
        lastExecutionLog = logMessage;
    }

    /**
     * Converte um inteiro em um array de 3 bytes (big-endian).
     *
     * @param value Valor inteiro a ser convertido.
     * @return Array de 3 bytes representando o valor.
     */
    private byte[] intTo3Bytes(int value) {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) ((value >> 16) & 0xFF); // Byte mais significativo
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) (value & 0xFF);         // Byte menos significativo
        return bytes;
    }

    /**
     * Retorna a mensagem de log da última instrução executada.
     *
     * @return Log da última execução.
     */
    public String getLastExecutionLog() {
        return lastExecutionLog;
    }

    /**
     * Indica se a execução foi encerrada (por exemplo, via RSUB que retorna 0).
     *
     * @return true se a execução foi encerrada; false caso contrário.
     */
    public boolean isHalted() {
        return halted;
    }

    /**
     * Permite atualizar a referência de memória (por exemplo, após alteração do tamanho).
     *
     * @param memory Nova instância de Memory.
     */
    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    /**
     * Reseta a ControlUnit, reiniciando o estado de execução e limpando os registradores.
     */
    public void reset() {
        halted = false;
        clearRegisters();
    }
}