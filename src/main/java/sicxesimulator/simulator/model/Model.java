package sicxesimulator.simulator.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.linker.Linker;
import sicxesimulator.loader.Loader;
import sicxesimulator.macroprocessor.MacroProcessor;
import sicxesimulator.machine.Machine;
import sicxesimulator.utils.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private final Machine machine;
    private final Loader loader;
    private final Linker linker;
    private final Assembler assembler;
    private final MacroProcessor macroProcessor;

    // Listeners
    private final List<ModelListener> listeners = new ArrayList<>();

    // Arquivos de objeto
    private final List<ObjectFile> objectFileList = new ArrayList<>();
    private ObjectFile mostRecentObjectFile = null;

    // View state
    private final ViewConfig viewConfig = new ViewConfig();

    // Estado do modelo
    private int memorySize;
    private int simulationSpeed;

    // Estados reativos
    private final BooleanProperty codeLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty simulationPaused = new SimpleBooleanProperty(false);
    private final BooleanProperty simulationFinished = new SimpleBooleanProperty(false);


    public Model() {
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
            listener.onFilesUpdated();
        }
    }

    ///  Getters de componentes

    public Machine getMachine() {
        return machine;
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

    public ObjectFile getObjectFileByName(String selectedFileName) {
        return objectFileList.stream()
                .filter(objFile -> objFile.getFilename().equals(selectedFileName))
                .findFirst()
                .orElse(null);
    }

    ///  Getters/Setters de atributos do modelo

    public void setSimulationSpeed(int newSimulationSpeed) {
        if (newSimulationSpeed >= 0 && newSimulationSpeed <= 4) {
            this.simulationSpeed = newSimulationSpeed;
        } else {
            throw new IllegalArgumentException("Velocidade inválida. Use 0 (tempo real), 1 (muito lento), 2 (lento), 3 (médio), ou 4 (rápido).");
        }
    }

    public int getSimulationSpeed() {
        return simulationSpeed;
    }

    public void setMemorySize(int newMemorySize) {
        this.memorySize = newMemorySize;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public BooleanProperty codeLoadedProperty() {
        return codeLoaded;
    }

    public void setCodeLoaded(boolean loaded) {
        codeLoaded.set(loaded);
    }

    public BooleanProperty simulationFinishedProperty() {
        return simulationFinished;
    }

    public void setSimulationFinished(boolean finished) {
        simulationFinished.set(finished);
    }

    public BooleanProperty simulationPausedProperty() {
        return simulationPaused;
    }

    public void setSimulationPaused(boolean paused) {
        simulationPaused.set(paused);
    }

    /// Controle dos módulos (montador, processador de macros, ligador, carregador)

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

    public ObjectFile linkProgram(List<ObjectFile> objectFiles, int loadAddress, boolean fullRelocation) {
        ObjectFile linkedObject = linker.link(objectFiles, loadAddress, fullRelocation);
        updateObjectFileList(linkedObject);
        return linkedObject;
    }

    /// Controle de execução do programa

    public void runNextInstruction() {
        machine.runCycle();
    }

    public void reset() {
        machine.reset();
        assembler.reset();
        objectFileList.clear();
        setCodeLoaded(false);
        setSimulationFinished(false);
        setSimulationPaused(false);
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

    public void removeObjectFile(ObjectFile objectFile) {
        objectFileList.remove(objectFile);
    }
}

