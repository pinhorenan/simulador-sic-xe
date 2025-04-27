package sicxesimulator.ui.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import sicxesimulator.ui.interfaces.ModelListener;
import sicxesimulator.ui.data.records.MemoryEntry;
import sicxesimulator.ui.data.records.RegisterEntry;
import sicxesimulator.ui.data.records.SymbolEntry;
import sicxesimulator.ui.util.DialogUtil;
import sicxesimulator.hardware.cpu.register.Register;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.assembler.Assembler;
import sicxesimulator.software.linker.Linker;
import sicxesimulator.software.loader.Loader;
import sicxesimulator.software.macroprocessor.MacroProcessor;
import sicxesimulator.hardware.system.Machine;
import sicxesimulator.common.utils.Convert;
import sicxesimulator.common.utils.FileUtils;
import sicxesimulator.common.utils.Logger;
import sicxesimulator.common.utils.Constants;

import java.io.*;
import java.util.*;

public class Model {
    private final Machine machine;
    private final Loader loader;
    private final Linker linker;
    private final Assembler assembler;
    private final MacroProcessor macroProcessor;
    private final BooleanProperty codeLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty simulationFinished = new SimpleBooleanProperty(false);
    private final List<ModelListener> listeners = new ArrayList<>();
    private ObjectFile lastLoadedCode;
    private LinkerMode linkerMode = LinkerMode.ABSOLUTO;

    public enum LinkerMode {
        ABSOLUTO,
        RELOCAVEL
    }

    public Model() {
        this.machine = new Machine();
        this.loader = new Loader();
        this.macroProcessor = new MacroProcessor();
        this.assembler = new Assembler();
        this.linker = new Linker();
        loadObjectFilesFromSaveDir();
    }

    public LinkerMode getLinkerMode() {
        return linkerMode;
    }

    public void setLinkerMode(LinkerMode newMode) {
        this.linkerMode = newMode;
    }

    /** Agora expõe diretamente o size em bytes. */
    public int getMemorySize() {
        return machine.getMemory().getSize();
    }

    public Machine getMachine() {
        return machine;
    }

    public List<MemoryEntry> getMemoryEntries() {
        List<MemoryEntry> entries = new ArrayList<>();
        var memory = machine.getMemory();
        int totalBytes = memory.getSize();
        int wordCount = totalBytes / 3;
        for (int wordIndex = 0; wordIndex < wordCount; wordIndex++) {
            byte[] word = memory.readWord(wordIndex);
            String address = Convert.intToHexString24(wordIndex * 3);
            entries.add(new MemoryEntry(address, Convert.bytesToHex(word)));
        }
        return entries;
    }

    public List<RegisterEntry> getRegisterEntries() {
        List<RegisterEntry> entries = new ArrayList<>();
        var registers = machine.getControlUnit().getRegisterSet().getAllRegisters();
        for (Register register : registers) {
            String value;
            if ("F".equals(register.getName())) {
                value = Convert.longToHexString48(register.getLongValue());
            } else {
                value = Convert.intToHexString24(register.getIntValue());
            }
            entries.add(new RegisterEntry(register.getName(), value));
        }
        return entries;
    }

    public List<SymbolEntry> getSymbolEntries() {
        List<SymbolEntry> entries = new ArrayList<>();
        if (lastLoadedCode != null) {
            lastLoadedCode.getSymbolTable().getAllSymbols().forEach((name, info) -> {
                String address = Convert.intToHexString24(info.address);
                entries.add(new SymbolEntry(name, address));
            });
        }
        return entries;
    }

    public BooleanProperty codeLoadedProperty() {
        return codeLoaded;
    }

    public BooleanProperty simulationFinishedProperty() {
        return simulationFinished;
    }

    public void setMemorySize(int newMemorySize) {
        machine.changeMemorySize(newMemorySize);
    }

    public void setCodeLoaded(boolean loaded) {
        codeLoaded.set(loaded);
    }

    public void setSimulationFinished(boolean finished) {
        simulationFinished.set(finished);
    }

    public List<String> processCodeMacros(List<String> rawSourceLines) throws IOException {
        FileUtils.ensureDirectoryExists(Constants.TEMP_DIR);
        String tempInputFile = Constants.TEMP_DIR + "/temp.asm";
        FileUtils.writeFile(tempInputFile, String.join("\n", rawSourceLines));
        macroProcessor.process(tempInputFile, Constants.TEMP_DIR + "/MASMAPRG.ASM");
        String expanded = FileUtils.readFile(Constants.TEMP_DIR + "/MASMAPRG.ASM");
        return Arrays.asList(expanded.split("\\r?\\n"));
    }

    public ObjectFile assembleCode(List<String> rawSourceLines, List<String> preProcessedSourceCode) throws IOException {
        ObjectFile machineCode = assembler.assemble(rawSourceLines, preProcessedSourceCode);
        addAndSaveObjectFileToList(machineCode);
        return machineCode;
    }

    public ObjectFile linkObjectFiles(List<ObjectFile> files, int loadAddress, boolean fullRelocation, String linkedName) {
        ObjectFile linkedObj = linker.linkModules(files, fullRelocation, loadAddress, linkedName);
        addAndSaveObjectFileToList(linkedObj);
        return linkedObj;
    }

    public void runNextInstruction() {
        machine.runCycle();
    }

    public void restartMachine() {
        setCodeLoaded(false);
        setSimulationFinished(false);
        machine.reset();
    }

    public void loadProgramToMachine(ObjectFile selectedFile, int baseAddress) {
        if (selectedFile != null) {
            loader.loadObjectFile(selectedFile, machine.getMemory(), baseAddress);
            machine.getControlUnit().setIntValuePC(selectedFile.getStartAddress());
            setCodeLoaded(true);
            lastLoadedCode = selectedFile;
            logDetailedState("Programa carregado em loadProgramToMachine()");
            notifyListeners();
        }
    }

    public void addAndSaveObjectFileToList(ObjectFile objectFile) {
        File savedDir = new File(Constants.SAVE_DIR);
        if (!savedDir.exists() && !savedDir.mkdirs()) {
            DialogUtil.showError("Erro ao criar diretório para salvar arquivos.");
            return;
        }
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
        if (savedDir.exists() && savedDir.isDirectory()) {
            File[] files = savedDir.listFiles((d, name) -> name.endsWith(".meta"));
            if (files != null) for (File file : files) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    Runnable showErr = () -> DialogUtil.showError("Erro ao carregar arquivo: " + e.getMessage());
                    if (javafx.application.Platform.isFxApplicationThread()) showErr.run();
                    else javafx.application.Platform.runLater(showErr);
                }
            }
        }
    }

    public void deleteSavedProgram(ObjectFile objectFile) {
        boolean ok1 = new File(Constants.SAVE_DIR, objectFile.getProgramName() + ".obj").delete();
        boolean ok2 = new File(Constants.SAVE_DIR, objectFile.getProgramName() + ".meta").delete();
        if (!ok1 || !ok2) {
            DialogUtil.showError("Falha ao apagar arquivos salvos.");
        }
    }

    public void logDetailedState(String contextMessage) {
        Map<String, Integer> symbolMap = new HashMap<>();
        String objectCodeText = "(Nenhum objeto carregado)";
        String sourceCodeText = "(Nenhum código fonte disponível)";
        if (lastLoadedCode != null) {
            objectCodeText = lastLoadedCode.getObjectCodeAsString();
            lastLoadedCode.getSymbolTable().getAllSymbols()
                    .forEach((name, info) -> symbolMap.put(name, info.address));
            var raw = lastLoadedCode.getRawSourceCode();
            if (raw != null && !raw.isEmpty()) sourceCodeText = String.join("\n", raw);
        }

        List<String> executionOutput = new ArrayList<>(machine.getControlUnit().getExecutionHistory());
        if (executionOutput.isEmpty()) {
            executionOutput = List.of("(Sem saída de execução)");
        }

        Logger.logMachineState(
                machine.getMemory(),
                machine.getControlUnit().getRegisterSet(),
                objectCodeText,
                symbolMap,
                sourceCodeText,
                executionOutput,
                contextMessage
        );
    }

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        listeners.forEach(ModelListener::onFilesUpdated);
    }
}
