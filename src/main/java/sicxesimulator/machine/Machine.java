package sicxesimulator.machine;

import sicxesimulator.machine.cpu.ControlUnit;
import sicxesimulator.machine.cpu.Register;

@SuppressWarnings("unused")
public class Machine {
    private final ControlUnit controlUnit;
    private Memory memory;
    private int cycleSpeed;

    public Machine() {
        this.memory = new Memory(1024);
        this.cycleSpeed = 0;
        this.controlUnit = new ControlUnit(this.memory);
    }

    /**
     * Carrega um programa a partir de um objectCode. Escreve na memória e atualiza o Program Counter.
     * @param startAddress - Endereço de início definido no programa.
     * @param programBytes - O objectCode gerado após o montador processar um código assembly.
     */
    public void loadProgram(int startAddress, byte[] programBytes) {
        memory.writeBytes(startAddress, programBytes);
        controlUnit.setPC(startAddress);
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
        applyCycleDelay();
    }

    /**
     * Reinicia a máquina, limpando a memória e os registradores da unidade de controle.
     */
    public void reset() {
        memory.clearMemory();
        controlUnit.clearRegisters();
    }

    /**
     * Aplica um delay dentro do ciclo de instrução.
     * Os delays podem de ser de 1s, 500ns, 250ns, 100ns ou 0.
     * A duração do delay depende da variável cycleSpeed.
     */
    private void applyCycleDelay() {
        if (cycleSpeed > 0) {
            try {
                long delay = getDelayForSpeed(cycleSpeed);
                Thread.sleep(delay); // Adiciona o Delay
            } catch (InterruptedException e) {
                System.err.println("Execução interrompida: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restaura o estado de interrupção
            }
        }
    }

    /**
     * Mapeia, a partir de um inteiro, a duração em ms do delay que deve ser aplicado.
     * @param speed - Um inteiro podendo ser:
     *              0 -> sem delay (padrão)
     *              1 -> muito lento (1000ms)
     *              2 -> lento (500ms)
     *              3 -> médio (250ms)
     *              4 -> rápido (100ms)
     * @return - Retorna o valor em ms do delay a ser aplicado no ciclo de instrução.
     */
    private long getDelayForSpeed(int speed) {
        return switch (speed) {
            case 1 -> 1000;// 1000ms (muito lento)
            case 2 -> 500; // 500ms (lento)
            case 3 -> 250; // 250ms (médio)
            case 4 -> 100; // 100ms (rápido)
            default -> 0; // Sem delay
        };
    }

    /**
     * Altera a velocidade de ciclo da máquina.
     * @param speed - Deve ser um valor inteiro entre 0 e 4.
     */
    public void setCycleSpeed(int speed) {
        if (speed >= 0 && speed <= 4) {
            this.cycleSpeed = speed;
        } else {
            throw new IllegalArgumentException("Velocidade inválida. Use 0 (tempo real), 1 (muito lento), 2 (lento), 3 (médio), ou 4 (rápido).");
        }
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
    public ControlUnit getControlUnit() { return controlUnit; }

    public int getCycleSpeed() { return cycleSpeed; }

    public Memory getMemoryState() { return memory; }

    public Register[] getRegisterState() { return controlUnit.getCurrentRegisters(); }

}