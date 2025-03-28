package sicxesimulator.hardware;

import sicxesimulator.hardware.cpu.ControlUnit;
import sicxesimulator.utils.Constants;

/**
 * Representa a máquina SIC/XE completa, composta por memória e unidade de controle.
 *
 * Responsável por executar ciclos, resetar o estado, alterar a memória
 * e expor os principais componentes da arquitetura.
 */
public class Machine {
    private final ControlUnit controlUnit;
    private Memory memory;

    public Machine() {
        this.memory = new Memory(Constants.DEFAULT_MEMORY_SIZE_IN_BYTES); // Valor arbitrário, parametrizável.
        this.controlUnit = new ControlUnit(this.memory);
    }

    /**
     * Executa um único ciclo de máquina, avançando a execução em uma instrução.
     *
     * Se o processador estiver em estado de parada, o ciclo é ignorado.
     * Caso ocorra exceção durante a execução da instrução, um erro é reportado no console.
     */
    public void runCycle() {
        if (controlUnit.isHalted()) return;

        try {
            controlUnit.step();
        } catch (Exception e) {
            System.err.println("Erro na execucao! Endereco da instrucao: " + e.getMessage() + "; PC: " + controlUnit.getIntValuePC());
        }
    }

    /**
     * Reinicia completamente o estado da máquina:
     * - Limpa a memória
     * - Reseta a unidade de controle
     */
    public void reset() {
        memory.reset();
        controlUnit.reset();
    }

    /**
     * Aloca nova memória para a máquina com o tamanho especificado.
     *
     * @param newSizeInBytes Novo tamanho da memória, em bytes.
     */
    public void changeMemorySize(int newSizeInBytes) {
        this.memory = new Memory(newSizeInBytes);
    }

    /**
     * Retorna a instância atual de memória da máquina.
     *
     * @return Objeto {@link Memory} usado pela máquina.
     */
    public Memory getMemory() { return this.memory; }

    /**
     * Retorna o tamanho atual da memória da máquina (em bytes).
     *
     * @return Tamanho da memória.
     */
    public int getMemorySize() {
        return memory.getSize();
    }

    /**
     * Retorna a unidade de controle responsável pela execução de instruções.
     *
     * @return Objeto {@link ControlUnit} associado à máquina.
     */
    public ControlUnit getControlUnit() { return controlUnit; }
}
