package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;
import sicxesimulator.utils.Convert;

import java.util.Arrays;

/**
 * A ControlUnit gerencia o ciclo de execução da máquina SIC/XE, realizando as fases de fetch, decode e execute,
 * além de atualizar os registradores e o PC conforme as instruções são executadas.
 * Nesta versão, o PC e os operandos são tratados em unidades de palavra.
 * Quando é necessário acessar a memória, converte-se o índice de palavra para endereço em bytes (multiplicando por 3).
 */
public class ControlUnit {
    private final InstructionSet instructionSet;
    private Memory memory;

    // Registradores do SIC/XE
    private final Register A, X, L, B, S, T, F;
    private final Register PC;
    private final Register SW;
    private final Register[] registers;

    // Variáveis do ciclo de instrução
    // baseAddress agora é em palavras (índice de palavra onde o programa foi carregado)
    private int baseAddress;
    private int currentOpcode;
    private int instructionFormat; // Formato: 1, 2, 3 ou 4
    private int[] operands;        // Operandos decodificados (em unidades de palavra)
    private boolean indexed;       // Modo indexado
    private boolean extended;      // Flag "e" (formato 4)

    // Variáveis de controle
    private String lastExecutionLog = "";
    private boolean halted;

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
        this.instructionSet = new InstructionSet(memory, this);
        halted = false;
    }

    public void clearRegisters() {
        for (Register r : registers) r.clearRegister();
    }

    public Register[] getCurrentRegisters() {
        return Arrays.copyOf(registers, registers.length);
    }

    private Register getRegisterById(int id) {
        return switch (id) {
            case 0 -> A; case 1 -> X; case 2 -> L; case 3 -> B;
            case 4 -> S; case 5 -> T; case 6 -> F; case 8 -> PC;
            case 9 -> SW; default -> throw new IllegalArgumentException("ID inválido: " + id);
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
    public Register getPC() {
        return PC;
    }

    /**
     * Incrementa o PC (em palavras) conforme o tamanho da instrução.
     */
    private void incrementPC(int instructionSizeInWords) {
        int bytesToIncrement = (instructionSizeInWords * 3);
        setPC(PC.getIntValue() + bytesToIncrement);
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
     * Fetch: Lê o opcode do primeiro byte da palavra apontada pelo PC.
     * PC é considerado um índice de palavra; para acessar a memória, multiplica-se por 3.
     */

    public void fetch() {
        int wordIndex = PC.getIntValue(); // PC é o índice da palavra
        int byteAddress = wordIndex * 3;  // Converte para endereço em bytes

        // Verificação adicional para garantir que o byteAddress não ultrapasse os limites da memória
        if (byteAddress < 0 || byteAddress >= memory.getSizeInBytes()) {
            throw new IllegalArgumentException("Endereço de memória inválido: " + byteAddress);
        }

        // Lê o primeiro byte da palavra (opcode)
        currentOpcode = memory.readByte(wordIndex, 0) & 0xFF; // Lê o primeiro byte (opcode)
    }


    /**
     * Decode: Determina o formato da instrução e extrai os operandos.
     * Atualiza o PC conforme o tamanho (em palavras) da instrução.
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
            default -> throw new IllegalStateException("Formato inválido: " + instructionFormat);
        }
        int instructionSizeInBytes = getInstructionSizeInBytes(currentOpcode);
        int instructionSizeInWords = instructionSizeInBytes / 3;

        incrementPC(instructionSizeInWords);

    }

    /**
     * Determina o formato da instrução com base no opcode e na flag "e".
     * Lê o segundo byte da palavra atual (offset 1).
     */
    private int determineInstructionFormat(int opcode) {
        if (opcode == 0x4C) return 3; // RSUB
        if (opcode == 0x90 || opcode == 0x04) return 2; // ADDR, CLEAR
        int flags = memory.readByte(PC.getIntValue(), 1) & 0xFF;
        return (flags & 0x01) != 0 ? 4 : 3;
    }

    /**
     * Decodifica instruções de formato 1: 1 byte (ocupando 1 palavra).
     */
    private void decodeFormat1() {
        operands = new int[0];
    }

    /**
     * Decodifica instruções de formato 2: utiliza o byte no offset 1 para os operandos.
     */
    private void decodeFormat2() {
        int byte2 = memory.readByte(PC.getIntValue(), 1) & 0xFF;
        operands = new int[]{ (byte2 >> 4) & 0xF, byte2 & 0xF };
    }

    /**
     * Decodifica instruções de formatos 3 e 4.
     * Para formato 3, os 3 bytes estão na mesma palavra.
     * Para formato 4, os 3 primeiros bytes estão na palavra corrente e o 4º byte está na posição 0 da palavra seguinte.
     * Nesta versão, o deslocamento é lido em bytes e convertido para um índice de palavra.
     */
    private void decodeFormat3or4() {
        int byte1 = memory.readByte(PC.getIntValue(), 1) & 0xFF;
        int byte2 = memory.readByte(PC.getIntValue(), 2) & 0xFF;
        extended = (byte1 & 0x01) != 0; // flag "e"
        indexed = (byte1 & 0x10) != 0;  // flag "x"
        int niFlags = (byte1 & 0xC0) >> 6; // flags "n" e "i"
        int addressingFlags = byte1 & 0xF0;  // flags de endereçamento (b, p, etc.)
        int dispOrAddress;
        int effectiveAddressBytes; // endereço efetivo em bytes

        if (extended) { // Formato 4
            int byte3 = memory.readByte(PC.getIntValue() + 1, 0) & 0xFF;
            dispOrAddress = ((byte1 & 0x0F) << 16) | (byte2 << 8) | byte3;
            // Para formato 4 direto (n=1, i=1, b=0, p=0)
            if (niFlags == 0x03 && (addressingFlags & 0x2E) == 0) {
                effectiveAddressBytes = dispOrAddress;
            } else {
                throw new UnsupportedOperationException("Formato 4 com endereçamento não direto não implementado");
            }
        } else { // Formato 3
            dispOrAddress = ((byte1 & 0x0F) << 8) | byte2;
            if (dispOrAddress >= 0x800) dispOrAddress -= 0x1000; // converte para signed (-2048 a 2047)
            if ((addressingFlags & 0x20) != 0) { // Base-relativo (b=1)
                effectiveAddressBytes = (B.getIntValue() * 3) + dispOrAddress;
            } else if ((addressingFlags & 0x10) != 0) { // PC-relativo (p=1)
                effectiveAddressBytes = (PC.getIntValue() * 3 + 3) + dispOrAddress;
            } else { // Direto (b=0, p=0)
                effectiveAddressBytes = dispOrAddress;
            }
        }
        // Converte o endereço efetivo (em bytes) para índice de palavra (supondo alinhamento)
        int effectiveWord = effectiveAddressBytes / 3;
        operands = new int[]{ effectiveWord, byte1 };
    }

    /**
     * Retorna o tamanho da instrução atual em palavras.
     * Formatos 1, 2 e 3 ocupam 1 palavra; Formato 4 ocupa 2 palavras.
     */
    private int getInstructionSize() {
        return switch (instructionFormat) {
            case 1, 2, 3 -> 1;
            case 4 -> 2;
            default -> throw new IllegalStateException("Formato inválido");
        };
    }

    // Métodos auxiliares para consulta

    public boolean isIndexed() {
        return indexed;
    }

    public int getInstructionFormat() {
        return instructionFormat;
    }

    // ----- Métodos de execução -----

    @SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
    public void execute() {
        String logMessage;
        switch (currentOpcode) {
            case 0x18 -> { // ADD (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeADD(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("ADD: A=%06X, Operando=%06X, Indexed=%b, X=%06X => Resultado=%06X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x58 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (ADDF)", currentOpcode));
            }
            case 0x90 -> { // ADDR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeADDR(r1Val, r2Val);
                logMessage = String.format("ADDR: R%d=%06X, R%d=%06X => Resultado=%06X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }
            case 0x40 -> { // AND (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeAND(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("AND: A=%06X, Operando=%06X, Indexed=%b, X=%06X => Resultado=%06X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x04 -> { // CLEAR ou LDX
                if (getInstructionFormat() == 2) { // CLEAR (Formato 2)
                    int reg = operands[0];
                    int oldVal = getRegisterById(reg).getIntValue();
                    int newVal = instructionSet.executeCLEAR();
                    logMessage = String.format("CLEAR: R%d antigo=%06X => novo=%06X", reg, oldVal, newVal);
                    getRegisterById(reg).setValue(newVal);
                } else { // LDX (Formato 3/4)
                    int operand = operands[0];
                    int result = instructionSet.executeLDX(operand, isIndexed(), X.getIntValue());
                    logMessage = String.format("LDX: X antigo=%06X, Operando=%06X, Indexed=%b => novo X=%06X",
                            X.getIntValue(), operand, isIndexed(), result);
                    X.setValue(result);
                }
            }
            case 0x28 -> { // COMP (Formato 3/4)
                int operand = operands[0];
                int comparison = instructionSet.executeCOMP(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("COMP: A=%06X, Operando=%06X, Indexed=%b, X=%06X => Comparação=%06X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), comparison);
                updateConditionCode(comparison);
            }
            case 0x88 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (COMPF)", currentOpcode));
            }
            case 0xA0 -> { // COMPR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int comparison = instructionSet.executeCOMPR(r1Val, r2Val);
                logMessage = String.format("COMPR: R%d=%06X, R%d=%06X => Comparação=%06X",
                        regs[0], r1Val, regs[1], r2Val, comparison);
                updateConditionCode(comparison);
            }
            case 0x24 -> { // DIV (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeDIV(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("DIV: A=%06X, Operando=%06X, Indexed=%b, X=%06X => Resultado=%06X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x64 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (DIVF)", currentOpcode));
            }
            case 0x9C -> { // DIVR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeDIVR(r1Val, r2Val);
                logMessage = String.format("DIVR: R%d=%06X, R%d=%06X => Resultado=%06X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }
            case 0xC4 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (FIX)", currentOpcode));
            }
            case 0xC0 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (FLOAT)", currentOpcode));
            }
            case 0xF4 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (HIO)", currentOpcode));
            }
            case 0x3C -> { // J (Formato 3/4)
                int newPC = instructionSet.executeJ(operands[0], isIndexed(), X.getIntValue());
                logMessage = String.format("J: Pulando para o endereço %06X", newPC);
                PC.setValue(newPC);
            }
            case 0x30 -> { // JEQ (Formato 3/4)
                int newPC = instructionSet.executeCONDITIONAL_JUMP(0, operands[0], getConditionCode());
                if (newPC != -1) {
                    logMessage = String.format("JEQ: Condição satisfeita, pulando para %06X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JEQ: Condição não satisfeita, sem pulo.";
                }
            }
            case 0x34 -> { // JGT (Formato 3/4)
                int newPC = instructionSet.executeCONDITIONAL_JUMP(2, operands[0], getConditionCode());
                if (newPC != -1) {
                    logMessage = String.format("JGT: Condição satisfeita, pulando para %06X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JGT: Condição não satisfeita, sem pulo.";
                }
            }
            case 0x38 -> { // JLT (Formato 3/4)
                int newPC = instructionSet.executeCONDITIONAL_JUMP(1, operands[0], getConditionCode());
                if (newPC != -1) {
                    logMessage = String.format("JLT: Condição satisfeita, pulando para %06X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JLT: Condição não satisfeita, sem pulo.";
                }
            }
            case 0x48 -> { // JSUB (Formato 3/4)
                int returnAddress = PC.getIntValue() + getInstructionSize();
                L.setValue(returnAddress);
                int newPC = instructionSet.executeJSUB(operands[0], isIndexed(), X.getIntValue());
                logMessage = String.format("JSUB: Endereço de retorno = %06X, pulando para %06X", returnAddress, newPC);
                PC.setValue(newPC);
            }
            case 0x00 -> { // LDA (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeLDA(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDA: Carregando do endereço %06X => A = %06X", operand, result);
                A.setValue(result);
            }
            case 0x68 -> { // LDB (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeLDB(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDB: Carregando do endereço %06X => B = %06X", operand, result);
                B.setValue(result);
            }
            case 0x50 -> { // LDCH (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeLDCH(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDCH: Carregando do endereço %06X => A = %06X", operand, result);
                A.setValue(result);
            }
            case 0x70 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (LDF)", currentOpcode));
            }
            case 0x08 -> { // LDL (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeLDL(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDL: Carregando do endereço %06X => L = %06X", operand, result);
                L.setValue(result);
            }
            case 0x6C -> { // LDS (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeLDS(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDS: Carregando do endereço %06X => S = %06X", operand, result);
                S.setValue(result);
            }
            case 0x74 -> { // LDT (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeLDT(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDT: Carregando do endereço %06X => T = %06X", operand, result);
                T.setValue(result);
            }
            case 0xD0 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (LPS)", currentOpcode));
            }
            case 0x20 -> { // MUL (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeMUL(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("MUL: A=%06X, Operando=%06X, Indexed=%b, X=%06X => Resultado=%06X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x60 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (MULF)", currentOpcode));
            }
            case 0x98 -> { // MULR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeMULR(r1Val, r2Val);
                logMessage = String.format("MULR: R%d=%06X, R%d=%06X => Resultado=%06X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }
            case 0xC8 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (NORM)", currentOpcode));
            }
            case 0x44 -> { // OR (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeOR(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("OR: A=%06X, Operando=%06X, Indexed=%b, X=%06X => Resultado=%06X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0xD8 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (RD)", currentOpcode));
            }
            case 0xAC -> { // RMO (Formato 2)
                int[] regs = operands;
                int sourceVal = getRegisterById(regs[0]).getIntValue();
                logMessage = String.format("RMO: Copiando R%d (%06X) para R%d", regs[0], sourceVal, regs[1]);
                getRegisterById(regs[1]).setValue(sourceVal);
            }
            case 0x4C -> { // RSUB (Formato 3/4)
                int returnAddress = L.getIntValue();
                logMessage = String.format("RSUB: Retornando para %06X", returnAddress);
                PC.setValue(returnAddress);
            }
            case 0xA4 -> { // SHIFTL (Formato 2)
                int[] op = operands; // [reg, count]
                int reg = op[0];
                int count = op[1];
                int oldVal = getRegisterById(reg).getIntValue();
                int shifted = instructionSet.executeSHIFTL(oldVal, count);
                logMessage = String.format("SHIFTL: R%d: %06X << %d = %06X", reg, oldVal, count, shifted);
                getRegisterById(reg).setValue(shifted);
                updateConditionCode(shifted);
            }
            case 0xA8 -> { // SHIFTR (Formato 2)
                int[] op = operands; // [reg, count]
                int reg = op[0];
                int count = op[1];
                int oldVal = getRegisterById(reg).getIntValue();
                int shifted = instructionSet.executeSHIFTR(oldVal, count);
                logMessage = String.format("SHIFTR: R%d: %06X >> %d = %06X", reg, oldVal, count, shifted);
                getRegisterById(reg).setValue(shifted);
                updateConditionCode(shifted);
            }
            case 0xF0 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (SIO)", currentOpcode));
            }
            case 0xEC -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (SSK)", currentOpcode));
            }
            case 0x0C -> { // STA (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                int staValue = instructionSet.executeSTA(A.getIntValue());
                memory.writeWord(effectiveAddress, Convert.intTo3Bytes(staValue));
                logMessage = String.format("STA: Escrevendo A (%06X) para memória na palavra %06X", A.getIntValue(), effectiveAddress);
            }
            case 0x78 -> { // STB (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                if (effectiveAddress < 0) {
                    throw new IllegalArgumentException("Endereço inválido para STB: " + effectiveAddress);
                }
                int stbValue = instructionSet.executeSTB(B.getIntValue());
                memory.writeWord(effectiveAddress, Convert.intTo3Bytes(stbValue));
                logMessage = String.format("STB: Escrevendo B (%06X) para memória na palavra %06X", B.getIntValue(), effectiveAddress);
            }
            case 0x54 -> { // STCH (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                int stchValue = instructionSet.executeSTCH(A.getIntValue());
                // Escreve o byte no offset 2 da palavra
                memory.writeByte(effectiveAddress, 2, stchValue);
                logMessage = String.format("STCH: Escrevendo byte de A (%02X) para memória (palavra %06X, offset 2)",
                        stchValue, effectiveAddress);
            }
            case 0x80 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (STF)", currentOpcode));
            }
            case 0xD4 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (STI)", currentOpcode));
            }
            case 0x14 -> { // STL (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                int stlValue = instructionSet.executeSTL(L.getIntValue());
                memory.writeWord(effectiveAddress, Convert.intTo3Bytes(stlValue));
                logMessage = String.format("STL: Escrevendo L (%06X) para memória na palavra %06X", L.getIntValue(), effectiveAddress);
            }
            case 0x7C -> { // STS (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                int stsValue = instructionSet.executeSTS(S.getIntValue());
                memory.writeWord(effectiveAddress, Convert.intTo3Bytes(stsValue));
                logMessage = String.format("STS: Escrevendo S (%06X) para memória na palavra %06X", S.getIntValue(), effectiveAddress);
            }
            case 0xE8 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (STSW)", currentOpcode));
            }
            case 0x84 -> { // STT (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                int sttValue = instructionSet.executeSTT(T.getIntValue());
                memory.writeWord(effectiveAddress, Convert.intTo3Bytes(sttValue));
                logMessage = String.format("STT: Escrevendo T (%06X) para memória na palavra %06X", T.getIntValue(), effectiveAddress);
            }
            case 0x10 -> { // STX (Formato 3/4)
                int effectiveAddress = instructionSet.calculateEffectiveAddress(operands[0], X.getIntValue(), isIndexed());
                int stxValue = instructionSet.executeSTX(X.getIntValue());
                memory.writeWord(effectiveAddress, Convert.intTo3Bytes(stxValue));
                logMessage = String.format("STX: Escrevendo X (%06X) para memória na palavra %06X", X.getIntValue(), effectiveAddress);
            }
            case 0x1C -> { // SUB (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeSUB(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("SUB: A=%06X, Operando=%06X, Indexed=%b, X=%06X => Resultado=%06X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }
            case 0x5C -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (SUBF)", currentOpcode));
            }
            case 0x94 -> { // SUBR (Formato 2)
                int[] regs = operands;
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeSUBR(r1Val, r2Val);
                logMessage = String.format("SUBR: R%d=%06X, R%d=%06X => Resultado=%06X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }
            case 0xB0 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (SVC)", currentOpcode));
            }
            case 0xE0 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (TD)", currentOpcode));
            }
            case 0xF8 -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (TIO)", currentOpcode));
            }
            case 0x2C -> { // TIX (Formato 3/4)
                int operand = operands[0];
                int result = instructionSet.executeTIX(X.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("TIX: X=%06X, Operando=%06X, Indexed=%b => Resultado=%06X",
                        X.getIntValue(), operand, isIndexed(), result);
                X.setValue(X.getIntValue() + 1);
                updateConditionCode(result);
            }
            case 0xB8 -> { // TIXR (Formato 2)
                int[] regs = operands;
                int rVal = getRegisterById(regs[0]).getIntValue();
                X.setValue(X.getIntValue() + 1);
                int comparison = instructionSet.executeTIXR(X.getIntValue(), rVal);
                logMessage = String.format("TIXR: R%d=%06X, X (incrementado)=%06X => Comparação=%06X",
                        regs[0], rVal, X.getIntValue(), comparison);
                updateConditionCode(comparison);
            }
            case 0xDC -> {
                throw new IllegalStateException(String.format("Instrução não implementada: %02X (WD)", currentOpcode));
            }
            default -> throw new IllegalStateException(String.format("Instrução desconhecida: %02X", currentOpcode));
        }
        lastExecutionLog = logMessage;
    }

    public String getLastExecutionLog() {
        return lastExecutionLog;
    }
    public boolean isHalted() {
        return halted;
    }
    public void setMemory(Memory memory) {
        this.memory = memory;
    }
    public void reset() {
        halted = false;
        clearRegisters();
    }
}
