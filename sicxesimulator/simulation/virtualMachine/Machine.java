package sicxesimulator.simulation.virtualMachine;

/**
 * Essa classe deverá lidar com a operação do simulador, ela é a classe "Máquina" e ela que contém Memória, Conjunto de Registradores, e ela que instanciará e coordenará os componentes atuantes.
 */
public class Machine {
    private Memory memory;
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

    // Métodos de acesso aos componentes da máquina.

    public Memory getMemory() {
        return memory;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public Register getRegister(String name) {
        return switch (name) {
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
