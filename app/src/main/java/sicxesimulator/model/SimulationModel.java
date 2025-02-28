package sicxesimulator.model;

import sicxesimulator.model.machine.Machine;
import sicxesimulator.model.assembler.Assembler;
import sicxesimulator.model.loader.Loader;

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
        // O Loader recebe a memória e a ControlUnit (obtidos via getters da Machine)
        loader.load(objectFilePath, machine.getMemoryState(), machine.getControlUnit());
    }

    /**
     * Monta e carrega um programa na máquina.
     * Gera um arquivo objeto temporário a partir do código fonte.
     *
     * @param sourceLines Lista contendo o código assembly fonte.
     * @throws IOException Se ocorrer erro durante a montagem ou carregamento.
     */
    public void assembleAndLoadProgram(List<String> sourceLines) throws IOException {
        // Gera um arquivo objeto temporário, por exemplo "temp.obj"
        String tempObjectFile = "temp.obj";
        assembler.assembleToFile(sourceLines, tempObjectFile);
        loadProgramFromObjectFile(tempObjectFile);
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
        return false; // Stub – ajuste conforme sua lógica de término
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
