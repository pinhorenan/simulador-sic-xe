package sicxesimulator.simulator.model;

import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.linker.Linker;
import sicxesimulator.loader.Loader;
import sicxesimulator.macroprocessor.MacroProcessor;
import sicxesimulator.machine.Machine;
import sicxesimulator.simulator.view.MainApp;
import sicxesimulator.utils.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MainModel {
    private final Machine machine;
    private final Loader loader;
    private final Assembler assembler;
    private final MacroProcessor macroProcessor;
    private final Linker linker;

    // Listeners
    private final List<ModelListener> listeners = new ArrayList<>();

    // Arquivos de objeto
    private List<ObjectFile> objectFileList = new ArrayList<>();
    private ObjectFile mostRecentObjectFile = null;

    // View state
    private final ViewConfig viewConfig = new ViewConfig();

    // Estado do modelo
    private int memorySize;
    private int simulationSpeed;
    private boolean isPaused = false;
    private boolean isFinished = false;
    private boolean hasLoadedCoded = false;

    public MainModel() {
        this.machine = new Machine();
        this.memorySize = machine.getMemorySize();
        this.loader = new Loader(machine);
        this.macroProcessor = new MacroProcessor();
        this.assembler = new Assembler();
        this.linker = new Linker();
    }

    /// Métodos de notificação

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (ModelListener listener : listeners) {
            listener.onFilesUpdated();  // Chama o método que atualizará a interface
        }
    }

    ///  Getters de componentes

    public Machine getMachine() {
        return machine;
    }

    public Assembler getAssembler() {
        return assembler;
    }

    public Linker getLinker() {
        return linker;
    }

    public ViewConfig getViewConfig() {
        return viewConfig;
    }

    ///  Getters de arquivos "em cache"

    public ObjectFile getMostRecentObjectFile() {
        return mostRecentObjectFile;
    }

    public List<ObjectFile> getObjectFilesList() {
        return objectFileList;
    }

    ///  Getters/Setters de atributos do modelo

    public void setSimulationSpeed(int newSimulationSpeed) {
        if (newSimulationSpeed >= 0 && newSimulationSpeed <= 4) {
            this.simulationSpeed = newSimulationSpeed;
        } else {
            throw new IllegalArgumentException("Velocidade inválida. Use 0 (tempo real), 1 (muito lento), 2 (lento), 3 (médio), ou 4 (rápido).");
        }
    }

    public void setMemorySize(int newMemorySize) {
        this.memorySize = newMemorySize;
    }

    public int getMemorySize() {
        return memorySize;
    }

    /// Métodos de controle de execução

    public boolean isFinished() {
        if (machine.getControlUnit().isHalted()) {
            isFinished = true;
        } else isFinished = false;
        return isFinished;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean hasLoadedCode() {
        return hasLoadedCoded;
    }

    /// Carregamento de arquivos

    public void assembleCode(List<String> macroProcessedWords) {
        ObjectFile machineCode = assembler.assemble(macroProcessedWords);
        updateObjectFileList(machineCode);
    }

    public List<String> expandMacros(List<String> sourceLines) throws IOException {
        String tempInputFile = "temp.asm";
        String macroOutputFile = "MASMAPRG.ASM"; // Nome definido nas especificações
        Files.write(Path.of(tempInputFile), sourceLines, StandardCharsets.UTF_8);
        macroProcessor.process(tempInputFile, macroOutputFile);
        return Files.readAllLines(Path.of(macroOutputFile), StandardCharsets.UTF_8);
    }

    public void loadObjectFile(ObjectFile selectedFile) {
        if (selectedFile != null) {
            loader.load(selectedFile);
            notifyListeners();  // Notifica os listeners quando um novo arquivo é carregado
        }
    }

    public void updateObjectFileList(ObjectFile objectFile) {
        objectFileList.add(objectFile);
        mostRecentObjectFile = objectFile;
        notifyListeners();
    }

   /// Controle de execução do programa

    public void runNextInstruction() {
        machine.runCycle();
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
        objectFileList.clear();
        isPaused = false;
        isFinished = false;
        hasLoadedCoded = false;
    }

    ///  Métodos auxiliares

    public void applyCycleDelay() {
        if (simulationSpeed > 0) {
            try {
                long delay = Mapper.mapSimulationSpeedToCycleDelay(simulationSpeed);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.err.println("Execução interrompida: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public int getCycleDelay() {
        return Mapper.mapSimulationSpeedToCycleDelay(simulationSpeed);
    }

    public void loadSampleCode(String sampleCode, MainApp view, String title) {


        view.getInputField().setText(sampleCode);
        view.getStage().setTitle(title);
    }

    public ObjectFile getObjectFileByName(String selectedFileName) {
        return objectFileList.stream()
                .filter(objFile -> objFile.getFilename().equals(selectedFileName))
                .findFirst()
                .orElse(null);
    }
}
