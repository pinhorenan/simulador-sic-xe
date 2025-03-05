package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;

public class InstructionSet {
    private final Memory memory;
    private final ControlUnit controlUnit;

    public InstructionSet(Memory memory, ControlUnit controlUnit) {
        this.memory = memory;
        this.controlUnit = controlUnit;
    }

    /// OPERAÇÕES BÁSICAS

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

    /**
     * Lê uma palavra (3 bytes) da memória e converte para um inteiro de 24 bits.
     * @param wordAddress Índice da palavra na memória.
     * @return Inteiro representando os 3 bytes.
     */
    public int readMemoryWord(int wordAddress) {
        byte[] wordBytes = memory.readWord(wordAddress);
        return ((wordBytes[0] & 0xFF) << 16) | ((wordBytes[1] & 0xFF) << 8) | (wordBytes[2] & 0xFF);
    }

    /// CÁLCULO DO ENDEREÇO EFETIVO

    /**
     * Calcula o endereço efetivo (índice de palavra) somando o deslocamento à base
     * e, se o modo indexado estiver ativo, adiciona o valor do registrador X.
     * @param disp Deslocamento (em palavras)
     * @param X Valor do registrador X (em palavras)
     * @param indexed Se a instrução é indexada.
     * @return Índice da palavra com o endereço efetivo.
     */
    public int calculateEffectiveAddress(int disp, int X, boolean indexed) {
        int effectiveAddress = controlUnit.getBaseAddress() + disp;
        if (indexed) {
            effectiveAddress += X;
        }
        return effectiveAddress;
    }

    /// LÓGICA DAS INSTRUÇÕES

    // ADD (Formato 3)
    public int executeADD(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
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

    // CLEAR (Formato 2) - Retorna 0
    public int executeCLEAR() {
        return 0;
    }

    // COMP (Formato 3) - Retorna a diferença para atualização do SW
    public int executeCOMP(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return currentA - memValue;
    }

    // COMPR (Formato 2)
    public int executeCOMPR(int reg1Value, int reg2Value) {
        return reg1Value - reg2Value;
    }

    // DIV (Formato 3)
    public int executeDIV(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int divisor = readMemoryWord(effectiveAddress);
        if (divisor == 0) throw new ArithmeticException("Divisão por zero");
        return divide(currentA, divisor);
    }

    // DIVR (Formato 2)
    public int executeDIVR(int reg1Value, int reg2Value) {
        if (reg2Value == 0) throw new ArithmeticException("Divisão por zero.");
        return divide(reg1Value, reg2Value);
    }

    // J (Formato 3/4) - Retorna novo valor para o PC (índice de palavra)
    public int executeJ(int address, boolean indexed, int indexRegValue) {
        return calculateEffectiveAddress(address, indexRegValue, indexed);
    }

    // JEQ/JGT/JLT (Formato 3/4) - Retorna novo PC ou -1 se não houver branch
    public int executeCONDITIONAL_JUMP(int conditionCode, int targetAddress, int currentCC) {
        if (currentCC == conditionCode) {
            return targetAddress;
        }
        return -1;
    }

    // JSUB (Formato 3) - Retorna o endereço efetivo para saltar
    public int executeJSUB(int address, boolean indexed, int indexRegValue) {
        return calculateEffectiveAddress(address, indexRegValue, indexed);
    }

    // LDA (Formato 3)
    public int executeLDA(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDB (Formato 3/4)
    public int executeLDB(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDCH (Formato 3)
    public int executeLDCH(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        // Para LDCH, lemos um byte do offset 2 (mesmo utilizado em STCH)
        int byteValue = memory.readByte(effectiveAddress, 2) & 0xFF;
        return (currentA & 0xFFFF00) | byteValue;
    }

    // LDL (Formato 3)
    public int executeLDL(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDS (Formato 3/4)
    public int executeLDS(int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        return readMemoryWord(effectiveAddress);
    }

    // LDT (Formato 3/4)
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

    // MULR (Formato 2)
    public int executeMULR(int reg1Value, int reg2Value) {
        return multiply(reg1Value, reg2Value);
    }

    // OR (Formato 3/4)
    public int executeOR(int currentA, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return or(currentA, memValue);
    }

    // RMO (Formato 2)
    // Aqui não há implementação, pois a operação é simples.

    // RSUB (Formato 3) - Retorna o valor de L
    // Aqui não há implementação, pois a operação é simples.

    // SHIFTL (Formato 2)
    public int executeSHIFTL(int regValue, int count) {
        return (regValue << count) & 0xFFFFFF;
    }

    // SHIFTR (Formato 2)
    public int executeSHIFTR(int regValue, int count) {
        return (regValue >>> count) & 0xFFFFFF;
    }

    // STA (Formato 3) - Retorna o valor do registrador A para armazenar
    public int executeSTA(int currentA) {
        return currentA;
    }

    // STB (Formato 3/4)
    public int executeSTB(int currentB) {
        return currentB;
    }

    // STCH (Formato 3) - Retorna o byte menos significativo de A para armazenar
    public int executeSTCH(int currentA) {
        return currentA & 0xFF;
    }

    // STL (Formato 3/4)
    public int executeSTL(int currentL) {
        return currentL;
    }

    // STS (Formato 3/4)
    public int executeSTS(int currentS) {
        return currentS;
    }

    // STT (Formato 3/4)
    public int executeSTT(int currentT) {
        return currentT;
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

    // SUBR (Formato 2)
    public int executeSUBR(int reg1Value, int reg2Value) {
        return subtract(reg1Value, reg2Value);
    }

    // TIX (Formato 3)
    public int executeTIX(int currentX, int address, boolean indexed, int indexRegValue) {
        int effectiveAddress = calculateEffectiveAddress(address, indexRegValue, indexed);
        int memValue = readMemoryWord(effectiveAddress);
        return (currentX + 1) - memValue;
    }

    // TIXR (Formato 2)
    public int executeTIXR(int currentX, int regValue) {
        return (currentX + 1) - regValue;
    }
}
