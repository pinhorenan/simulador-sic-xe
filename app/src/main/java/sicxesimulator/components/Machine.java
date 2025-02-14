package sicxesimulator.components;

public class Machine {
    private final Memory memory;
    private final Register A;
    private final Register X;
    private final Register L;
    private final Register PC;
    private final Register B;
    private final Register S;
    private final Register T;
    private final Register F;
    private final Register SW;

    public Machine() {
        this.memory = new Memory();
        this.A = new Register("A");
        this.X = new Register("X");
        this.L = new Register("L");
        this.PC = new Register("PC");
        this.B = new Register("B");
        this.S = new Register("S");
        this.T = new Register("T");
        this.F = new Register("F");
        this.SW = new Register("SW");
    }

    public Memory getMemory() {
        return memory;
    }

    public void setPC(int value) {
        PC.setValue(Integer.toString(value));
    }

    public String getPC() {
        return PC.getValue();
    }

    public Register getRegister(String name) {
        switch (name.toUpperCase()) {
            case "A":
                return A;
            case "X":
                return X;
            case "L":
                return L;
            case "PC":
                return PC;
            case "B":
                return B;
            case "S":
                return S;
            case "T":
                return T;
            case "F":
                return F;
            case "SW":
                return SW;
            default:
                return null;
        }
    }

    public void reset() {
        // Zere os valores dos registradores
        this.A.setValue(0);
        this.X.setValue(0);
        this.L.setValue(0);
        this.PC.setValue(0);
        this.B.setValue(0);
        this.S.setValue(0);
        this.T.setValue(0);
        this.F.setValue(0);
        this.SW.setValue(0);
        // Zere a memória
        this.memory.clear();  // Supondo que você tenha um método clear() na classe Memory
    }
}