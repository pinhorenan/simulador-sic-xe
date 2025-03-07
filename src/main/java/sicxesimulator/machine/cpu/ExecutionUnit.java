package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;
import sicxesimulator.utils.Convert;

public class ExecutionUnit {
    private final RegisterSet registers;
    private final Memory memory;

    public ExecutionUnit(RegisterSet registers, Memory memory) {
        this.registers = registers;
        this.memory = memory;
    }

    public String executeADD(int[] operands, boolean indexed, int effectiveAddress) {
        Register A = registers.getRegister("A");
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() + operandValue;
        A.setValue(result);
        updateConditionCode(result);
        return String.format("ADD: Resultado = %06X", result);
    }

    public String executeADDR(int[] operands) {
        int r1 = operands[0];
        int r2 = operands[1];
        Register reg1 = getRegisterByNumber(r1);
        Register reg2 = getRegisterByNumber(r2);

        int result = reg1.getIntValue() + reg2.getIntValue();
        reg2.setValue(result);
        updateConditionCode(result);

        return String.format("ADDR: R%d + R%d = %06X", r1, r2, result);
    }

    public String executeAND(int[] operands, boolean indexed, int effectiveAddress) {
        Register A = registers.getRegister("A");
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() & operandValue;
        A.setValue(result);
        updateConditionCode(result);

        return String.format("AND: Resultado = %06X", result);
    }

    public String executeCLEAR_LDX(Instruction instruction, int[] operands) {
        if (operands.length == 1) { // CLEAR
            Register reg = getRegisterByNumber(operands[0]);
            reg.setValue(0);

            return String.format("CLEAR R%d", operands[0]);
        } else { // LDX
            int effectiveAddress = instruction.getEffectiveAddress();
            byte[] wordBytes = memory.readWord(effectiveAddress / 3);
            registers.getRegister("X").setValue(Convert.bytesToInt(wordBytes));

            return String.format("LDX: Carregado %06X", Convert.bytesToInt(wordBytes));
        }
    }

    public String executeCOMP(int[] operands, boolean indexed, int effectiveAddress) {
        Register A = registers.getRegister("A");
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int memValue = Convert.bytesToInt(wordBytes);

        int comparison = A.getIntValue() - memValue;
        updateConditionCode(comparison);

        return String.format("COMP: A=%06X vs Mem[%06X]=%06X => %s",
                A.getIntValue(), effectiveAddress, memValue, getConditionCodeDescription());
    }

    public String executeCOMPR(int[] operands) {
        Register r1 = getRegisterByNumber(operands[0]);
        Register r2 = getRegisterByNumber(operands[1]);
        int comparison = r1.getIntValue() - r2.getIntValue();

        updateConditionCode(comparison);

        return String.format("COMPR: R%d=%06X vs R%d=%06X => %s",
                operands[0], r1.getIntValue(), operands[1], r2.getIntValue(), getConditionCodeDescription());
    }

    public String executeDIV(int[] operands, boolean indexed, int effectiveAddress) {
        Register A = registers.getRegister("A");
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int divisor = Convert.bytesToInt(wordBytes);

        if (divisor == 0) throw new ArithmeticException("Divisão por zero");
        int result = A.getIntValue() / divisor;
        A.setValue(result);
        updateConditionCode(result);

        return String.format("DIV: Resultado = %06X", result);
    }

    public String executeDIVR(int[] operands) {
        Register r1 = getRegisterByNumber(operands[0]);
        Register r2 = getRegisterByNumber(operands[1]);

        if (r2.getIntValue() == 0) throw new ArithmeticException("Divisão por zero");
        int result = r1.getIntValue() / r2.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);

        return String.format("DIVR: R%d / R%d = %06X", operands[0], operands[1], result);
    }

    public String executeJ(int[] operands, boolean indexed, int effectiveAddress) {
        registers.getRegister("PC").setValue(effectiveAddress);

        return String.format("J: PC ← %06X", effectiveAddress);
    }

    public String executeJEQ(int[] operands, boolean indexed, int effectiveAddress) {
        if (registers.getRegister("SW").getIntValue() == 0) {
            registers.getRegister("PC").setValue(effectiveAddress);
            return String.format("JEQ: PC ← %06X", effectiveAddress);
        } else {
            return "JEQ: Condição não satisfeita";
        }
    }

    public String executeJGT(int[] operands, boolean indexed, int effectiveAddress) {
        if (registers.getRegister("SW").getIntValue() == 2) {
            registers.getRegister("PC").setValue(effectiveAddress);
            return String.format("JGT: PC ← %06X", effectiveAddress);
        } else {
            return "JGT: Condição não satisfeita";
        }
    }

    public String executeJLT(int[] operands, boolean indexed, int effectiveAddress) {
        if (registers.getRegister("SW").getIntValue() == 1) {
            registers.getRegister("PC").setValue(effectiveAddress);
            return String.format("JLT: PC ← %06X", effectiveAddress);
        } else {
            return "JLT: Condição não satisfeita";
        }
    }

    public String executeJSUB(int[] operands, boolean indexed, int effectiveAddress) {
        int returnAddress = registers.getRegister("PC").getIntValue();
        registers.getRegister("L").setValue(returnAddress);
        registers.getRegister("PC").setValue(effectiveAddress);
        return String.format("JSUB: PC ← %06X | L=%06X", effectiveAddress, returnAddress);
    }

    public String executeLDA(int[] operands, boolean indexed, int effectiveAddress) {
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("A").setValue(Convert.bytesToInt(wordBytes));
        return String.format("LDA: A ← %06X", Convert.bytesToInt(wordBytes));
    }

    public String executeLDB(int[] operands, boolean indexed, int effectiveAddress) {
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("B").setValue(Convert.bytesToInt(wordBytes));
        return String.format("LDB: B ← %06X", Convert.bytesToInt(wordBytes));
    }

    public String executeLDCH(int[] operands, boolean indexed, int effectiveAddress) {
        int byteValue = memory.readByte(effectiveAddress / 3, effectiveAddress % 3);
        registers.getRegister("A").setValue(byteValue & 0xFF);

        return String.format("LDCH: A[byte] ← %02X", byteValue);
    }

    public String executeLDL(int[] operands, boolean indexed, int effectiveAddress) {
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("L").setValue(Convert.bytesToInt(wordBytes));

        return String.format("LDL: L ← %06X", Convert.bytesToInt(wordBytes));
    }

    public String executeLDS(int[] operands, boolean indexed, int effectiveAddress) {
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("S").setValue(Convert.bytesToInt(wordBytes));

        return String.format("LDS: S ← %06X", Convert.bytesToInt(wordBytes));
    }

    public String executeLDT(int[] operands, boolean indexed, int effectiveAddress) {
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        registers.getRegister("T").setValue(Convert.bytesToInt(wordBytes));

        return String.format("LDT: T ← %06X", Convert.bytesToInt(wordBytes));
    }

    public String executeMUL(int[] operands, boolean indexed, int effectiveAddress) {
        Register A = registers.getRegister("A");
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() * operandValue;
        A.setValue(result);
        updateConditionCode(result);

        return String.format("MUL: Resultado = %06X", result);
    }

    public String executeMULR(int[] operands) {
        Register r1 = getRegisterByNumber(operands[0]);
        Register r2 = getRegisterByNumber(operands[1]);

        int result = r1.getIntValue() * r2.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);

        return String.format("MULR: R%d * R%d = %06X", operands[0], operands[1], result);
    }

    public String executeOR(int[] operands, boolean indexed, int effectiveAddress) {
        Register A = registers.getRegister("A");
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() | operandValue;
        A.setValue(result);
        updateConditionCode(result);

        return String.format("OR: Resultado = %06X", result);
    }

    public String executeRMO(int[] operands) {
        Register source = getRegisterByNumber(operands[0]);
        Register dest = getRegisterByNumber(operands[1]);

        dest.setValue(source.getIntValue());

        return String.format("RMO: R%d → R%d | Valor=%06X", operands[0], operands[1], source.getIntValue());
    }

    public String executeRSUB() {
        int returnAddress = registers.getRegister("L").getIntValue();
        registers.getRegister("PC").setValue(returnAddress);

        return String.format("RSUB: Retornando para %06X", returnAddress);
    }

    public String executeSHIFTL(int[] operands) {
        Register reg = getRegisterByNumber(operands[0]);
        int count = operands[1];
        int value = reg.getIntValue() << count;

        reg.setValue(value);
        updateConditionCode(value);

        return String.format("SHIFTL: R%d << %d = %06X", operands[0], count, value);
    }

    public String executeSHIFTR(int[] operands) {
        Register reg = getRegisterByNumber(operands[0]);
        int count = operands[1];
        int value = reg.getIntValue() >>> count; // Deslocamento lógico

        reg.setValue(value);
        updateConditionCode(value);
        return String.format("SHIFTR: R%d >> %d = %06X", operands[0], count, value);
    }

    public String executeSTA(int[] operands, boolean indexed, int effectiveAddress) {
        int value = registers.getRegister("A").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));

        return String.format("STA: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    public String executeSTB(int[] operands, boolean indexed, int effectiveAddress) {
        int value = registers.getRegister("B").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));

        return String.format("STB: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    public String executeSTCH(int[] operands, boolean indexed, int effectiveAddress) {
        int byteValue = registers.getRegister("A").getIntValue() & 0xFF;
        memory.writeByte(effectiveAddress / 3, effectiveAddress % 3, byteValue);

        return String.format("STCH: Mem[%06X] ← %02X", effectiveAddress, byteValue);
    }

    public String executeSTL(int[] operands, boolean indexed, int effectiveAddress) {
        int value = registers.getRegister("L").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));

        return String.format("STL: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    public String executeSTS(int[] operands, boolean indexed, int effectiveAddress) {
        int value = registers.getRegister("S").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));

        return String.format("STS: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    public String executeSTT(int[] operands, boolean indexed, int effectiveAddress) {
        int value = registers.getRegister("T").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));

        return String.format("STT: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    public String executeSTX(int[] operands, boolean indexed, int effectiveAddress) {
        int value = registers.getRegister("X").getIntValue();
        memory.writeWord(effectiveAddress / 3, Convert.intTo3Bytes(value));

        return String.format("STX: Mem[%06X] ← %06X", effectiveAddress, value);
    }

    public String executeSUB(int[] operands, boolean indexed, int effectiveAddress) {
        Register A = registers.getRegister("A");
        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int operandValue = Convert.bytesToInt(wordBytes);

        int result = A.getIntValue() - operandValue;
        A.setValue(result);
        updateConditionCode(result);

        return String.format("SUB: Resultado = %06X", result);
    }

    public String executeSUBR(int[] operands) {
        Register r1 = getRegisterByNumber(operands[0]);
        Register r2 = getRegisterByNumber(operands[1]);

        int result = r2.getIntValue() - r1.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);

        return String.format("SUBR: R%d - R%d = %06X", operands[1], operands[0], result);
    }

    public String executeTIX(int[] operands, boolean indexed, int effectiveAddress) {
        Register X = registers.getRegister("X");
        X.setValue(X.getIntValue() + 1);

        byte[] wordBytes = memory.readWord(effectiveAddress / 3);
        int memValue = Convert.bytesToInt(wordBytes);

        int comparison = X.getIntValue() - memValue;
        updateConditionCode(comparison);

        return String.format("TIX: X=%06X vs Mem[%06X]=%06X => %s",
                X.getIntValue(), effectiveAddress, memValue, getConditionCodeDescription());
    }

    public String executeTIXR(int[] operands) {
        Register X = registers.getRegister("X");
        X.setValue(X.getIntValue() + 1);

        Register r = getRegisterByNumber(operands[0]);
        int comparison = X.getIntValue() - r.getIntValue();

        updateConditionCode(comparison);

        return String.format("TIXR: X=%06X vs R%d=%06X => %s",
                X.getIntValue(), operands[0], r.getIntValue(), getConditionCodeDescription());
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
}
