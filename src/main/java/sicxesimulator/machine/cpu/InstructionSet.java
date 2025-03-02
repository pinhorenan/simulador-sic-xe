package sicxesimulator.machine.cpu;

import sicxesimulator.machine.Memory;

public class InstructionSet {
    private final Memory memory;

    public InstructionSet(Memory memory) {
        this.memory = memory;
    }

    ///  OPERAÇÕES BÁSICAS

    public int add(int a, int b) {
        return (a + b) & 0xFFFFFF; // Mantém 24 bits
    }

    public int subtract(int a, int b) {
        return (a - b) & 0xFFFFFF;
    }

    public int multiply(int a, int b) {
        return (a * b) & 0xFFFFFF;
    }

    public int divide(int a, int b) {
        if (b == 0) throw new ArithmeticException("Divisão por zero.");
        return (a / b) & 0xFFFFFF;
    }

    public int and(int a, int b) {
        return a & b;
    }

    public int or(int a, int b) {
        return a | b;
    }

    /// OPERAÇÕES DE MEMÓRIA

    public int readMemoryWord(int address) {
        return memory.readWord(address);
    }

    public void writeMemoryWord(int address, int value) {
        memory.writeWord(address, value);
    }

    /// CÁLCULO DO ENDEREÇO EFETIVO

    public int calculateEffectiveAddress(int base, int index, boolean indexed) {
        return indexed ? (base + index) : base;
    }

    /// LÓGICA DAS INSTRUÇÕES

    // ADD (Formato 2)
    public int executeADD(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = memory.readWord(effectiveAddress);
        return add(currentA, memValue);
    }

    // ADDR (Formato 2)
    public int executeADDR(int regValue1, int regValue2) {
        return add(regValue1, regValue2);
    }

    // AND (Formato 3)
    public int executeAND(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return and(currentA, memValue);
    }

    // CLEAR (Formato 2) - Retorna 0 (valor para definir no registrador)
    public int executeCLEAR() {
        return 0;
    }

    // COMP (Formato 3) - Retorna resultado da comparação para atualizar SW
    public int executeCOMP(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return currentA - memValue;
    }

    // DIV (Formato 3)
    public int executeDIV(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int divisor = readMemoryWord(effectiveAddress);
        if (divisor == 0) throw new ArithmeticException("Divisão por zero");
        return divide(currentA, divisor);
    }

    // J (Formato 3/4) - Retorna novo valor para PC
    public int executeJ(int address, boolean indexed, int indexRegValue) {
        return calculateEffectiveAddress(address, indexRegValue, indexed);
    }

    // JEQ/JLT/JGT (Formato 3/4) - Retorna novo PC ou -1 (sem branch)
    public int executeCONDITIONAL_JUMP(int conditionCode, int targetAddress, int currentCC) {
        if (currentCC == conditionCode) {
            return targetAddress;
        }
        return -1; // Indica que o salto não foi tomado
    }

    // LDA (Formato 3)
    public int executeLDA(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDCH (Formato 3)
    public int executeLDCH(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int byteValue = memory.readByte(effectiveAddress);
        return (currentA & 0xFFFF00) | byteValue;
    }

    // LDL (Formato 3)
    public int executeLDL(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDX (Formato 3)
    public int executeLDX(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // RSUB (Formato 3) - Retorna valor de L
    public int executeRSUB(int currentL) {
        return currentL;
    }

    // STA (Formato 3) - Retorna valor para escrever na memória
    public int executeSTA(int currentA) {
        return currentA;
    }

    // STCH (Formato 3) - Retorna byte para escrever na memória
    public int executeSTCH(int currentA) {
        return currentA & 0xFF;
    }

    // STX (Formato 3)
    public int executeSTX(int currentX) {
        return currentX;
    }

    // SUB (Formato 3)
    public int executeSUB(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return subtract(currentA, memValue);
    }

    // TIX (Formato 3)
    public int executeTIX(int currentX, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return (currentX + 1) - memValue; // Retorna comparação para atualizar SW
    }

    // MUL (Formato 3)
    public int executeMUL(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return multiply(currentA, memValue);
    }

    // JSUB (Formato 3)
    public int executeJSUB(int currentPC, int address, boolean indexed, int indexRegValue) {
        return calculateEffectiveAddress(address, indexRegValue, indexed);
    }

    // ADDF (Formato 3)
    public long executeADDF(long currentF, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        long value2 = ((long) readMemoryWord(effectiveAddress) << 24) | readMemoryWord(effectiveAddress + 3);
        return currentF + value2;
    }

    // SUBF (Formato 3)
    public long executeSUBF(long currentF, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        long value2 = ((long) readMemoryWord(effectiveAddress) << 24) | readMemoryWord(effectiveAddress + 3);
        return currentF - value2;
    }
}