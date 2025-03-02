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

    // COMPR (Format 2)
    public int executeCOMPR(int reg1Value, int reg2Value) {
        return reg1Value - reg2Value; // Retorna a diferença para atualizar o Condition Code
    }

    // DIV (Formato 3)
    public int executeDIV(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int divisor = readMemoryWord(effectiveAddress);
        if (divisor == 0) throw new ArithmeticException("Divisão por zero");
        return divide(currentA, divisor);
    }

    // DIVR (Format 2)
    public int executeDIVR(int reg1Value, int reg2Value) {
        if (reg2Value == 0) throw new ArithmeticException("Divisão por zero.");
        return divide(reg1Value, reg2Value);
    }

    // J (Formato 3/4) - Retorna novo valor para PC
    public int executeJ(int address, boolean indexed, int indexRegValue) {
        return calculateEffectiveAddress(address, indexRegValue, indexed);
    }

    // JEQ/JGT/JLT/ (Formato 3/4) - Retorna novo PC ou -1 (sem branch)
    public int executeCONDITIONAL_JUMP(int conditionCode, int targetAddress, int currentCC) {
        if (currentCC == conditionCode) {
            return targetAddress;
        }
        return -1; // Indica que o salto não foi tomado
    }

    // JSUB (Formato 3)
    public int executeJSUB (int address, boolean indexed, int indexRegValue) {
        return calculateEffectiveAddress(address, indexRegValue, indexed);
    }

    // LDA (Formato 3)
    public int executeLDA(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDB (Format 3/4)
    public int executeLDB(int address, boolean indexed, int indexRegValue) {
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

    // LDS (Format 3/4)
    public int executeLDS(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDT (Format 3/4)
    public int executeLDT(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDX (Formato 3)
    public int executeLDX(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // MUL (Formato 3)
    public int executeMUL(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return multiply(currentA, memValue);
    }

    // MULR (Format 2)
    public int executeMULR(int reg1Value, int reg2Value) {
        return multiply(reg1Value, reg2Value);
    }

    // OR (Format 3/4)
    public int executeOR(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return or(currentA, memValue);
    }

    // RMO (Format 2)
    // Não necessita de implementação aqui devido à sua simplicidade, talvez eu mude de ideia depois.

    // RSUB (Formato 3) - Retorna valor de L
    public int executeRSUB(int currentL) {
        return currentL;
    }

    // SHIFTL (Format 2)
    public int executeSHIFTL(int regValue, int count) {
        return (regValue << count) & 0xFFFFFF; // Deslocamento à esquerda
    }

    // SHIFTR (Format 2)
    public int executeSHIFTR(int regValue, int count) {
        return (regValue >>> count) & 0xFFFFFF; // Deslocamento à direita (com preenchimento de zeros)
    }

    // STA (Formato 3) - Retorna valor para escrever na memória
    public int executeSTA(int currentA) {
        return currentA;
    }

    // STB (Format 3/4)
    public int executeSTB(int currentB) {
        return currentB; // Retorna o valor do registrador B para escrever na memória
    }

    // STCH (Formato 3) - Retorna byte para escrever na memória
    public int executeSTCH(int currentA) {
        return currentA & 0xFF;
    }

    // STL (Format 3/4)
    public int executeSTL(int currentL) {
        return currentL; // Retorna o valor do registrador L para escrever na memória
    }

    // STS (Format 3/4)
    public int executeSTS(int currentS) {
        return currentS; // Retorna o valor do registrador S para escrever na memória
    }

    // STT (Format 3/4)
    public int executeSTT(int currentT) {
        return currentT; // Retorna o valor do registrador T para escrever na memória
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

    // SUBR (Format 2)
    public int executeSUBR(int reg1Value, int reg2Value) {
        return subtract(reg1Value, reg2Value);
    }

    // TIX (Formato 3)
    public int executeTIX(int currentX, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return (currentX + 1) - memValue; // Retorna comparação para atualizar SW
    }

    // TIXR (Format 2)
    public int executeTIXR(int currentX, int regValue) {
        return (currentX + 1) - regValue; // Retorna a comparação para atualizar o Condition Code
    }
}