package sicxesimulator.machine;

import sicxesimulator.machine.cpu.ControlUnit;
import sicxesimulator.machine.memory.Memory;

public class Machine {
    private final ControlUnit controlUnit;
    private Memory memory;

    public Machine() {
        this.memory = new Memory(24576); // Valor arbitrário, parametrizável.
        this.controlUnit = new ControlUnit(this.memory);
    }

    public int getMemorySize() {
        return memory.getMemorySize();
    }

    public void runCycle() {
        if (controlUnit.isHalted()) return;

        try {
            controlUnit.step();
        } catch (Exception e) {
            System.err.println("Erro na execução: " + e.getMessage());
            controlUnit.reset();
        }
    }

    public void reset() {
        memory.clearMemory();
        controlUnit.reset();
        System.out.println("Máquina reiniciada.");
    }

    public void changeMemorySize(int newSizeInBytes) {
        this.memory = new Memory(newSizeInBytes);
    }

    public Memory getMemory() { return this.memory; }

    public ControlUnit getControlUnit() { return controlUnit; }
}
