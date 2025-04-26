package sicxesimulator.hardware;

import sicxesimulator.hardware.cpu.core.ControlUnit;
import sicxesimulator.common.utils.Constants;

/**
 * Representa a máquina SIC/XE completa (CPU + memória).
 * <p>
 * Oferece métodos para avançar ciclos, resetar estado e ajustar
 * tamanho de memória dinamicamente.
 *
 * @author Renan
 * @since 1.0.0
 */
public class Machine {

    private ControlUnit controlUnit;
    private Memory memory;

    /** Cria máquina com tamanho de memória padrão. */
    public Machine() {
        this.memory = new Memory(Constants.DEFAULT_MEMORY_SIZE_IN_BYTES);
        this.controlUnit = new ControlUnit(memory);
    }

    /**
     * Cria máquina com memória de tamanho customizado.
     *
     * @param sizeInBytes tamanho da memória em bytes
     */
    @SuppressWarnings("unused")
    public Machine(int sizeInBytes) {
        this.memory      = new Memory(sizeInBytes);
        this.controlUnit = new ControlUnit(memory);
    }

    /** Avança um ciclo de CPU, a menos que esteja halted. */
    public void runCycle() {
        if (!controlUnit.isHalted()) {
            try {
                controlUnit.step();
            } catch (Exception e) {
                System.err.printf("Erro na execucao em PC=%06X: %s%n",
                        controlUnit.getIntValuePC(), e.getMessage());
            }
        }
    }

    /** Restaura máquina ao estado inicial (memória e CPU). */
    public void reset() {
        memory.reset();
        controlUnit.reset();
    }

    /**
     * Ajusta o tamanho da memória, reiniciando-a.
     *
     * @param newSizeInBytes novo tamanho em bytes
     */
    public void changeMemorySize(int newSizeInBytes) {
        this.memory = new Memory(newSizeInBytes);
        this.controlUnit = new ControlUnit(memory);
    }

    /** @return instância de {@link Memory} atualmente em uso */
    public Memory getMemory() {
        return memory;
    }

    /** @return instância de {@link ControlUnit} da CPU */
    public ControlUnit getControlUnit() {
        return controlUnit;
    }
}
