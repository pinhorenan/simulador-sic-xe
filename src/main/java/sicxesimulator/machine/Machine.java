package sicxesimulator.machine;

import sicxesimulator.machine.cpu.ControlUnit;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.machine.memory.Memory;

@SuppressWarnings("unused")
public class Machine {
    private final ControlUnit controlUnit;
    private Memory memory;

    public Machine() {
        this.memory = new Memory(4096); // Valor arbitrário, parametrizável.
        this.controlUnit = new ControlUnit(this.memory);
    }

    /**
     * Ciclo de execução básico.
     * Primeiro ocorre o fetching da instrução.
     * Em segundo lugar a instrução é decodificada.
     * Então a unidade de controle de fato irá executar a instrução
     * Pode haver um delay, dependendo da cycleSpeed definida nesta máquina.
     */
    public void runCycle() {
        if (controlUnit.isHalted()) return;
        controlUnit.fetch();
        controlUnit.decode();
        controlUnit.execute();
    }

    /**
     * Reinicia a máquina, limpando a memória e os registradores da unidade de controle.
     */
    public void reset() {
        memory.clearMemory();
        controlUnit.reset();
    }

    /**
     * Altera o tamanho da memória da máquina, para isso uma nova memória maior deverá ser criada e substituirá a antiga.
     * @param memorySize - Tamanho de bytes da memória, o tamanho mínimo aceitável é de 1024.
     */
    public void changeMemorySize(int memorySize) {
        this.memory = new Memory(memorySize);
        controlUnit.setMemory(this.memory);
    }

    /// GETTERS
    public Memory getMemory() { return memory; }

    public Register[] getRegisters() { return controlUnit.getCurrentRegisters(); }

    public ControlUnit getControlUnit() { return controlUnit; }

}