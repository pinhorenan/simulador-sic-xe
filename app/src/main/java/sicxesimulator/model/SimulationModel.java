package sicxesimulator.model;

import sicxesimulator.model.components.Machine;
import sicxesimulator.model.systems.Assembler;
import sicxesimulator.model.systems.Loader;
import sicxesimulator.model.systems.Runner;

import java.io.IOException;
import java.util.List;

public class SimulationModel {
    private final Machine virtualMachine;
    private final Assembler assembler;
    private final Runner runner;
    private final Loader loader;

    public SimulationModel(Machine virtualMachine, Assembler assembler, Runner runner, Loader loader) {
        this.virtualMachine = virtualMachine;
        this.assembler = assembler;
        this.runner = runner;
        this.loader = loader;
    }

    public void loadProgramFromObjectFile(String objectFilePath) throws IOException {
        loader.loadProgram(objectFilePath, virtualMachine);
        runner.setStartAddress(virtualMachine.getPC().getIntValue());
    }

    public void assembleAndLoadProgram(List<String> sourceLines) throws IOException {
        // Monta e gera arquivo objeto temporário
        String tempObjectFile = "temp.obj";
        assembler.assembleToFile(sourceLines, tempObjectFile);

        // Carrega o programa montado
        loadProgramFromObjectFile(tempObjectFile);
    }

    public void runNextInstruction() {
        if (!runner.isFinished()) {
            runner.runNextInstruction();
        }
    }

    public boolean isFinished() {
        return runner.isFinished();
    }

    public void reset() {
        virtualMachine.reset();
        runner.setStartAddress(0);
    }

    public Machine getVirtualMachine() {
        return virtualMachine;
    }

    public Assembler getAssembler() {
        return assembler;
    }
}