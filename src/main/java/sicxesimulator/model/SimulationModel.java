package sicxesimulator.model;

import javafx.scene.control.Alert;
import sicxesimulator.machine.Machine;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.loader.Loader;
import sicxesimulator.view.SimulationApp;

import java.io.IOException;
import java.util.List;

public class SimulationModel {
    private final Machine machine;
    private final Assembler assembler;
    private final Loader loader;
    private boolean isPaused;
    private int simulationSpeed;

    // Armazena o último código objeto montado e informações relacionadas
    private byte[] objectCode = null;
    private int startAddress;
    private int programLength;

    public SimulationModel(Machine machine, Assembler assembler, Loader loader) {
        this.machine = machine;
        this.assembler = assembler;
        this.loader = loader;
        this.isPaused = false;
        this.simulationSpeed = 0;
    }

    /**
     * Monta e carrega um programa na máquina.
     * Gera o código objeto (como array de bytes) a partir do código fonte e carrega na memória.
     *
     * @param sourceLines Lista contendo o código assembly fonte.
     * @throws IOException Se ocorrer erro durante a montagem ou carregamento.
     */
    public void assembleAndLoadProgram(List<String> sourceLines) throws IOException {
        // Gera o código objeto diretamente a partir do código fonte.
        objectCode = assembler.assemble(sourceLines);

        // Obtém o endereço de início (definido via diretiva START no código assembly)
        startAddress = assembler.getStartAddress();

        // Armazena o tamanho do programa para um eventual mecanismo de término
        programLength = objectCode.length;

        // Carrega o programa na memória a partir do endereço de início
        loader.load(machine.getMemory(), startAddress, objectCode);

        // Atualiza o PC para o início do programa
        machine.getControlUnit().setPC(startAddress);
    }

    /**
     * Executa um ciclo de instrução (fetch-decode-execute).
     */
    public void runNextInstruction() {
        machine.runCycle();
        applyCycleDelay();
    }

    /**
     * Aplica um delay dentro do ciclo de instrução.
     * Os delays podem de ser de 1s, 500ns, 250ns, 100ns ou 0.
     * A duração do delay depende da variável cycleSpeed.
     */
    private void applyCycleDelay() {
        if (simulationSpeed > 0) {
            try {
                long delay = getDelayForSpeed(simulationSpeed);
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
     * @param newSimulationSpeed - Deve ser um valor inteiro entre 0 e 4.
     */
    public void setCycleSpeed(int newSimulationSpeed) {
        if (newSimulationSpeed >= 0 && newSimulationSpeed <= 4) {
            this.simulationSpeed = newSimulationSpeed
            ;
        } else {
            throw new IllegalArgumentException("Velocidade inválida. Use 0 (tempo real), 1 (muito lento), 2 (lento), 3 (médio), ou 4 (rápido).");
        }
    }

    /**
     * Indica se a execução foi concluída.
     * Nesta versão, como não há um mecanismo de término automático, retorna sempre false.
     * Para uma implementação futura, você pode verificar se o PC ultrapassou o endereço final do programa.
     * Exemplo:
     * return machine.getControlUnit().getProgramCounter() ≧ (startAddress + programLength);
     *
     * @return true se a execução estiver concluída, false caso contrário.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isFinished() {
        return machine.getControlUnit().isHalted();
    }

    public boolean hasAssembledCode() {
        return objectCode != null;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void pause() {
        isPaused = true;
    }

    public void unpause() {
        isPaused = false;
    }

    /**
     * Reseta o estado da simulação.
     * Limpa o estado da máquina, do assembler e do loader, além de limpar o código objeto carregado.
     */
    public void reset() {
        machine.reset();
        assembler.reset();
        startAddress = 0;
        programLength = 0;
        objectCode = null;
        isPaused = false;
    }

    /**
     * Carrega um código assembly simples, utilizado para demonstração e para facilitar a testagem durante o desenvolvimento.
     * @param view - A janela principal do programa, passada por controller quando invocado.
     */
    public void loadSambleCode(SimulationApp view) {
        // Código de exemplo
        String exampleCode =
                """
                        COPY START 1000
                        FIRST  LDA   FIVE
                               ADD   FOUR
                               STA   RESULT
                               RSUB
                        FIVE   WORD  5
                        FOUR   WORD  4
                        RESULT RESW  1""";

        // Coloca o código exemplo no campo de entrada
            view.getInputField().setText(exampleCode);

        // Atualiza o título da janela (opcional)
            view.getStage().setTitle("Simulador SIC/XE - Exemplo Carregado");

        // Exibe uma mensagem (opcional)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Código de Exemplo");
            alert.setHeaderText("Código Assembly de Exemplo Carregado");
            alert.setContentText("O código de exemplo foi carregado no campo de entrada.");
            alert.showAndWait();
    }

    /// GETTERS

    public Machine getMachine() { return machine; }

    public Assembler getAssembler() { return assembler; }

    ///  SETTERS


}
