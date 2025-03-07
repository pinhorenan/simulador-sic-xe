package sicxesimulator.simulator.model;

import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.machine.Machine;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.loader.Loader;
import sicxesimulator.macroprocessor.MacroProcessor;
import sicxesimulator.simulator.view.SimulationApp;
import sicxesimulator.utils.ViewConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SimulationModel {
    private final Machine machine;
    private final Loader loader;
    private final Assembler assembler;
    private final MacroProcessor macroProcessor;
    private ObjectFile lastObjectFile = null;
    private int startAddress;
    private int simulationSpeed;
    private boolean isPaused;

    private final ViewConfig viewConfig = new ViewConfig();

    public SimulationModel(Machine machine, Assembler assembler, Loader loader) {
        this.machine = machine;
        this.assembler = assembler;
        this.loader = loader;
        this.macroProcessor = new MacroProcessor();
        this.isPaused = false;
        this.simulationSpeed = 0;
    }

    /// GETTERS

    public Machine getMachine() { return machine; }

    public Assembler getAssembler() { return assembler; }

    public ViewConfig getViewConfig() {
        return viewConfig;
    }

    public List<String> processMacros(List<String> sourceLines) throws IOException {
        String tempInputFile = "temp.asm";
        String macroOutputFile = "MASMAPRG.ASM"; // Nome definido nas especificações: não mudar!!!!
        Files.write(Path.of(tempInputFile), sourceLines, StandardCharsets.UTF_8);
        macroProcessor.process(tempInputFile, macroOutputFile);
        return Files.readAllLines(Path.of(macroOutputFile), StandardCharsets.UTF_8);
    }

    public void loadObjectFile(ObjectFile selectedFile) {
        if (selectedFile != null) {
            loader.load(selectedFile);
            lastObjectFile = selectedFile; // Atualiza o último carregado
        }
    }

    public void updateLastObjectFile(ObjectFile objectFile) {
        this.lastObjectFile = objectFile;
    }

    public ObjectFile getLastObjectFile() {
        return lastObjectFile;
    }

    public void assembleCode(List<String> sourceLines) {
        ObjectFile machineCode = assembler.assemble(sourceLines);
        updateLastObjectFile(machineCode);
    }

    public void runNextInstruction() {
        machine.runCycle();
    }

    public void applyCycleDelay() {
        if (simulationSpeed > 0) {
            try {
                long delay = getDelayForSpeed(simulationSpeed);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.err.println("Execução interrompida: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }


    private int getDelayForSpeed(int speed) {
        return switch (speed) {
            case 1 -> 1000;
            case 2 -> 500;
            case 3 -> 250;
            case 4 -> 100;
            default -> 0;
        };
    }

    public int getCycleDelay() {
        return getDelayForSpeed(simulationSpeed);
    }

    public void setCycleSpeed(int newSimulationSpeed) {
        if (newSimulationSpeed >= 0 && newSimulationSpeed <= 4) {
            this.simulationSpeed = newSimulationSpeed;
        } else {
            throw new IllegalArgumentException("Velocidade inválida. Use 0 (tempo real), 1 (muito lento), 2 (lento), 3 (médio), ou 4 (rápido).");
        }
    }

    public boolean isFinished() {
        int pc = machine.getControlUnit().getIntValuePC();
        int programLength = lastObjectFile != null ? lastObjectFile.getProgramLength() : 0;
        return pc >= (startAddress + programLength);
    }

    public boolean hasAssembledCode() {
        return lastObjectFile != null;
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

    public void reset() {
        machine.reset();
        assembler.reset();
        startAddress = 0;
        lastObjectFile = null;
        isPaused = false;
    }

    public void loadSampleCode(String sampleCode, SimulationApp view, String title) {
        view.getInputField().setText(sampleCode);
        view.getStage().setTitle(title);
    }
}
