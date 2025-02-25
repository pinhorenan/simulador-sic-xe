package sicxesimulator.model.components.operations;

import sicxesimulator.model.components.Machine;
import sicxesimulator.model.components.Register;
import sicxesimulator.model.components.Memory;

public class Operation {
    protected final Machine machine;
    protected final Memory memory;
    protected final Register A, X, L, PC, B, S, T, F, SW;

    public Operation(Machine machine) {
        this.machine = machine;
        this.memory = machine.getMemory();
        this.A = machine.getRegister("A");
        this.X = machine.getRegister("X");
        this.L = machine.getRegister("L");
        this.PC = machine.getRegister("PC");
        this.B = machine.getRegister("B");
        this.S = machine.getRegister("S");
        this.T = machine.getRegister("T");
        this.F = machine.getRegister("F");
        this.SW = machine.getRegister("SW");
    }

    // ================ HELPER METHODS ================
    private int parseAddress(String operand) {
        boolean indexed = operand.contains(",X");
        String cleanOperand = operand.replace(",X", "").replace("#", "").replace("@", "");
        int address = Integer.parseInt(cleanOperand, 16);

        if (indexed) {
            address += X.getIntValue();
        }
        return address;
    }

    private int readMemoryWord(int address) {
        return Integer.parseInt(
                memory.read(address) +
                        memory.read(address + 1) +
                        memory.read(address + 2), 16
        );
    }

    private void writeMemoryWord(int address, int value) {
        String hex = String.format("%06X", value);
        memory.write(address, hex.substring(0, 2));
        memory.write(address + 1, hex.substring(2, 4));
        memory.write(address + 2, hex.substring(4, 6));
    }

    protected void updateFlags(int result) {
        if (result == 0) {
            SW.setValue("000000");  // CC = 00
        } else if (result < 0) {
            SW.setValue("000001");  // CC = 01
        } else {
            SW.setValue("000002");  // CC = 10
        }
    }

    // ================ INSTRUCTION SET ================

    // ADD (Format 3)
    public void add(String[] args) {
        int address = parseAddress(args[0]);
        int memValue = readMemoryWord(address);
        int regValue = A.getIntValue();
        int result = (regValue + memValue) & 0xFFFFFF;
        A.setValue(result);
        updateFlags(result);
    }

    // ADDR (Format 2)
    public void addr(String[] args) {
        Register r1 = machine.getRegister(args[0]);
        Register r2 = machine.getRegister(args[1]);
        int result = (r2.getIntValue() + r1.getIntValue()) & 0xFFFFFF;
        r2.setValue(result);
        updateFlags(result);
    }

    // AND (Format 3)
    public void and(String[] args) {
        int address = parseAddress(args[0]);
        int memValue = readMemoryWord(address);
        int result = A.getIntValue() & memValue;
        A.setValue(result);
        updateFlags(result);
    }

    // CLEAR (Format 2)
    public void clear(String[] args) {
        Register r = machine.getRegister(args[0]);
        r.setValue(0);
    }

    // COMP (Format 3)
    public void comp(String[] args) {
        int address = parseAddress(args[0]);
        int memValue = readMemoryWord(address);
        int comparison = A.getIntValue() - memValue;
        updateFlags(comparison);
    }

    // DIV (Format 3)
    public void div(String[] args) {
        int address = parseAddress(args[0]);
        int divisor = readMemoryWord(address);
        if (divisor == 0) throw new ArithmeticException("Division by zero");
        int result = A.getIntValue() / divisor;
        A.setValue(result);
        updateFlags(result);
    }

    // J (Format 3/4)
    public void j(String[] args) {
        PC.setValue(parseAddress(args[0]));
    }

    // JEQ (Format 3/4)
    public void jeq(String[] args) {
        if (SW.getIntValue() == 0) {  // CC = 00
            PC.setValue(parseAddress(args[0]));
        }
    }

    // JLT (Format 3/4)
    public void jlt(String[] args) {
        if (SW.getIntValue() == 1) {  // CC = 01
            PC.setValue(parseAddress(args[0]));
        }
    }

    // JGT (Format 3/4)
    public void jgt(String[] args) {
        if (SW.getIntValue() == 2) {  // CC = 10
            PC.setValue(parseAddress(args[0]));
        }
    }

    // LDA (Format 3)
    public void lda(String[] args) {
        int address = parseAddress(args[0]);
        A.setValue(readMemoryWord(address));
    }

    // LDCH (Format 3)
    public void ldch(String[] args) {
        int address = parseAddress(args[0]);
        String byteValue = memory.read(address);
        A.setValue((A.getIntValue() & 0xFFFF00) | Integer.parseInt(byteValue, 16));
    }

    // LDL (Format 3)
    public void ldl(String[] args) {
        int address = parseAddress(args[0]);
        L.setValue(readMemoryWord(address));
    }

    // LDX (Format 3)
    public void ldx(String[] args) {
        int address = parseAddress(args[0]);
        X.setValue(readMemoryWord(address));
    }

    // RSUB (Format 3)
    public void rsub() {
        PC.setValue(L.getIntValue());
    }

    // STA (Format 3)
    public void sta(String[] args) {
        int address = parseAddress(args[0]);
        writeMemoryWord(address, A.getIntValue());
    }

    // STCH (Format 3)
    public void stch(String[] args) {
        int address = parseAddress(args[0]);
        String byteValue = String.format("%02X", A.getIntValue() & 0xFF);
        memory.write(address, byteValue);
    }

    // STX (Format 3)
    public void stx(String[] args) {
        int address = parseAddress(args[0]);
        writeMemoryWord(address, X.getIntValue());
    }

    // SUB (Format 3)
    public void sub(String[] args) {
        int address = parseAddress(args[0]);
        int memValue = readMemoryWord(address);
        int result = (A.getIntValue() - memValue) & 0xFFFFFF;
        A.setValue(result);
        updateFlags(result);
    }

    // TIX (Format 3)
    public void tix(String[] args) {
        int address = parseAddress(args[0]);
        X.increment();
        int comparison = X.getIntValue() - readMemoryWord(address);
        updateFlags(comparison);
    }

    // MUL (Format 3)
    public void mul(String[] args) {
        int address = parseAddress(args[0]);
        int memValue = readMemoryWord(address);
        int result = (A.getIntValue() * memValue) & 0xFFFFFF;
        A.setValue(result);
        updateFlags(result);
    }

    // JSUB (Format 3)
    public void jsub(String[] args) {
        L.setValue(PC.getIntValue() + 3); // Assume format 3
        PC.setValue(parseAddress(args[0]));
    }

    // ================ FLOATING POINT OPERATIONS (F REGISTER) ================

    // ADDF (Format 3)
    public void addf(String[] args) {
        int address = parseAddress(args[0]);
        long value1 = F.getLongValue();
        long value2 = readMemoryWord(address) | ((long) readMemoryWord(address + 3) << 24);
        F.setValue(value1 + value2);
    }

    // SUBF (Format 3)
    public void subf(String[] args) {
        int address = parseAddress(args[0]);
        long value1 = F.getLongValue();
        long value2 = readMemoryWord(address) | ((long) readMemoryWord(address + 3) << 24);
        F.setValue(value1 - value2);
    }
}