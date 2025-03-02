package sicxesimulator.machine.cpu;

import sicxesimulator.machine.Memory;

import java.util.Arrays;

public class ControlUnit {
    private final Memory memory;
    private final InstructionSet instructionSet;

    // Registradores do SIC/XE
    private final Register A, X, L, B, S, T, F;
    private final Register PC;
    private final Register SW;
    private final Register[] registers;

    // Variáveis para o ciclo de instrução
    private int currentOpcode;
    private int instructionFormat; // 1, 2, 3 ou 4
    private int[] operands;        // Operandos decodificados
    private boolean indexed;       // Modo indexado
    private boolean extended;      // Flag "e" (formato 4)

    // Flag para indicar se a execução foi encerrada
    private boolean halted = false;

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
     * Executa um ciclo completo: fetch, decode e execute.
     * Se a execução for finalizada (por exemplo, através de uma instrução RSUB que retorne 0), marca halted como true.
     */
    public void runCycle() {
        if (halted) return;
        fetch();
        decode();
        execute();
    }

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
     * Para opcodes não explicitamente mapeados, verifica o flag "e" no byte seguinte para distinguir entre
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
        if (extended) {
            // Formato 4: 4 bytes no total.
            // Endereço (20 bits) = (lower nibble de byte1 << 16) | (byte2 << 8) | (byte3)
            int lowNibble = byte1 & 0x0F;
            int byte2 = memory.readByte(PC.getIntValue() + 2) & 0xFF;
            int byte3 = memory.readByte(PC.getIntValue() + 3) & 0xFF;
            addressField = (lowNibble << 16) | (byte2 << 8) | byte3;
        } else {
            // Formato 3: 3 bytes no total.
            // Deslocamento de 12 bits = (lower nibble de byte1 << 8) | (byte2)
            int lowNibble = byte1 & 0x0F;
            int byte2 = memory.readByte(PC.getIntValue() + 2) & 0xFF;
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

    // Getters para os dados decodificados
    public int getInstructionFormat() { return instructionFormat; }
    public int[] getOperands() { return operands; }
    public boolean isIndexed() { return indexed; }
    public boolean isExtended() { return extended; }


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

            // COMPR
            case 0xA0 -> {
                //TODO
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

            // DIVR
            case 0x9C -> {
                // TODO
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

            // JGT (Format 3/4 - Opcode 0x34)
            case 0x34 -> {
                int newPC = instructionSet.executeCONDITIONAL_JUMP(
                        2, // CC = 10 (Greater)
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

            // LDA (Format 3/4 - Opcode 0x00)
            case 0x00 -> {
                A.setValue(instructionSet.executeLDA(
                        getOperands()[0], // address
                        isIndexed(),      // indexed
                        X.getIntValue()
                ));
            }

            // LDB
            case 0x68 -> {
                // TODO
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

            // LDS
            case 0x6c -> {
                // TODO
            }

            // LDT
            case 0x74 -> {
                // TODO
            }

            // LDX
            // Já coberto no condicional de clear, já que ambas possuem mesmo OPCODE

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

            // MULR
            case 0x98 -> {
                // TODO
            }

            // OR
            case 0x44 -> {
                // TODO
            }

            // RMO
            case 0xAC -> {
                // TODO
            }

            // RSUB (Format 3/4 - Opcode 0x4C)
            case 0x4C -> PC.setValue(instructionSet.executeRSUB(L.getIntValue()));

            // SHIFTL
            case 0xA4 -> {
                // TODO
            }

            // SHIFTR
            case 0xA8 -> {
                // TODO
            }

            // STA (Format 3/4 - Opcode 0x0C)
            case 0x0C -> {
                int address = instructionSet.calculateEffectiveAddress(
                        getOperands()[0], // address
                        X.getIntValue(),
                        isIndexed()
                );
                memory.writeWord(address, instructionSet.executeSTA(A.getIntValue()));
            }

            // STB
            case 0x78 -> {
                // TODO
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

            // STL
            case 0x14 -> {
                // TODO
            }

            // STS
            case 0x7C -> {
                // TODO
            }

            // STT
            case 0x84 -> {
                // TODO
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

            // SUBR
            case 0x94 -> {
                // TODO
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

            // TIXR
            case 0xB8 -> {
                // TODO
            }

            // Instruções não implementadas
            default -> throw new IllegalStateException(String.format("Instrução não suportada: %02X", currentOpcode));
        }
    }

    // Getter para verificar se a execução foi encerrada
    public boolean isHalted() {
        return halted;
    }
}