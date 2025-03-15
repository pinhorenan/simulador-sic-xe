package sicxesimulator.application.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import sicxesimulator.application.model.records.MemoryEntry;
import sicxesimulator.application.model.records.RegisterEntry;
import sicxesimulator.application.model.records.SymbolEntry;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.linker.Linker;
import sicxesimulator.loader.Loader;
import sicxesimulator.macroprocessor.MacroProcessor;
import sicxesimulator.machine.Machine;
import sicxesimulator.utils.*;

import java.io.*;
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

    // Estados reativos
    private final BooleanProperty codeLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty simulationPaused = new SimpleBooleanProperty(false);
    private final BooleanProperty simulationFinished = new SimpleBooleanProperty(false);
    // Listeners

    private final List<ModelListener> listeners = new ArrayList<>();

    // Último arquivo carregado
    private ObjectFile lastLoadedCode;

    // View state
    private final ViewConfig viewConfig = new ViewConfig();

    // Estado do modelo
    private int memorySize;
    private int simulationSpeed;

    public Model() {
        this.machine = new Machine();
        this.memorySize = machine.getMemorySize();
        this.loader = new Loader();
        this.macroProcessor = new MacroProcessor();
        this.assembler = new Assembler();
        this.linker = new Linker();

        // Verifica a pasta apontada pela constante "SAVE_DIR" e carrega os arquivos de objeto
        loadObjectFilesFromSaveDir();
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

    public ObjectFile getLastLoadedCode() {
        return lastLoadedCode;
    }

    public List<MemoryEntry> getMemoryEntries() {
        List<MemoryEntry> entries = new ArrayList<>();
        var memory = machine.getMemory();
        for (int wordIndex = 0; wordIndex < memory.getAddressRange(); wordIndex++) {
            byte[] word = memory.readWord(wordIndex);
            int byteAddress = wordIndex * 3;
            String formattedAddress = ValueFormatter.formatAddress(byteAddress, viewConfig.getAddressFormat());
            entries.add(new MemoryEntry(formattedAddress, Convert.bytesToHex(word)));
        }
        return entries;
    }

    public List<RegisterEntry> getRegisterEntries() {
        List<RegisterEntry> entries = new ArrayList<>();
        var registers = machine.getControlUnit().getRegisterSet().getAllRegisters();
        for (Register register : registers) {
            String value = ValueFormatter.formatRegisterValue(register, viewConfig.getAddressFormat());
            entries.add(new RegisterEntry(register.getName(), value));
        }
        return entries;
    }

    public List<SymbolEntry> getSymbolEntries() {
        List<SymbolEntry> entries = new ArrayList<>();
        ObjectFile objectFile = getLastLoadedCode();
        if (objectFile != null) {
            // Usa getAllSymbols(), que retorna Map<String, SymbolInfo>
            var symbolsMap = objectFile.getSymbolTable().getAllSymbols();
            symbolsMap.forEach((name, info) -> {
                // Agora info.address já é em bytes
                int byteAddress = info.address;
                String formattedAddress = ValueFormatter.formatAddress(byteAddress, viewConfig.getAddressFormat());
                entries.add(new SymbolEntry(name, formattedAddress));
            });
        }
        return entries;
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

    public List<String> processCodeMacros(List<String> rawSourceLines) throws IOException {
        String tempInputFile = "temp.asm"; // TODO: Revisar;
        String macroOutputFile = "MASMAPRG.ASM"; // Nome definido nas especificações
        Files.write(Path.of(tempInputFile), rawSourceLines, StandardCharsets.UTF_8);

        macroProcessor.process(tempInputFile, macroOutputFile);

        return Files.readAllLines(Path.of(macroOutputFile), StandardCharsets.UTF_8);
    }

    public ObjectFile assembleCode(List<String> rawSourceLines, List<String> preProcessedSourceCode) throws IOException {
        ObjectFile machineCode = assembler.assemble(rawSourceLines, preProcessedSourceCode);
        addAndSaveObjectFileToList(machineCode);

        return machineCode;
    }

    public ObjectFile linkObjectFiles(List<ObjectFile> files, int loadAddress, boolean fullRelocation) {
        ObjectFile linkedObj = linker.linkModules(files, fullRelocation, loadAddress, "LinkedProgram");
        addAndSaveObjectFileToList(linkedObj);
        return linkedObj;
    }


    /// Controle de execução do programa

    public void runNextInstruction() {
        machine.runCycle();
    }

    public void applyCycleDelay() {
        if (simulationSpeed > 0) {
            try {
                long delay = Map.simulationSpeedToCycleDelay(simulationSpeed);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.err.println("Execução interrompida: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void restartMachine() {
        setCodeLoaded(false);
        setSimulationFinished(false);
        machine.reset();
    }

    // Carrega um arquivo objeto na memória do simulador.
    @Deprecated
    public void loadProgramToMachine(ObjectFile selectedFile) {
        if (selectedFile != null) {
            // Carregaremos em 0x300 (ou 768 em decimal) na memória do simulador. ISSO É PROVISÓRIO!
            loader.loadObjectFile(
                    selectedFile,
                    machine.getMemory(),
                    0x300       // TODO: Revisar, carregar em 0x300 (768 em decimal) é provisório
            );
            setCodeLoaded(true);
            lastLoadedCode = selectedFile;
            notifyListeners();  // Notifica os listeners quando um novo arquivo é carregado
        }
    }

    public void loadProgramToMachine(ObjectFile selectedFile, int baseAddress) {
        if (selectedFile != null) {
            loader.loadObjectFile(selectedFile, machine.getMemory(), baseAddress);
            setCodeLoaded(true);
            lastLoadedCode = selectedFile;
            notifyListeners();
        }
    }

    /// ===== Manipulação da lista de arquivos objeto =====

    // TODO: Revisar, está dando acesso negado ao tentar salvar o arquivo
    public void addAndSaveObjectFileToList(ObjectFile objectFile) {
        File savedDir = new File(Constants.SAVE_DIR);
        if (!savedDir.exists()) {
            if (!savedDir.mkdirs()) {
                DialogUtil.showError("Erro ao criar diretório para salvar arquivos.");
                return;
            }
        }

        // Use .meta em vez de .obj
        File saveFile = new File(savedDir, objectFile.getProgramName() + ".meta");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(objectFile);
        } catch (IOException e) {
            DialogUtil.showError("Erro ao salvar o arquivo: " + e.getMessage());
        }

        notifyListeners();
    }

    public void loadObjectFilesFromSaveDir() {
        File savedDir = new File(Constants.SAVE_DIR);

        // Verifica se o diretório existe
        if (savedDir.exists() && savedDir.isDirectory()) {
            File[] files = savedDir.listFiles((dir, name) -> name.endsWith(".meta"));
            if (files != null) {
                for (File file : files) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        // Carrega o ObjectFile do arquivo
                        ois.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        // Agendar a exibição do erro na thread do JavaFX, se necessário
                        if (javafx.application.Platform.isFxApplicationThread()) {
                            DialogUtil.showError("Erro ao carregar arquivo: " + e.getMessage());
                        } else {
                            javafx.application.Platform.runLater(() ->
                                    DialogUtil.showError("Erro ao carregar arquivo: " + e.getMessage())
                            );
                        }
                    }
                }
            }
        }
    }

    public void removeAndDeleteObjectFileFromList(ObjectFile objectFile) {
        File objFile = new File(Constants.SAVE_DIR, objectFile.getProgramName() + ".obj");

        // Verifica se o arquivo existe e o deleta (para os .obj)
        if (objFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            objFile.delete();
        }

        File metaFile = new File(Constants.SAVE_DIR, objectFile.getProgramName() + ".meta");

        // Verifica se o arquivo existe e o deleta (para os .meta)
        if (metaFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            metaFile.delete();
        }
    }
}