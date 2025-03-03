package sicxesimulator.machine.cpu;

import sicxesimulator.machine.Memory;

import java.util.Arrays;

public class ControlUnit {
    private  Memory memory;
    private final InstructionSet instructionSet;

    // Registradores do SIC/XE
    private final Register A, X, L, B, S, T, F;
    private final Register PC;
    private final Register SW;
    private final Register[] registers;

    // Variáveis para o ciclo de instrução
    private int baseAddress;       // Endereço onde o programa foi carregado
    private int currentOpcode;
    private int instructionFormat; // 1, 2, 3 ou 4
    private int[] operands;        // Operandos decodificados
    private boolean indexed;       // Modo indexado
    private boolean extended;      // Flag "e" (formato 4)

    // Campo para armazenar o log da última instrução executada
    private String lastExecutionLog = "";
    private boolean halted;

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

    // Limpa todos os registradores
    public void clearRegisters() {
        for (Register r : registers) {
            r.clearRegister();
        }
    }

    // Retorna uma cópia do estado atual dos registradores
    public Register[] getCurrentRegisters() {
        return Arrays.copyOf(registers, registers.length);
    }

    // Mapeia um ID de registrador para o objeto Register correspondente
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

    // Manipulação do PC

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

    // Manipulação do Condition Code
    private int getConditionCode() {
        return SW.getIntValue() & 0x03;
    }

    private void updateConditionCode(int result) {
        int conditionCode;
        if (result == 0) {
            conditionCode = 0; // Equal
        } else if (result < 0) {
            conditionCode = 1; // Less
        } else {
            conditionCode = 2; // Greater
        }
        setConditionCode(conditionCode);
    }

    private void setConditionCode(int conditionCode) {
        int currentSW = SW.getIntValue();
        SW.setValue((currentSW & 0xFFFFFC) | (conditionCode & 0x03));
    }

    // ----- CICLO DE INSTRUÇÃO -----

    /**
     * Fetch: lê o opcode da memória no endereço apontado pelo PC.
     */
    public void fetch() {
        currentOpcode = memory.readByte(PC.getIntValue()) & 0xFF;
    }

    /**
     * Decode: determina o formato da instrução e extrai os operandos.
     * Após a decodificação, o PC é incrementado pelo tamanho da instrução.
     */
    public void decode() {
        instructionFormat = determineInstructionFormat(currentOpcode);
        // Reinicia os operandos e flags
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
     * Determina o formato da instrução com base no opcode.
     * Para opcodes não explicitamente mapeados, verifica a flag "e" no byte seguinte para distinguir entre
     * formato 3 e 4.
     */
    private int determineInstructionFormat(int opcode) {
        return switch (opcode) {
            case 0x4C -> 3; // RSUB (Formato 3)
            case 0x90, 0x04 -> 2; // ADDR, CLEAR (Formato 2) – note: 0x04 também pode ser LDX se não for formato 2
            case 0x00, 0x18, 0x3C -> {
                // Para LDA, ADD, J, etc. pode ser formato 3 ou 4.
                // Verifica o flag "e" no segundo byte.
                int flags = memory.readByte(PC.getIntValue() + 1) & 0xFF;
                if ((flags & 0x01) != 0) yield 4;
                else yield 3;
            }
            default -> {
                int flags = memory.readByte(PC.getIntValue() + 1) & 0xFF;
                if ((flags & 0x01) != 0) yield 4;
                else yield 3;
            }
        };
    }

    // Decodificação do formato 1: 1 byte sem operandos.
    private void decodeFormat1() {
        operands = new int[0];
    }

    // Decodificação do formato 2: 2 bytes – operandos são os dois nibbles do segundo byte.
    private void decodeFormat2() {
        int byte2 = memory.readByte(PC.getIntValue() + 1) & 0xFF;
        operands = new int[]{ (byte2 >> 4) & 0xF, byte2 & 0xF };
    }

    /**
     * Decodifica instruções dos formatos 3 e 4.
     * - Formato 3 (3 bytes): Byte0 = opcode, Byte1 = flags, Byte2 = deslocamento (12 bits, formado pelo nibble inferior de Byte1 e os 8 bits de Byte2).
     * - Formato 4 (4 bytes): Byte0 = opcode, Byte1 = flags, Byte2 e Byte3 = endereço (20 bits, formado pelo nibble inferior de Byte1 concatenado com Byte2 e Byte3).
     */
    private void decodeFormat3or4() {
        int byte1 = memory.readByte(PC.getIntValue() + 1) & 0xFF;
        // Os bits: ni: bits 7-6; x: bit 5; b: bit 4; p: bit 3; e: bit 0.
        extended = (byte1 & 0x01) != 0; // e flag
        indexed = (byte1 & 0x10) != 0;  // x flag

        int addressField;
        int lowNibble = byte1 & 0x0F;
        int byte2 = memory.readByte(PC.getIntValue() + 2) & 0xFF;
        if (extended) {
            // Formato 4: 4 bytes no total.
            // Endereço (20 bits) = (lower nibble de byte1 << 16) | (byte2 << 8) | (byte3)
            int byte3 = memory.readByte(PC.getIntValue() + 3) & 0xFF;
            addressField = (lowNibble << 16) | (byte2 << 8) | byte3;
        } else {
            // Formato 3: 3 bytes no total.
            // Deslocamento de 12 bits = (lower nibble de byte1 << 8) | (byte2)
            addressField = (lowNibble << 8) | byte2;
        }
        operands = new int[]{ addressField, byte1 }; // Armazena também o byte de flags
    }

    /**
     * Retorna o tamanho da instrução atual, com base no formato.
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

    ///  GETTERS AUXILIARES PARA DADOS DECODIFICADOS
    public int getInstructionFormat() { return instructionFormat; }
    public int[] getOperands() { return operands; }
    public boolean isIndexed() { return indexed; }

    /**
     * Executa a instrução decodificada. Além de realizar a operação,
     * registra uma mensagem com os detalhes da execução.
     */
    public void execute() {
        String logMessage;

        switch (currentOpcode) {
            // ADD (Formato 3/4 - Opcode 0x18)
            case 0x18 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeADD(
                        A.getIntValue(),
                        operand,
                        isIndexed(),
                        X.getIntValue()
                );
                logMessage = String.format("ADD: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resulto=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }

            // ADDR (Formato 2 - Opcode 0x90)
            case 0x90 -> {
                int[] regs = getOperands(); // [r1, r2]
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeADDR(r1Val, r2Val);
                logMessage = String.format("ADDR: R%d=%04X, R%d=%04X => Resulto=%04X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }

            // AND (Formato 3/4 - Opcode 0x40)
            case 0x40 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeAND(
                        A.getIntValue(),
                        operand,
                        isIndexed(),
                        X.getIntValue()
                );
                logMessage = String.format("AND: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resultado=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }

            // CLEAR ou LDX (Opcode 0x04)
            case 0x04 -> {
                if (getInstructionFormat() == 2) { // CLEAR (Formato 2)
                    int reg = getOperands()[0];
                    int oldVal = getRegisterById(reg).getIntValue();
                    int newVal = instructionSet.executeCLEAR();
                    logMessage = String.format("CLEAR: R%d antigo=%04X => novo=%04X", reg, oldVal, newVal);
                    getRegisterById(reg).setValue(newVal);
                } else { // LDX (Formato 3/4)
                    int operand = getOperands()[0];
                    int result = instructionSet.executeLDX(
                            operand,
                            isIndexed(),
                            X.getIntValue()
                    );
                    logMessage = String.format("LDX: X antigo=%04X, Operando=%04X, Indexed=%b => novo X=%04X",
                            X.getIntValue(), operand, isIndexed(), result);
                    X.setValue(result);
                }
            }

            // COMP (Formato 3/4 - Opcode 0x28)
            case 0x28 -> {
                int operand = getOperands()[0];
                int comparison = instructionSet.executeCOMP(
                        A.getIntValue(),
                        operand,
                        isIndexed(),
                        X.getIntValue()
                );
                logMessage = String.format("COMP: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Comparação=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), comparison);
                updateConditionCode(comparison);
            }

            // COMPR (Formato 2 - Opcode 0xA0)
            case 0xA0 -> {
                int[] regs = getOperands(); // [r1, r2]
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int comparison = instructionSet.executeCOMPR(r1Val, r2Val);
                logMessage = String.format("COMPR: R%d=%04X, R%d=%04X => Comparação=%04X",
                        regs[0], r1Val, regs[1], r2Val, comparison);
                updateConditionCode(comparison);
            }

            // DIV (Formato 3/4 - Opcode 0x24)
            case 0x24 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeDIV(
                        A.getIntValue(),
                        operand,
                        isIndexed(),
                        X.getIntValue()
                );
                logMessage = String.format("DIV: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resulto=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }

            // DIVR (Formato 2 - Opcode 0x9C)
            case 0x9C -> {
                int[] regs = getOperands();
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeDIVR(r1Val, r2Val);
                logMessage = String.format("DIVR: R%d=%04X, R%d=%04X => Resulto=%04X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }

            // J (Formato 3/4 - Opcode 0x3C)
            case 0x3C -> {
                int newPC = instructionSet.executeJ(
                        getOperands()[0],
                        isIndexed(),
                        X.getIntValue()
                );
                logMessage = String.format("J: Pulando para o endereço %04X", newPC);
                PC.setValue(newPC);
            }

            // JEQ (Formato 3/4 - Opcode 0x30)
            case 0x30 -> {
                int newPC = instructionSet.executeCONDITIONAL_JUMP(
                        0, // Equal
                        getOperands()[0],
                        getConditionCode()
                );
                if (newPC != -1) {
                    logMessage = String.format("JEQ: Condição satisfeita, pulando para %04X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JEQ: Condição não satisfeita, sem pulo.";
                }
            }

            // JGT (Formato 3/4 - Opcode 0x34)
            case 0x34 -> {
                int newPC = instructionSet.executeCONDITIONAL_JUMP(
                        2, // Greater
                        getOperands()[0],
                        getConditionCode()
                );
                if (newPC != -1) {
                    logMessage = String.format("JGT: Condição satisfeita, pulando para %04X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JGT: Condição não satisfeita, sem pulo.";
                }
            }

            // JLT (Formato 3/4 - Opcode 0x38)
            case 0x38 -> {
                int newPC = instructionSet.executeCONDITIONAL_JUMP(
                        1, // Less
                        getOperands()[0],
                        getConditionCode()
                );
                if (newPC != -1) {
                    logMessage = String.format("JLT: Condição satisfeita, pulando para %04X", newPC);
                    PC.setValue(newPC);
                } else {
                    logMessage = "JLT: Condição não satisfeita, sem pulo.";
                }
            }

            // JSUB (Formato 3/4 - Opcode 0x48)
            case 0x48 -> {
                int returnAddress = PC.getIntValue() + getInstructionSize();
                L.setValue(returnAddress);
                int newPC = instructionSet.executeJSUB(
                        getOperands()[0],
                        isIndexed(),
                        X.getIntValue()
                );
                logMessage = String.format("JSUB: Endereço de retorno = %04X, Pulando para %04X", returnAddress, newPC);
                PC.setValue(newPC);
            }

            // LDA (Formato 3/4 - Opcode 0x00)
            case 0x00 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeLDA(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDA: Carregando do endereço %04X => A = %04X", operand, result);
                A.setValue(result);
            }

            // LDB (Formato 3/4 - Opcode 0x68)
            case 0x68 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeLDB(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDB: Carregando do endereço %04X => B = %04X", operand, result);
                B.setValue(result);
            }

            // LDCH (Formato 3/4 - Opcode 0x50)
            case 0x50 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeLDCH(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDCH: Carregando do endereço %04X => A = %04X", operand, result);
                A.setValue(result);
            }

            // LDL (Formato 3/4 - Opcode 0x08)
            case 0x08 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeLDL(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDL: Carregando do endereço %04X => L = %04X", operand, result);
                L.setValue(result);
            }

            // LDS (Formato 3/4 - Opcode 0x6C)
            case 0x6C -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeLDS(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDS: Carregando do endereço %04X => S = %04X", operand, result);
                S.setValue(result);
            }

            // LDT (Formato 3/4 - Opcode 0x74)
            case 0x74 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeLDT(operand, isIndexed(), X.getIntValue());
                logMessage = String.format("LDT: Carregando do endereço %04X => T = %04X", operand, result);
                T.setValue(result);
            }

            // MUL (Formato 3/4 - Opcode 0x20)
            case 0x20 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeMUL(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("MUL: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resulto=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }

            // MULR (Formato 2 - Opcode 0x98)
            case 0x98 -> {
                int[] regs = getOperands();
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeMULR(r1Val, r2Val);
                logMessage = String.format("MULR: R%d=%04X, R%d=%04X => Resulto=%04X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }

            // OR (Formato 3/4 - Opcode 0x44)
            case 0x44 -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeOR(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("OR: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resulto=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }

            // RMO (Formato 2 - Opcode 0xAC)
            case 0xAC -> {
                int[] regs = getOperands();
                int sourceVal = getRegisterById(regs[0]).getIntValue();
                logMessage = String.format("RMO: Copiando R%d (%04X) para R%d", regs[0], sourceVal, regs[1]);
                getRegisterById(regs[1]).setValue(sourceVal);
            }

            // RSUB (Formato 3/4 - Opcode 0x4C)
            case 0x4C -> {
                int newPC = instructionSet.executeRSUB(L.getIntValue());
                logMessage = String.format("RSUB: L=%04X, novo PC=%04X", L.getIntValue(), newPC);
                PC.setValue(newPC);
                if (newPC == 0) {
                    halted = true;
                    logMessage += " [Execução Encerrada]";
                }
            }


            // SHIFTL (Formato 2 - Opcode 0xA4)
            case 0xA4 -> {
                int[] op = getOperands(); // [reg, count]
                int reg = op[0];
                int count = op[1];
                int oldVal = getRegisterById(reg).getIntValue();
                int shifted = instructionSet.executeSHIFTL(oldVal, count);
                logMessage = String.format("SHIFTL: R%d: %04X << %d = %04X", reg, oldVal, count, shifted);
                getRegisterById(reg).setValue(shifted);
                updateConditionCode(shifted);
            }

            // SHIFTR (Formato 2 - Opcode 0xA8)
            case 0xA8 -> {
                int[] op = getOperands(); // [reg, count]
                int reg = op[0];
                int count = op[1];
                int oldVal = getRegisterById(reg).getIntValue();
                int shifted = instructionSet.executeSHIFTR(oldVal, count);
                logMessage = String.format("SHIFTR: R%d: %04X >> %d = %04X", reg, oldVal, count, shifted);
                getRegisterById(reg).setValue(shifted);
                updateConditionCode(shifted);
            }

            // STA (Formato 3/4 - Opcode 0x0C)
            case 0x0C -> {
                int effectiveAddress = instructionSet.calculateEffectiveAddress(getOperands()[0], X.getIntValue(), isIndexed());
                int staValue = instructionSet.executeSTA(A.getIntValue());
                memory.writeWord(effectiveAddress, staValue);
                logMessage = String.format("STA: Escrevendo A (%04X) para memõria[%04X]", A.getIntValue(), effectiveAddress);
            }

            // STB (Formato 3/4 - Opcode 0x78)
            case 0x78 -> {
                int effectiveAddress = instructionSet.calculateEffectiveAddress(getOperands()[0], X.getIntValue(), isIndexed());
                int stbValue = instructionSet.executeSTB(B.getIntValue());
                memory.writeWord(effectiveAddress, stbValue);
                logMessage = String.format("STB: Escrevendo B (%04X) para memória[%04X]", B.getIntValue(), effectiveAddress);
            }

            // STCH (Formato 3/4 - Opcode 0x54)
            case 0x54 -> {
                int effectiveAddress = instructionSet.calculateEffectiveAddress(getOperands()[0], X.getIntValue(), isIndexed());
                int stchValue = instructionSet.executeSTCH(A.getIntValue());
                memory.writeByte(effectiveAddress, stchValue);
                logMessage = String.format("STCH: Escrevendo A (%04X) para memória[%04X] como byte", A.getIntValue(), effectiveAddress);
            }

            // STL (Formato 3/4 - Opcode 0x14)
            case 0x14 -> {
                int effectiveAddress = instructionSet.calculateEffectiveAddress(getOperands()[0], X.getIntValue(), isIndexed());
                int stlValue = instructionSet.executeSTL(L.getIntValue());
                memory.writeWord(effectiveAddress, stlValue);
                logMessage = String.format("STL: Escrevendo L (%04X) para memória[%04X]", L.getIntValue(), effectiveAddress);
            }

            // STS (Formato 3/4 - Opcode 0x7C)
            case 0x7C -> {
                int effectiveAddress = instructionSet.calculateEffectiveAddress(getOperands()[0], X.getIntValue(), isIndexed());
                int stsValue = instructionSet.executeSTS(S.getIntValue());
                memory.writeWord(effectiveAddress, stsValue);
                logMessage = String.format("STS: Escrevendo S (%04X) para memória[%04X]", S.getIntValue(), effectiveAddress);
            }

            // STT (Formato 3/4 - Opcode 0x84)
            case 0x84 -> {
                int effectiveAddress = instructionSet.calculateEffectiveAddress(getOperands()[0], X.getIntValue(), isIndexed());
                int sttValue = instructionSet.executeSTT(T.getIntValue());
                memory.writeWord(effectiveAddress, sttValue);
                logMessage = String.format("STT: Escrevendo T (%04X) para memória[%04X]", T.getIntValue(), effectiveAddress);
            }

            // STX (Formato 3/4 - Opcode 0x10)
            case 0x10 -> {
                int effectiveAddress = instructionSet.calculateEffectiveAddress(getOperands()[0], X.getIntValue(), isIndexed());
                int stxValue = instructionSet.executeSTX(X.getIntValue());
                memory.writeWord(effectiveAddress, stxValue);
                logMessage = String.format("STX: Escrevendo X (%04X) para memória[%04X]", X.getIntValue(), effectiveAddress);
            }

            // SUB (Formato 3/4 - Opcode 0x1C)
            case 0x1C -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeSUB(A.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("SUB: A=%04X, Operando=%04X, Indexed=%b, X=%04X => Resulto=%04X",
                        A.getIntValue(), operand, isIndexed(), X.getIntValue(), result);
                A.setValue(result);
                updateConditionCode(result);
            }

            // SUBR (Formato 2 - Opcode 0x94)
            case 0x94 -> {
                int[] regs = getOperands();
                int r1Val = getRegisterById(regs[0]).getIntValue();
                int r2Val = getRegisterById(regs[1]).getIntValue();
                int result = instructionSet.executeSUBR(r1Val, r2Val);
                logMessage = String.format("SUBR: R%d=%04X, R%d=%04X => Resulto=%04X",
                        regs[0], r1Val, regs[1], r2Val, result);
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }

            // TIX (Formato 3/4 - Opcode 0x2C)
            case 0x2C -> {
                int operand = getOperands()[0];
                int result = instructionSet.executeTIX(X.getIntValue(), operand, isIndexed(), X.getIntValue());
                logMessage = String.format("TIX: X=%04X, Operando=%04X, Indexed=%b => Resulto=%04X",
                        X.getIntValue(), operand, isIndexed(), result);
                X.setValue(X.getIntValue() + 1); // Incrementa X
                updateConditionCode(result);
            }

            // TIXR (Formato 2 - Opcode 0xB8)
            case 0xB8 -> {
                int[] regs = getOperands(); // [r]
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
     * Retorna a mensagem de log da última instrução executada.
     */
    public String getLastExecutionLog() {
        return lastExecutionLog;
    }

    /**
     * Retorna se a execução foi encerrada.
     */
    public boolean isHalted() {
        return halted;
    }

    /**
     * Setter para eventual mudança da memória da máquina, invocado por Machine.
     * @param memory - Nova instância de memória criada e enviada por Machine.
     */
    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void reset() {
        halted = false;
        clearRegisters();
    }
}