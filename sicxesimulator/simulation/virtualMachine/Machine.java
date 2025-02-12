package sicxesimulator.simulation.virtualMachine;

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
            default -> null;
        };
    }
}