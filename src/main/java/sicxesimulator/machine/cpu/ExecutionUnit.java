package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;
import sicxesimulator.utils.Convert;

public class ExecutionUnit {
    private final RegisterSet registers;
    private final Memory memory;
    private String lastExecutionLog;

    public ExecutionUnit(RegisterSet registers, Memory memory) {
        this.registers = registers;
        this.memory = memory;
    }

    public String execute(Instruction instruction) {
        int currentOpcode = instruction.getOpcode();
        int[] operands = instruction.getOperands();
        boolean indexed = instruction.isIndexed();

        switch (currentOpcode) {
            case 0x18 -> executeADD(operands, indexed);    // ADD
            case 0x90 -> executeADDR(operands);            // ADDR
            case 0x40 -> executeAND(operands, indexed);    // AND
            case 0x04 -> executeCLEAR_LDX(instruction, operands);       // CLEAR/LDX
            case 0x28 -> executeCOMP(operands, indexed);   // COMP
            case 0xA0 -> executeCOMPR(operands);           // COMPR
            case 0x24 -> executeDIV(operands, indexed);    // DIV
            case 0x9C -> executeDIVR(operands);            // DIVR
            case 0x3C -> executeJ(operands, indexed);      // J
            case 0x30 -> executeJEQ(operands, indexed);    // JEQ
            case 0x34 -> executeJGT(operands, indexed);    // JGT
            case 0x38 -> executeJLT(operands, indexed);    // JLT
            case 0x48 -> executeJSUB(operands, indexed);   // JSUB
            case 0x00 -> executeLDA(operands, indexed);    // LDA
            case 0x68 -> executeLDB(operands, indexed);    // LDB
            case 0x50 -> executeLDCH(operands, indexed);   // LDCH
            case 0x08 -> executeLDL(operands, indexed);    // LDL
            case 0x6C -> executeLDS(operands, indexed);    // LDS
            case 0x74 -> executeLDT(operands, indexed);    // LDT
            case 0x20 -> executeMUL(operands, indexed);    // MUL
            case 0x98 -> executeMULR(operands);            // MULR
            case 0x44 -> executeOR(operands, indexed);     // OR
            case 0xAC -> executeRMO(operands);             // RMO
            case 0x4C -> executeRSUB();                    // RSUB
            case 0xA4 -> executeSHIFTL(operands);          // SHIFTL
            case 0xA8 -> executeSHIFTR(operands);          // SHIFTR
            case 0x0C -> executeSTA(operands, indexed);    // STA
            case 0x78 -> executeSTB(operands, indexed);    // STB
            case 0x54 -> executeSTCH(operands, indexed);   // STCH
            case 0x14 -> executeSTL(operands, indexed);    // STL
            case 0x7C -> executeSTS(operands, indexed);    // STS
            case 0x84 -> executeSTT(operands, indexed);    // STT
            case 0x10 -> executeSTX(operands, indexed);    // STX
            case 0x1C -> executeSUB(operands, indexed);    // SUB
            case 0x94 -> executeSUBR(operands);            // SUBR
            case 0x2C -> executeTIX(operands, indexed);    // TIX
            case 0xB8 -> executeTIXR(operands);            // TIXR
            default -> throw new IllegalStateException("Instrução não implementada: " + Integer.toHexString(currentOpcode));
        }

        return getLastExecutionLog();
    }

    // ----- Implementações das instruções -----

    private void executeADD(int[] operands, boolean indexed) {
        Register A = registers.getRegister("A");
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() + operandValue;
        A.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("ADD: Resultado = %06X", result);
    }

    private void executeADDR(int[] operands) {
        int r1 = operands[0];
        int r2 = operands[1];
        Register reg1 = getRegisterByNumber(r1);
        Register reg2 = getRegisterByNumber(r2);

        int result = reg1.getIntValue() + reg2.getIntValue();
        reg2.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("ADDR: R%d + R%d = %06X", r1, r2, result);
    }

    private void executeAND(int[] operands, boolean indexed) {
        Register A = registers.getRegister("A");
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() & operandValue;
        A.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("AND: Resultado = %06X", result);
    }

    private void executeCLEAR_LDX(Instruction instruction, int[] operands) {
        if (operands.length == 1) { // CLEAR
            Register reg = getRegisterByNumber(operands[0]);
            reg.setValue(0);
            lastExecutionLog = String.format("CLEAR R%d", operands[0]);
        } else { // LDX
            int effectiveAddress = calculateEffectiveAddress(operands[0], instruction.isIndexed());
            byte[] wordBytes = memory.readWord(effectiveAddress / 3);
            registers.getRegister("X").setValue(Convert.bytesToInt(wordBytes));
            lastExecutionLog = String.format("LDX: Carregado %06X", Convert.bytesToInt(wordBytes));
        }
    }

    // COMP (Comparar com memória)
    private void executeCOMP(int[] operands, boolean indexed) {
        Register A = registers.getRegister("A");
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int memValue = Convert.bytesToInt(wordBytes);

        int comparison = A.getIntValue() - memValue;
        updateConditionCode(comparison);
        lastExecutionLog = String.format("COMP: A=%06X vs Mem[%06X]=%06X => %s",
                A.getIntValue(), effectiveAddress, memValue, getConditionCodeDescription());
    }

    // COMPR (Comparar registradores)
    private void executeCOMPR(int[] operands) {
        Register r1 = getRegisterByNumber(operands[0]);
        Register r2 = getRegisterByNumber(operands[1]);
        int comparison = r1.getIntValue() - r2.getIntValue();

        updateConditionCode(comparison);
        lastExecutionLog = String.format("COMPR: R%d=%06X vs R%d=%06X => %s",
                operands[0], r1.getIntValue(), operands[1], r2.getIntValue(), getConditionCodeDescription());
    }

    // DIV (Dividir)
    private void executeDIV(int[] operands, boolean indexed) {
        Register A = registers.getRegister("A");
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int divisor = Convert.bytesToInt(wordBytes);

        if (divisor == 0) throw new ArithmeticException("Divisão por zero");
        int result = A.getIntValue() / divisor;
        A.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("DIV: Resultado = %06X", result);
    }

    // DIVR (Dividir registradores)
    private void executeDIVR(int[] operands) {
        Register r1 = getRegisterByNumber(operands[0]);
        Register r2 = getRegisterByNumber(operands[1]);

        if (r2.getIntValue() == 0) throw new ArithmeticException("Divisão por zero");
        int result = r1.getIntValue() / r2.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("DIVR: R%d / R%d = %06X", operands[0], operands[1], result);
    }

    // J (Jump incondicional)
    private void executeJ(int[] operands, boolean indexed) {
        int targetAddress = calculateEffectiveAddress(operands[0], indexed);
        registers.getRegister("PC").setValue(targetAddress);
        lastExecutionLog = String.format("J: PC ← %06X", targetAddress);
    }

    // JEQ (Jump se igual)
    private void executeJEQ(int[] operands, boolean indexed) {
        if (registers.getRegister("SW").getIntValue() == 0) {
            int targetAddress = calculateEffectiveAddress(operands[0], indexed);
            registers.getRegister("PC").setValue(targetAddress);
            lastExecutionLog = String.format("JEQ: PC ← %06X", targetAddress);
        } else {
            lastExecutionLog = "JEQ: Condição não satisfeita";
        }
    }

    // JGT (Jump se maior)
    private void executeJGT(int[] operands, boolean indexed) {
        if (registers.getRegister("SW").getIntValue() == 2) {
            int targetAddress = calculateEffectiveAddress(operands[0], indexed);
            registers.getRegister("PC").setValue(targetAddress);
            lastExecutionLog = String.format("JGT: PC ← %06X", targetAddress);
        } else {
            lastExecutionLog = "JGT: Condição não satisfeita";
        }
    }

    // JLT (Jump se menor)
    private void executeJLT(int[] operands, boolean indexed) {
        if (registers.getRegister("SW").getIntValue() == 1) {
            int targetAddress = calculateEffectiveAddress(operands[0], indexed);
            registers.getRegister("PC").setValue(targetAddress);
            lastExecutionLog = String.format("JLT: PC ← %06X", targetAddress);
        } else {
            lastExecutionLog = "JLT: Condição não satisfeita";
        }
    }

    // JSUB (Jump para sub-rotina)
    private void executeJSUB(int[] operands, boolean indexed) {
        int returnAddress = registers.getRegister("PC").getIntValue();
        registers.getRegister("L").setValue(returnAddress);

        int targetAddress = calculateEffectiveAddress(operands[0], indexed);
        registers.getRegister("PC").setValue(targetAddress);
        lastExecutionLog = String.format("JSUB: PC ← %06X | L=%06X", targetAddress, returnAddress);
    }

    // LDA (Carregar A)
    private void executeLDA(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("A").setValue(Convert.bytesToInt(wordBytes));
        lastExecutionLog = String.format("LDA: A ← %06X", Convert.bytesToInt(wordBytes));
    }

    // LDB (Carregar B)
    private void executeLDB(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("B").setValue(Convert.bytesToInt(wordBytes));
        lastExecutionLog = String.format("LDB: B ← %06X", Convert.bytesToInt(wordBytes));
    }

    // LDCH (Carregar caractere)
    private void executeLDCH(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        int byteValue = memory.readByte(effectiveAddress / 3, effectiveAddress % 3);
        registers.getRegister("A").setValue(byteValue & 0xFF);
        lastExecutionLog = String.format("LDCH: A[byte] ← %02X", byteValue);
    }

    // LDL (Carregar L)
    private void executeLDL(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("L").setValue(Convert.bytesToInt(wordBytes));
        lastExecutionLog = String.format("LDL: L ← %06X", Convert.bytesToInt(wordBytes));
    }

    // MUL (Multiplicar)
    private void executeMUL(int[] operands, boolean indexed) {
        Register A = registers.getRegister("A");
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() * operandValue;
        A.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("MUL: Resultado = %06X", result);
    }

    // MULR (Multiplicar registradores)
    private void executeMULR(int[] operands) {
        Register r1 = getRegisterByNumber(operands[0]);
        Register r2 = getRegisterByNumber(operands[1]);

        int result = r1.getIntValue() * r2.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("MULR: R%d * R%d = %06X", operands[0], operands[1], result);
    }

    // OR (Operação OR)
    private void executeOR(int[] operands, boolean indexed) {
        Register A = registers.getRegister("A");
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() | operandValue;
        A.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("OR: Resultado = %06X", result);
    }

    // RMO (Mover registrador)
    private void executeRMO(int[] operands) {
        Register source = getRegisterByNumber(operands[0]);
        Register dest = getRegisterByNumber(operands[1]);

        dest.setValue(source.getIntValue());
        lastExecutionLog = String.format("RMO: R%d → R%d | Valor=%06X", operands[0], operands[1], source.getIntValue());
    }

    // SHIFTL (Deslocar à esquerda)
    private void executeSHIFTL(int[] operands) {
        Register reg = getRegisterByNumber(operands[0]);
        int count = operands[1];
        int value = reg.getIntValue() << count;

        reg.setValue(value);
        updateConditionCode(value);
        lastExecutionLog = String.format("SHIFTL: R%d << %d = %06X", operands[0], count, value);
    }

    // SHIFTR (Deslocar à direita)
    private void executeSHIFTR(int[] operands) {
        Register reg = getRegisterByNumber(operands[0]);
        int count = operands[1];
        int value = reg.getIntValue() >>> count; // Deslocamento lógico

        reg.setValue(value);
        updateConditionCode(value);
        lastExecutionLog = String.format("SHIFTR: R%d >> %d = %06X", operands[0], count, value);
    }

    // STA (Armazenar A)
    private void executeSTA(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        int value = registers.getRegister("A").getIntValue();

        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));
        lastExecutionLog = String.format("STA: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    // SUB (Subtrair)
    private void executeSUB(int[] operands, boolean indexed) {
        Register A = registers.getRegister("A");
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() - operandValue;
        A.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("SUB: Resultado = %06X", result);
    }

    // SUBR (Subtrair registradores)
    private void executeSUBR(int[] operands) {
        Register r1 = getRegisterByNumber(operands[0]);
        Register r2 = getRegisterByNumber(operands[1]);

        int result = r2.getIntValue() - r1.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);
        lastExecutionLog = String.format("SUBR: R%d - R%d = %06X", operands[1], operands[0], result);
    }

    // TIX (Incrementar e comparar)
    private void executeTIX(int[] operands, boolean indexed) {
        Register X = registers.getRegister("X");
        X.setValue(X.getIntValue() + 1);

        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int memValue = Convert.bytesToInt(wordBytes);

        int comparison = X.getIntValue() - memValue;
        updateConditionCode(comparison);
        lastExecutionLog = String.format("TIX: X=%06X vs Mem[%06X]=%06X => %s",
                X.getIntValue(), effectiveAddress, memValue, getConditionCodeDescription());
    }

    // TIXR (Incrementar e comparar com registrador)
    private void executeTIXR(int[] operands) {
        Register X = registers.getRegister("X");
        X.setValue(X.getIntValue() + 1);

        Register r = getRegisterByNumber(operands[0]);
        int comparison = X.getIntValue() - r.getIntValue();

        updateConditionCode(comparison);
        lastExecutionLog = String.format("TIXR: X=%06X vs R%d=%06X => %s",
                X.getIntValue(), operands[0], r.getIntValue(), getConditionCodeDescription());
    }


    private void executeRSUB() {
        int returnAddress = registers.getRegister("L").getIntValue();
        registers.getRegister("PC").setValue(returnAddress);
        lastExecutionLog = String.format("RSUB: Retornando para %06X", returnAddress);
    }

    // LDS (Carregar S)
    private void executeLDS(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("S").setValue(Convert.bytesToInt(wordBytes));
        lastExecutionLog = String.format("LDS: S ← %06X", Convert.bytesToInt(wordBytes));
    }

    // LDT (Carregar T)
    private void executeLDT(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("T").setValue(Convert.bytesToInt(wordBytes));
        lastExecutionLog = String.format("LDT: T ← %06X", Convert.bytesToInt(wordBytes));
    }

    // STB (Armazenar B)
    private void executeSTB(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        int value = registers.getRegister("B").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));
        lastExecutionLog = String.format("STB: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    // STCH (Armazenar caractere)
    private void executeSTCH(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        int byteValue = registers.getRegister("A").getIntValue() & 0xFF; // Pega o byte menos significativo
        memory.writeByte(effectiveAddress / 3, effectiveAddress % 3, byteValue);
        lastExecutionLog = String.format("STCH: Mem[%06X] ← %02X", effectiveAddress, byteValue);
    }

    // STL (Armazenar L)
    private void executeSTL(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        int value = registers.getRegister("L").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));
        lastExecutionLog = String.format("STL: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    // STS (Armazenar S)
    private void executeSTS(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        int value = registers.getRegister("S").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));
        lastExecutionLog = String.format("STS: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    // STT (Armazenar T)
    private void executeSTT(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        int value = registers.getRegister("T").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));
        lastExecutionLog = String.format("STT: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    // STX (Armazenar X)
    private void executeSTX(int[] operands, boolean indexed) {
        int effectiveAddress = calculateEffectiveAddress(operands[0], indexed);
        int value = registers.getRegister("X").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));
        lastExecutionLog = String.format("STX: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    // ----- Métodos auxiliares -----

    private int calculateEffectiveAddress(int address, boolean indexed) {
        return indexed ? address + registers.getRegister("X").getIntValue() : address;
    }

    private Register getRegisterByNumber(int num) {
        return switch (num) {
            case 0 -> registers.getRegister("A");
            case 1 -> registers.getRegister("X");
            case 2 -> registers.getRegister("L");
            case 3 -> registers.getRegister("B");
            case 4 -> registers.getRegister("S");
            case 5 -> registers.getRegister("T");
            default -> throw new IllegalArgumentException("Registrador inválido: " + num);
        };
    }

    private void updateConditionCode(int value) {
        int cc = (value == 0) ? 0 : (value < 0 ? 1 : 2);
        registers.getRegister("SW").setValue(cc);
    }

    private String getConditionCodeDescription() {
        return switch (registers.getRegister("SW").getIntValue()) {
            case 0 -> "Igual";
            case 1 -> "Menor";
            case 2 -> "Maior";
            default -> "Desconhecido";
        };
    }

    public String getLastExecutionLog() {
        return lastExecutionLog;
    }
}