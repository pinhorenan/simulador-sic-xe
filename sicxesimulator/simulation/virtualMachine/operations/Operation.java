package sicxesimulator.simulation.virtualMachine.operations;

import sicxesimulator.simulation.virtualMachine.Machine;
import sicxesimulator.simulation.virtualMachine.Register;
import sicxesimulator.simulation.virtualMachine.Memory;

public class Operation {
    protected Machine machine;
    protected Memory memory;
    protected Register A, X, L, PC, B, S, T, F, SW;

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

    /**
     * Atualiza as flags do registrador SW com base no resultado.
     * Para este exemplo:
     * - "00000001" indica igualdade.
     * - "00000010" indica que o valor é menor.
     * - "00000011" indica que o valor é maior.
     */
    protected void updateFlags(int result) {
        if (result == 0) {
            SW.setValue("00000001");
        } else if (result < 0) {
            SW.setValue("00000010");
        } else {
            SW.setValue("00000011");
        }
    }

    // ---------------------------
    // Instruções de carregamento, armazenamento e operações aritméticas
    // ---------------------------

    // ADD: A ← A + (conteúdo da memória)
    public void add(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: ADD requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int memValue = Integer.parseInt(memory.read(address), 16);
        int regA = Integer.parseInt(A.getValue(), 16);
        int result = (regA + memValue) & 0xFFFFFF;
        updateFlags(result);
        A.setValue(String.format("%06X", result));
        System.out.println("ADD: A = " + A.getValue());
    }

    // ADDR: r2 ← r2 + r1
    public void addr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: ADDR requer dois argumentos.");
            return;
        }
        String r1Name = args[0];
        String r2Name = args[1];
        Register r1 = machine.getRegister(r1Name);
        Register r2;
        r2 = machine.getRegister(r2Name);
        int value1 = Integer.parseInt(r1.getValue(), 16);
        int value2 = Integer.parseInt(r2.getValue(), 16);
        int result = (value2 + value1) & 0xFFFFFF;
        updateFlags(result);
        r2.setValue(String.format("%06X", result));
        System.out.println("ADDR: " + r2Name + " = " + r2.getValue());
    }

    // AND: A ← A & (conteúdo da memória)
    public void and(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: AND requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int memValue = Integer.parseInt(memory.read(address), 16);
        int regA = Integer.parseInt(A.getValue(), 16);
        int result = regA & memValue;
        updateFlags(result);
        A.setValue(String.format("%06X", result));
        System.out.println("AND: A = " + A.getValue());
    }

    // CLEAR: r ← 0 (zera o registrador especificado)
    public void clear(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: CLEAR requer um argumento.");
            return;
        }
        String regName = args[0];
        Register reg = machine.getRegister(regName);
        String zeroValue = regName.equals("F") ? "000000000000" : "000000";
        reg.setValue(zeroValue);
        System.out.println("CLEAR: " + regName + " set to " + zeroValue);
    }

    // LDA: A ← (conteúdo da memória)
    public void LDA(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDA requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int value = Integer.parseInt(memory.read(address), 16);
        A.setValue(String.format("%06X", value));
        System.out.println("LDA: A = " + A.getValue());
    }

    // STA: (conteúdo da memória) ← A
    public void sta(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STA requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        String value = A.getValue();
        memory.write(address, value);
        System.out.println("STA: Mem[" + args[0] + "] = " + value);
    }

    // SUB: A ← A - (conteúdo da memória)
    public void sub(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: SUB requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int memValue = Integer.parseInt(memory.read(address), 16);
        int regA = Integer.parseInt(A.getValue(), 16);
        int result = (regA - memValue) & 0xFFFFFF;
        updateFlags(result);
        A.setValue(String.format("%06X", result));
        System.out.println("SUB: A = " + A.getValue());
    }

    // ---------------------------
    // Instruções de salto e sub-rotina
    // ---------------------------

    // J: PC ← (endereço)  (salto incondicional)
    public void j(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: J requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        PC.setValue(String.format("%06X", address));
        System.out.println("J: Jump para " + address);
    }

    // LDX: X ← (conteúdo da memória)
    public void ldx(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDX requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int value = Integer.parseInt(memory.read(address), 16);
        X.setValue(String.format("%06X", value));
        System.out.println("LDX: X = " + X.getValue());
    }

    // COMP: Compara "A" com o conteúdo da memória e ajusta SW
    public void comp(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: COMP requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int memValue = Integer.parseInt(memory.read(address), 16);
        int regA = Integer.parseInt(A.getValue(), 16);
        int result = regA - memValue;
        if (result == 0) {
            SW.setValue("00000001");
            System.out.println("COMP: CC ajustado para igual (SW = 00000001).");
        } else if (result < 0) {
            SW.setValue("00000010");
            System.out.println("COMP: CC ajustado para menor (SW = 00000010).");
        } else {
            SW.setValue("00000011");
            System.out.println("COMP: CC ajustado para maior (SW = 00000011).");
        }
    }

    // COMPR: Compara o conteúdo de dois registradores e ajusta SW
    public void compr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: COMPR requer dois argumentos.");
            return;
        }
        String r1Name = args[0];
        String r2Name = args[1];
        int value1 = Integer.parseInt(machine.getRegister(r1Name).getValue(), 16);
        int value2 = Integer.parseInt(machine.getRegister(r2Name).getValue(), 16);
        int result = value1 - value2;
        if (result == 0) {
            SW.setValue("00000001");
            System.out.println("COMPR: CC ajustado para igual (SW = 00000001).");
        } else if (result < 0) {
            SW.setValue("00000010");
            System.out.println("COMPR: CC ajustado para menor (SW = 00000010).");
        } else {
            SW.setValue("00000011");
            System.out.println("COMPR: CC ajustado para maior (SW = 00000011).");
        }
    }

    // DIV: A ← A / (conteúdo da memória)
    public void div(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: DIV requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int memValue = Integer.parseInt(memory.read(address), 16);
        int regA = Integer.parseInt(A.getValue(), 16);
        if (memValue == 0) {
            System.out.println("Erro: Divisão por zero.");
            return;
        }
        int result = regA / memValue;
        updateFlags(result);
        A.setValue(String.format("%06X", result));
        System.out.println("DIV: A = " + A.getValue());
    }

    // DIVR: r2 ← r2 / r1
    public void divr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: DIVR requer dois argumentos.");
            return;
        }
        String r1Name = args[0];
        String r2Name = args[1];
        Register r1 = machine.getRegister(r1Name);
        Register r2 = machine.getRegister(r2Name);
        int value1 = Integer.parseInt(r1.getValue(), 16);
        int value2 = Integer.parseInt(r2.getValue(), 16);
        if (value1 == 0) {
            System.out.println("Erro: Divisão por zero.");
            return;
        }
        int result = value2 / value1;
        updateFlags(result);
        r2.setValue(String.format("%06X", result));
        System.out.println("DIVR: " + r2Name + " = " + r2.getValue());
    }

    // JEQ: Se SW indica igualdade, PC ← (endereço)
    public void jeq(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: JEQ requer um argumento.");
            return;
        }
        if (SW.getValue().equals("00000001")) {
            int address = Integer.parseInt(args[0], 16);
            PC.setValue(String.format("%06X", address));
            System.out.println("JEQ: Salto para " + address);
        } else {
            System.out.println("JEQ: Condição não satisfeita (SW = " + SW.getValue() + ").");
        }
    }

    // JGT: Se SW indica maior, PC ← (endereço)
    public void jgt(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: JGT requer um argumento.");
            return;
        }
        if (SW.getValue().equals("00000011")) {
            int address = Integer.parseInt(args[0], 16);
            PC.setValue(String.format("%06X", address));
            System.out.println("JGT: Salto para " + address);
        } else {
            System.out.println("JGT: Condição não satisfeita (SW = " + SW.getValue() + ").");
        }
    }

    // JLT: Se SW indica menor, PC ← (endereço)
    public void jlt(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: JLT requer um argumento.");
            return;
        }
        if (SW.getValue().equals("00000010")) {
            int address = Integer.parseInt(args[0], 16);
            PC.setValue(String.format("%06X", address));
            System.out.println("JLT: Salto para " + address);
        } else {
            System.out.println("JLT: Condição não satisfeita (SW = " + SW.getValue() + ").");
        }
    }

    // JSUB: L ← PC + 1; PC ← (endereço)
    public void jsub(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: JSUB requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int pcVal = Integer.parseInt(PC.getValue(), 16);
        L.setValue(String.format("%06X", pcVal + 1));
        PC.setValue(String.format("%06X", address));
        System.out.println("JSUB: Salto para " + address + " com retorno armazenado em L = " + L.getValue());
    }

    // LDB: B ← (conteúdo da memória)
    public void ldb(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDB requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int value = Integer.parseInt(memory.read(address), 16);
        B.setValue(String.format("%06X", value));
        System.out.println("LDB: B = " + B.getValue());
    }

    // LDCH: Carrega 1 byte da memória no registrador A (mantém os 16 bits mais significativos)
    public void ldch(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDCH requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int memValue = Integer.parseInt(memory.read(address), 16);
        int memByte = memValue & 0xFF;
        int regA = Integer.parseInt(A.getValue(), 16);
        int newValue = (regA & 0xFFFF00) | memByte;
        A.setValue(String.format("%06X", newValue));
        System.out.println("LDCH: A = " + A.getValue());
    }

    // LDL: L ← (conteúdo da memória)
    public void ldl(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDL requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int value = Integer.parseInt(memory.read(address), 16);
        L.setValue(String.format("%06X", value));
        System.out.println("LDL: L = " + L.getValue());
    }

    // LDS: S ← (conteúdo da memória)
    public void lds(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDS requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int value = Integer.parseInt(memory.read(address), 16);
        S.setValue(String.format("%06X", value));
        System.out.println("LDS: S = " + S.getValue());
    }

    // LDT: T ← (conteúdo da memória)
    public void ldt(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDT requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int value = Integer.parseInt(memory.read(address), 16);
        T.setValue(String.format("%06X", value));
        System.out.println("LDT: T = " + T.getValue());
    }

    // MUL: A ← A * (conteúdo da memória)
    public void mul(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: MUL requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int memValue = Integer.parseInt(memory.read(address), 16);
        int regA = Integer.parseInt(A.getValue(), 16);
        int result = (regA * memValue) & 0xFFFFFF;
        updateFlags(result);
        A.setValue(String.format("%06X", result));
        System.out.println("MUL: A = " + A.getValue());
    }

    // MULR: r2 ← r2 * r1
    public void mulr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: MULR requer dois argumentos.");
            return;
        }
        String r1Name = args[0];
        String r2Name = args[1];
        Register r1 = machine.getRegister(r1Name);
        Register r2 = machine.getRegister(r2Name);
        int value1 = Integer.parseInt(r1.getValue(), 16);
        int value2 = Integer.parseInt(r2.getValue(), 16);
        int result = (value2 * value1) & 0xFFFFFF;
        updateFlags(result);
        r2.setValue(String.format("%06X", result));
        System.out.println("MULR: " + r2Name + " = " + r2.getValue());
    }

    // OR: A ← A | (conteúdo da memória)
    public void or(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: OR requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int memValue = Integer.parseInt(memory.read(address), 16);
        int regA = Integer.parseInt(A.getValue(), 16);
        int result = regA | memValue;
        updateFlags(result);
        A.setValue(String.format("%06X", result));
        System.out.println("OR: A = " + A.getValue());
    }

    // RMO: r2 ← r1 (copia o conteúdo de um registrador para outro)
    public void rmo(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: RMO requer dois argumentos.");
            return;
        }
        String sourceName = args[0];
        String destName = args[1];
        Register source = machine.getRegister(sourceName);
        Register dest = machine.getRegister(destName);
        dest.setValue(source.getValue());
        System.out.println("RMO: " + destName + " = " + dest.getValue() + " (copiado de " + sourceName + ")");
    }

    // RSUB: PC ← L (retorno de sub-rotina)
    public void rsub() {
        int returnAddress = Integer.parseInt(L.getValue(), 16);
        PC.setValue(String.format("%06X", returnAddress));
        System.out.println("RSUB: Retorno para " + returnAddress);
    }

    // SHIFTL: r ← r << n (deslocamento lógico à esquerda)
    public void shiftl(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: SHIFTL requer dois argumentos: registrador e quantidade de bits.");
            return;
        }
        String regName = args[0];
        int shiftCount = Integer.parseInt(args[1]);
        Register reg = machine.getRegister(regName);
        if (regName.equals("F")) {
            long value = Long.parseLong(reg.getValue(), 16);
            long result = (value << shiftCount) & 0xFFFFFFFFFFFFL;
            reg.setValue(String.format("%012X", result));
            updateFlags((int) (result & 0xFFFFFF)); // atualiza flags com os 24 bits menos significativos
            System.out.println("SHIFTL: " + regName + " = " + reg.getValue());
        } else {
            int value = Integer.parseInt(reg.getValue(), 16);
            int result = (value << shiftCount) & 0xFFFFFF;
            updateFlags(result);
            reg.setValue(String.format("%06X", result));
            System.out.println("SHIFTL: " + regName + " = " + reg.getValue());
        }
    }

    // SHIFTR: r ← r >> n (deslocamento lógico à direita)
    public void shiftr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: SHIFTR requer dois argumentos: registrador e quantidade de bits.");
            return;
        }
        String regName = args[0];
        int shiftCount = Integer.parseInt(args[1]);
        Register reg = machine.getRegister(regName);
        if (regName.equals("F")) {
            long value = Long.parseLong(reg.getValue(), 16);
            long result = (value >> shiftCount) & 0xFFFFFFFFFFFFL;
            reg.setValue(String.format("%012X", result));
            updateFlags((int) (result & 0xFFFFFF));
            System.out.println("SHIFTR: " + regName + " = " + reg.getValue());
        } else {
            int value = Integer.parseInt(reg.getValue(), 16);
            int result = (value >> shiftCount) & 0xFFFFFF;
            updateFlags(result);
            reg.setValue(String.format("%06X", result));
            System.out.println("SHIFTR: " + regName + " = " + reg.getValue());
        }
    }

    // STB: (conteúdo da memória) ← B
    public void stb(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STB requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        String value = B.getValue();
        memory.write(address, value);
        System.out.println("STB: Mem[" + args[0] + "] = " + value);
    }

    // STCH: Armazena o byte menos significativo de A na memória
    public void stch(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STCH requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        String regA = A.getValue();
        String byteValue = regA.substring(regA.length() - 2);
        memory.setByte(address, byteValue);
        System.out.println("STCH: Mem[" + args[0] + "] = " + byteValue);
    }

    // STL: (conteúdo da memória) ← L
    public void stl(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STL requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        String value = L.getValue();
        memory.write(address, value);
        System.out.println("STL: Mem[" + args[0] + "] = " + value);
    }

    // STS: (conteúdo da memória) ← S
    public void sts(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STS requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        String value = S.getValue();
        memory.write(address, value);
        System.out.println("STS: Mem[" + args[0] + "] = " + value);
    }

    // STT: (conteúdo da memória) ← T
    public void stt(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STT requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        String value = T.getValue();
        memory.write(address, value);
        System.out.println("STT: Mem[" + args[0] + "] = " + value);
    }

    // STX: (conteúdo da memória) ← X
    public void stx(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STX requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        String value = X.getValue();
        memory.write(address, value);
        System.out.println("STX: Mem[" + args[0] + "] = " + value);
    }

    // SUBR: r2 ← r2 - r1
    public void subr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: SUBR requer dois argumentos.");
            return;
        }
        String r1Name;
        r1Name = args[0];
        String r2Name = args[1];
        Register r1 = machine.getRegister(r1Name);
        Register r2 = machine.getRegister(r2Name);
        int value1 = Integer.parseInt(r1.getValue(), 16);
        int value2 = Integer.parseInt(r2.getValue(), 16);
        int result = (value2 - value1) & 0xFFFFFF;
        updateFlags(result);
        r2.setValue(String.format("%06X", result));
        System.out.println("SUBR: " + r2Name + " = " + r2.getValue());
    }

    // TIX: X ← X + 1; compara X com o conteúdo da memória e ajusta SW
    public void tix(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: TIX requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0], 16);
        int regX = Integer.parseInt(X.getValue(), 16);
        regX = (regX + 1) & 0xFFFFFF;
        X.setValue(String.format("%06X", regX));
        int memValue = Integer.parseInt(memory.read(address), 16);
        int result = regX - memValue;
        if (result == 0) {
            SW.setValue("00000001");
        } else if (result < 0) {
            SW.setValue("00000010");
        } else {
            SW.setValue("00000011");
        }
        System.out.println("TIX: X = " + X.getValue());
    }

    // TIXR: X ← X + 1; compara X com o registrador r1 e ajusta SW
    public void tixr(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: TIXR requer um argumento.");
            return;
        }
        String r1Name = args[0];
        int regX = Integer.parseInt(X.getValue(), 16);
        regX = (regX + 1) & 0xFFFFFF;
        X.setValue(String.format("%06X", regX));
        int r1Value = Integer.parseInt(machine.getRegister(r1Name).getValue(), 16);
        int result = regX - r1Value;
        if (result == 0) {
            SW.setValue("00000001");
        } else if (result < 0) {
            SW.setValue("00000010");
        } else {
            SW.setValue("00000011");
        }
        System.out.println("TIXR: X = " + X.getValue());
    }
}
