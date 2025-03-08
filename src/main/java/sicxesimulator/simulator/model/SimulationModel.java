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
import java.util.ArrayList;
import java.util.List;

public class SimulationModel {
    private final Machine machine;
    private final Loader loader;
    private final Assembler assembler;
    private final MacroProcessor macroProcessor;
    private ObjectFile lastObjectFile = null;
    private List<ModelListener> listeners = new ArrayList<>();
    private List<ObjectFile> assembledObjectFiles = new ArrayList<>();
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

    // Adiciona um listener para monitorar mudanças no modelo
    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    // Notifica os listeners quando os arquivos forem atualizados
    private void notifyListeners() {
        for (ModelListener listener : listeners) {
            listener.onFilesUpdated();  // Chama o método que atualizará a interface
        }
    }

    // Método para atualizar os arquivos de objetos
    public void updateObjectFiles(List<ObjectFile> newFiles) {
        this.assembledObjectFiles = newFiles;  // Atualiza a lista de arquivos
        notifyListeners();  // Notifica os listeners sobre a mudança nos arquivos
    }

    public Machine getMachine() {
        return machine;
    }

    public Assembler getAssembler() {
        return assembler;
    }

    public ViewConfig getViewConfig() {
        return viewConfig;
    }

    public List<ObjectFile> getAssembledObjectFiles() {
        return assembledObjectFiles;
    }

    public List<String> processMacros(List<String> sourceLines) throws IOException {
        String tempInputFile = "temp.asm";
        String macroOutputFile = "MASMAPRG.ASM"; // Nome definido nas especificações
        Files.write(Path.of(tempInputFile), sourceLines, StandardCharsets.UTF_8);
        macroProcessor.process(tempInputFile, macroOutputFile);
        return Files.readAllLines(Path.of(macroOutputFile), StandardCharsets.UTF_8);
    }

    public void loadObjectFile(ObjectFile selectedFile) {
        if (selectedFile != null) {
            loader.load(selectedFile);
            lastObjectFile = selectedFile; // Atualiza o último carregado
            notifyListeners();  // Notifica os listeners quando um novo arquivo é carregado
        }
    }

    public void updateLastObjectFile(ObjectFile objectFile) {
        this.lastObjectFile = objectFile;
        notifyListeners();  // Notifica os listeners sobre a atualização do arquivo
    }

    public ObjectFile getLastObjectFile() {
        return lastObjectFile;
    }

    public void assembleCode(List<String> macroProcessedWords) {
        ObjectFile machineCode = assembler.assemble(macroProcessedWords);
        assembledObjectFiles.add(machineCode);
        updateLastObjectFile(machineCode);  // Atualiza o último arquivo montado
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
        return machine.getControlUnit().isHalted();
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

    public ObjectFile getObjectFileByName(String selectedFileName) {
        return assembledObjectFiles.stream()
                .filter(objFile -> objFile.getFilename().equals(selectedFileName))
                .findFirst()
                .orElse(null);
    }
}
