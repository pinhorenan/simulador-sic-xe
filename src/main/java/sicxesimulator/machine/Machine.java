package sicxesimulator.machine;

import sicxesimulator.machine.cpu.ControlUnit;

/**
 * Representa a máquina SIC/XE, composta por uma unidade de controle e uma memória.
 */
public class Machine {
    private final ControlUnit controlUnit;
    private Memory memory;

    public Machine() {
        this.memory = new Memory(24576); // Valor arbitrário, parametrizável.
        this.controlUnit = new ControlUnit(this.memory);
    }

    /**
     * Executa um ciclo de máquina, avançando a execução em uma instrução.
     */
    public void runCycle() {
        if (controlUnit.isHalted()) return;

        try {
            controlUnit.step();
        } catch (Exception e) {
            System.err.println("Erro na execução: " + e.getMessage());
            controlUnit.reset();
        }
    }

    /**
     * Reinicia a máquina, limpando a memória e resetando a unidade de controle.
     */
    public void reset() {
        memory.clearMemory();
        controlUnit.reset();
        System.out.println("Máquina reiniciada.");
    }

    /**
     * Altera o tamanho da memória da máquina.
     * @param newSizeInBytes O novo tamanho da memória, em bytes.
     */
    public void changeMemorySize(int newSizeInBytes) {
        this.memory = new Memory(newSizeInBytes);
    }

    /**
     * Retorna a memória da máquina.
     * @return A memória.
     */
    public Memory getMemory() { return this.memory; }

    /**
     * Retorna o tamanho da memória da máquina (em bytes).
     * @return O tamanho da memória.
     */
    public int getMemorySize() {
        return memory.getSize();
    }

    /**
     * Retorna a unidade de controle da máquina.
     * @return  A unidade de controle.
     */
    @SuppressWarnings("ClassEscapesDefinedScope")
    public ControlUnit getControlUnit() { return controlUnit; }
}
