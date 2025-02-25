package sicxesimulator.model.components;

public class Machine {
    private final Memory memory;
    private final Register A, X, L, PC, B, S, T, F, SW;

    public Machine(int totalMemorySize) {
        this.memory = new Memory(totalMemorySize);
        this.A = new Register("A");
        this.X = new Register("X");
        this.L = new Register("L");
        this.PC = new Register("PC");
        this.B = new Register("B");
        this.S = new Register("S");
        this.T = new Register("T");
        this.F = new Register("F");
        this.SW = new Register("SW");
        this.PC.setValue(0); // Inicializa PC com 0
    }

    public void reset() {
        // Reinicia todos os registradores
        A.setValue(0);
        X.setValue(0);
        L.setValue(0);
        B.setValue(0);
        S.setValue(0);
        T.setValue(0);
        F.setValue(0L); // Registrador de 48 bits
        SW.setValue(0);

        // Reinicia o PC para o endereço inicial
        PC.setValue(0);

        // Limpa toda a memória
        memory.clear();
    }

    // ================ MEMORY ACCESS ================
    public Memory getMemory() { return memory; }

    // ================ REGISTER ACCESS ================
    public Register getRegister(String name) {
        return switch (name.toUpperCase()) {
            case "A" -> A;
            case "X" -> X;
            case "L" -> L;
            case "PC" -> PC;
            case "B" -> B;
            case "S" -> S;
            case "T" -> T;
            case "F" -> F;
            case "SW" -> SW;
            default -> throw new IllegalArgumentException("Registrador inválido: " + name);
        };
    }

    // ================ PROGRAM COUNTER MANAGEMENT ================
    public void setPC(int address) {
        PC.setValue(address);
    }

    public Register getPC() {
        return PC;
    }

}