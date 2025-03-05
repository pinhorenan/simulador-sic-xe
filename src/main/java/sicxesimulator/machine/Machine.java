package sicxesimulator.machine;

import sicxesimulator.machine.cpu.ControlUnit;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.machine.memory.Memory;

public class Machine {
    private final ControlUnit controlUnit;
    private Memory memory;

    public Machine() {
        this.memory = new Memory(24576); // Valor arbitrário, parametrizável.
        this.controlUnit = new ControlUnit(this.memory);
    }

    public void runCycle() {
        if (controlUnit.isHalted()) return;
        System.out.println("Iniciando ciclo de execução...");
        controlUnit.fetch();
        controlUnit.decode();
        controlUnit.execute();
        System.out.println("Ciclo de execução concluído.");
    }

    public void reset() {
        memory.clearMemory();
        controlUnit.reset();
        System.out.println("Máquina reiniciada.");
    }

    public void changeMemorySize(int newMemorySizeInBytes) {
        this.memory = new Memory(newMemorySizeInBytes);
        controlUnit.setMemory(this.memory);
        System.out.println("Tamanho da memória alterado para " + newMemorySizeInBytes + " bytes.");
    }

    public Memory getMemory() { return this.memory; }

    public Register[] getRegisters() { return controlUnit.getCurrentRegisters(); }

    public ControlUnit getControlUnit() { return controlUnit; }
}
