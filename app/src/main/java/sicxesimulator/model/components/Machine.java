package sicxesimulator.model.components;

import sicxesimulator.model.components.cpu.ControlUnit;
import sicxesimulator.model.components.cpu.Register;

public class Machine {
    private final Memory memory;
    private final ControlUnit controlUnit;
    private boolean isRunning;

    // Construtor padrão com memória mínima de 1KB
    public Machine() {
        this.memory = new Memory(1024);
        this.controlUnit = new ControlUnit(this.memory);
        this.isRunning = false;
    }

    // Construtor com memória parametrizada
    public Machine(int memorySize) {
        this.memory = new Memory(memorySize);
        this.controlUnit = new ControlUnit(this.memory);
        this.isRunning = false;
    }

    // Carrega um programa na memória e define o PC
    public void loadProgram(int startAddress, byte[] programBytes) {
        memory.writeBytes(startAddress, programBytes);
        controlUnit.setPC(startAddress);
    }

    // Métodos de controle de estado da máquina.
    public void run() {
        isRunning = true;
        while (isRunning) {
            controlUnit.fetch();
            controlUnit.decode();
            controlUnit.execute();
        }
    }

    public void runCycle() {
        controlUnit.fetch();
        controlUnit.decode();
        controlUnit.execute();
    }

    public void pause() { isRunning = false; }

    public void resume() { isRunning = true; }

    public boolean isRunning() { return isRunning; }

    // Métodos para interface gráfica (ex: obter estado atual)
    public Memory getMemoryState() { return memory; }

    public Register[] getRegisterState() { return controlUnit.getCurrentRegisters(); }

}