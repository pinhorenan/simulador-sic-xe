package sicxesimulator.model;

import sicxesimulator.machine.Machine;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.loader.Loader;

import java.io.IOException;
import java.util.List;

public class SimulationModel {
    private final Machine machine;
    private final Assembler assembler;
    private final Loader loader;

    public SimulationModel(Machine machine, Assembler assembler, Loader loader) {
        this.machine = machine;
        this.assembler = assembler;
        this.loader = loader;
    }

    /**
     * Carrega um arquivo objeto na memória usando o Loader.
     * O Loader ajusta a memória e a ControlUnit (PC) da máquina.
     *
     * @param objectFilePath Caminho para o arquivo objeto.
     * @throws IOException Se ocorrer erro de I/O.
     */
    public void loadProgramFromObjectFile(String objectFilePath) throws IOException {
        loader.load(objectFilePath, machine.getMemoryState(), machine.getControlUnit());
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
        byte[] objectCode = assembler.assemble(sourceLines);
        // Obtém o endereço de início (definido via diretiva START no código assembly)
        int startAddr = assembler.getStartAddress();
        // Carrega o programa na memória a partir do endereço de início
        machine.loadProgram(startAddr, objectCode);
    }

    /**
     * Executa um ciclo de instrução (fetch-decode-execute).
     */
    public void runNextInstruction() {
        machine.runCycle();
    }

    /**
     * Indica se a execução foi concluída.
     * Como não há um mecanismo de término automático, retorna sempre false.
     */
    public boolean isFinished() {
        return false; // Stub – ajuste conforme a lógica de término do seu simulador
    }

    public void reset() {
        machine.reset();
    }

    public Machine getMachine() {
        return machine;
    }

    public Assembler getAssembler() {
        return assembler;
    }
}
