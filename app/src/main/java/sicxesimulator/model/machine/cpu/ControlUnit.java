package sicxesimulator.model.machine.cpu;

import sicxesimulator.model.machine.Memory;

import java.util.Arrays;

public class ControlUnit {
    private final Memory memory;
    private final InstructionSet instructionSet;
    private final Register A, X, L, B, S, T, F;
    private final Register PC;
    private final Register SW;
    private final Register[] registers;
    private int currentOpcode;

    // Variáveis temporárias para armazenar dados decodificados
    private int instructionFormat; //1, 2, 3, ou 4
    private int[] operands;         // Operandos decodificados
    private boolean indexed;        // Modo indexado (true/false)
    private boolean extended;       // Formato estendido (e=1)


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
        this.instructionSet = new InstructionSet(memory);
    }

    // Retorna uma cópia do estado atual dos registradores.
    public Register[] getCurrentRegisters() { return Arrays.copyOf(registers, registers.length); }

    // Obtém registrador por ID (mapeamento conforme tabela SIC/XE)
    private Register getRegisterById(int id) {
        return switch (id) {
            case 0 -> A;
            case 1 -> X;
            case 2 -> L;
            case 3 -> B;
            case 4 -> S;
            case 5 -> T;
            case 8 -> PC;
            case 9 -> SW;
            default -> throw new IllegalArgumentException("ID de registrador inválido: " + id);
        };
    }

    /// Manipulação do Program Counter

    public void setPC(int value) { PC.setValue(value); }

    void incrementPC(int instructionSize) { setPC((PC.getIntValue() + instructionSize)); }

    /// Manipulação do Condition Code

    int getConditionCode() { return SW.getIntValue() & 0x03; }

    void updateConditionCode(int result) {
        int conditionCode;
        if (result == 0) {
            conditionCode = 0; // CC = 00 (Equal)
        } else if (result < 0) {
            conditionCode = 1; // CC = 01 (Less)
        } else {
            conditionCode = 2; // CC = 10 (Greater)
        }
        setConditionCode(conditionCode);
    }

    void setConditionCode(int conditionCode) {
        int currentSW = SW.getIntValue();
        SW.setValue((currentSW & 0xFFFFFC) | (conditionCode & 0x03));
    }


    ///  FETCH

    public void fetch() {
        currentOpcode = memory.readByte(PC.getIntValue());
    }

    ///  DECODE

    public void decode() {
        // Determina o formato e decodifica operandos
        instructionFormat = determineInstructionFormat(currentOpcode);
        operands = new int[0];
        indexed = false;
        extended = false;

        switch (instructionFormat) {
            case 1 -> decodeFormat1();
            case 2 -> decodeFormat2();
            case 3, 4 -> decodeFormat3or4();
        }

        // Atualiza PC com o tamanho total da instrução
        incrementPC(getInstructionSize());
    }


    // ================ MÉTODOS AUXILIARES DE DECODIFICAÇÃO ================
    private int determineInstructionFormat(int opcode) {
        // Mapeamento de opcodes para formatos (exemplos)
        return switch (opcode) {
            case 0x4C -> 3;          // RSUB (Formato 3)
            case 0x90, 0x4 -> 2;     // ADDR, CLEAR (Formato 2)
            case 0x00, 0x18, 0x3C -> 3; // LDA, ADD, J (Formato 3/4)
            default -> {
                // Verifica se é formato 4 (e=1)
                if ((memory.readByte(PC.getIntValue() + 1) & 0x01) != 0) yield 4;
                else yield 3;
            }
        };
    }

    private void decodeFormat1() {
        // Formato 1: 1 byte (sem operandos)
        operands = new int[0];
    }

    private void decodeFormat2() {
        // Formato 2: 2 bytes (r1, r2)
        int byte2 = memory.readByte(PC.getIntValue() + 1);
        operands = new int[]{(byte2 >> 4) & 0xF, byte2 & 0xF};
    }

    private void decodeFormat3or4() {
        // Formato 3/4: 3 ou 4 bytes (ni xbpe disp/address)
        int flags = memory.readByte(PC.getIntValue() + 1);
        extended = (flags & 0x01) != 0; // e=1 (formato 4)
        indexed = (flags & 0x10) != 0;  // x=1 (indexado)

        int addressField;
        if (extended) {
            // Formato 4: 20 bits de endereço
            addressField = (memory.readByte(PC.getIntValue() + 2) << 16)
                    | (memory.readByte(PC.getIntValue() + 3) << 8)
                    | memory.readByte(PC.getIntValue() + 4);
        } else {
            // Formato 3: 12 bits de deslocamento
            addressField = (memory.readByte(PC.getIntValue() + 2) << 8)
                    | memory.readByte(PC.getIntValue() + 3);
        }

        operands = new int[]{addressField, flags};
    }

    private int getInstructionSize() {
        return switch (instructionFormat) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            default -> throw new IllegalStateException("Formato inválido");
        };
    }

    // ================ GETTERS PARA DADOS DECODIFICADOS ================
    public int getInstructionFormat() {
        return instructionFormat;
    }

    public int[] getOperands() {
        return operands;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public boolean isExtended() {
        return extended;
    }

    ///  EXECUÇÃO

    public void execute() {
        switch (currentOpcode) {
            // ADD (Format 3/4 - Opcode 0x18)
            case 0x18 -> {
                int result = instructionSet.executeADD(
                        A.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                );
                A.setValue(result);
                updateConditionCode(result);
            }

            // ADDR (Format 2 - Opcode 0x90)
            case 0x90 -> {
                int[] regs = getOperands(); // [r1, r2]
                int result = instructionSet.executeADDR(
                        getRegisterById(regs[0]).getIntValue(),
                        getRegisterById(regs[1]).getIntValue()
                );
                getRegisterById(regs[1]).setValue(result);
                updateConditionCode(result);
            }

            // AND (Format 3/4 - Opcode 0x40)
            case 0x40 -> {
                int result = instructionSet.executeAND(
                        A.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                );
                A.setValue(result);
                updateConditionCode(result);
            }

            // CLEAR ou LDX, dependendo do formato. Ambas possuem OPCODE=4.
            case 0x04 -> {
                if (getInstructionFormat() == 2) { // CLEAR (Formato 2)
                    int reg = getOperands()[0]; // Extrai o registrador
                    getRegisterById(reg).setValue(instructionSet.executeCLEAR());
                } else { // LDX (Formato 3/4)
                    X.setValue(instructionSet.executeLDX(
                            getOperands()[0], // address
                            isIndexed(),      // indexed
                            X.getIntValue()
                    ));
                }
            }

            // COMP (Format 3/4 - Opcode 0x28)
            case 0x28 -> {
                int comparison = instructionSet.executeCOMP(
                        A.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                );
                updateConditionCode(comparison);
            }

            // DIV (Format 3/4 - Opcode 0x24)
            case 0x24 -> {
                int result = instructionSet.executeDIV(
                        A.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                );
                A.setValue(result);
                updateConditionCode(result);
            }

            // J (Format 3/4 - Opcode 0x3C)
            case 0x3C -> {
                PC.setValue(instructionSet.executeJ(
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                ));
            }

            // JEQ (Format 3/4 - Opcode 0x30)
            case 0x30 -> {
                int newPC = instructionSet.executeCONDITIONAL_JUMP(
                        0, // CC = 00 (Equal)
                        getOperands()[0], // address
                        getConditionCode()
                );
                if (newPC != -1) PC.setValue(newPC);
            }

            // JLT (Format 3/4 - Opcode 0x38)
            case 0x38 -> {
                int newPC = instructionSet.executeCONDITIONAL_JUMP(
                        1, // CC = 01 (Less)
                        getOperands()[0], // address
                        getConditionCode()
                );
                if (newPC != -1) PC.setValue(newPC);
            }

            // JGT (Format 3/4 - Opcode 0x34)
            case 0x34 -> {
                int newPC = instructionSet.executeCONDITIONAL_JUMP(
                        2, // CC = 10 (Greater)
                        getOperands()[0], // address
                        getConditionCode()
                );
                if (newPC != -1) PC.setValue(newPC);
            }

            // LDA (Format 3/4 - Opcode 0x00)
            case 0x00 -> {
                A.setValue(instructionSet.executeLDA(
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                ));
            }

            // LDCH (Format 3/4 - Opcode 0x50)
            case 0x50 -> {
                A.setValue(instructionSet.executeLDCH(
                        A.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                ));
            }

            // LDL (Format 3/4 - Opcode 0x08)
            case 0x08 -> {
                L.setValue(instructionSet.executeLDL(
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                ));
            }

            // RSUB (Format 3/4 - Opcode 0x4C)
            case 0x4C -> PC.setValue(instructionSet.executeRSUB(L.getIntValue()));

            // STA (Format 3/4 - Opcode 0x0C)
            case 0x0C -> {
                int address = instructionSet.calculateEffectiveAddress(
                        getOperands()[0], // address
                        X.getIntValue(),
                        isIndexed()
                );
                memory.writeWord(address, instructionSet.executeSTA(A.getIntValue()));
            }

            // STCH (Format 3/4 - Opcode 0x54)
            case 0x54 -> {
                int address = instructionSet.calculateEffectiveAddress(
                        getOperands()[0], // address
                        X.getIntValue(),
                        isIndexed()
                );
                memory.writeByte(address, instructionSet.executeSTCH(A.getIntValue()));
            }

            // STX (Format 3/4 - Opcode 0x10)
            case 0x10 -> {
                int address = instructionSet.calculateEffectiveAddress(
                        getOperands()[0], // address
                        X.getIntValue(),
                        isIndexed()
                );
                memory.writeWord(address, instructionSet.executeSTX(X.getIntValue()));
            }

            // SUB (Format 3/4 - Opcode 0x1C)
            case 0x1C -> {
                int result = instructionSet.executeSUB(
                        A.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                );
                A.setValue(result);
                updateConditionCode(result);
            }

            // TIX (Format 3/4 - Opcode 0x2C)
            case 0x2C -> {
                int comparison = instructionSet.executeTIX(
                        X.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                );
                X.setValue(X.getIntValue() + 1); // Incrementa X
                updateConditionCode(comparison);
            }

            // MUL (Format 3/4 - Opcode 0x20)
            case 0x20 -> {
                int result = instructionSet.executeMUL(
                        A.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                );
                A.setValue(result);
                updateConditionCode(result);
            }

            // JSUB (Format 3/4 - Opcode 0x48)
            case 0x48 -> {
                L.setValue(PC.getIntValue() + 3); // Salva endereço de retorno
                PC.setValue(instructionSet.executeJSUB(
                        PC.getIntValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                ));
            }

            // ADDF (Format 3/4 - Opcode 0x58)
            case 0x58 -> {
                F.setValue(instructionSet.executeADDF(
                        F.getLongValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                ));
            }

            // SUBF (Format 3/4 - Opcode 0x5C)
            case 0x5C -> {
                F.setValue(instructionSet.executeSUBF(
                        F.getLongValue(),
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                ));
            }

            // Instruções não implementadas
            default -> throw new IllegalStateException("Instrução não suportada: " + Integer.toHexString(currentOpcode));
        }
    }

    // TODO: Algumas instruções estão faltando.


}