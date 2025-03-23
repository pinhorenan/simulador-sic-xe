package sicxesimulator.application.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import sicxesimulator.application.interfaces.ModelListener;
import sicxesimulator.application.model.records.MemoryEntry;
import sicxesimulator.application.model.records.RegisterEntry;
import sicxesimulator.application.model.records.SymbolEntry;
import sicxesimulator.application.util.DialogUtil;
import sicxesimulator.application.util.ValueFormatter;
import sicxesimulator.application.view.ViewConfig;
import sicxesimulator.hardware.cpu.Register;
import sicxesimulator.data.ObjectFile;
import sicxesimulator.software.assembler.Assembler;
import sicxesimulator.software.linker.Linker;
import sicxesimulator.software.loader.Loader;
import sicxesimulator.software.macroprocessor.MacroProcessor;
import sicxesimulator.hardware.Machine;
import sicxesimulator.utils.*;

import java.io.*;
import java.util.*;

public class Model {
    private final Machine machine;
    private final Loader loader;
    private final Linker linker;
    private final Assembler assembler;
    private final MacroProcessor macroProcessor;
    private final BooleanProperty codeLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty simulationPaused = new SimpleBooleanProperty(false);
    private final BooleanProperty simulationFinished = new SimpleBooleanProperty(false);
    private final List<ModelListener> listeners = new ArrayList<>();
    private final ViewConfig viewConfig = new ViewConfig();
    private ObjectFile lastLoadedCode;
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

    public int getSimulationSpeed() {
        return simulationSpeed;
    }

    public int getMemorySize() {
        return memorySize;
    }

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
            entries.add(new MemoryEntry(formattedAddress, Converter.bytesToHex(word)));
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

    public BooleanProperty codeLoadedProperty() {
        return codeLoaded;
    }

    public BooleanProperty simulationFinishedProperty() {
        return simulationFinished;
    }

    public BooleanProperty simulationPausedProperty() {
        return simulationPaused;
    }

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

    public void setCodeLoaded(boolean loaded) {
        codeLoaded.set(loaded);
    }

    public void setSimulationFinished(boolean finished) {
        simulationFinished.set(finished);
    }

    public void setSimulationPaused(boolean paused) {
        simulationPaused.set(paused);
    }

    /**
     * Processa as macros no código fonte fornecido.
     * @param rawSourceLines O código-fonte original, com as definições de macro.
     * @return O código-fonte processado, sem as definições de macro e com todos os macros expandidos.
     * @throws IOException Se ocorrer um erro de I/O.
     */
    public List<String> processCodeMacros(List<String> rawSourceLines) throws IOException {
        // Usa a constante TEMP_DIR definida em Constants
        FileUtils.ensureDirectoryExists(Constants.TEMP_DIR);

        // Define os caminhos completos para os arquivos temporários
        String tempInputFile = Constants.TEMP_DIR + "/temp.asm";

        // Escreve o código fonte original no arquivo de entrada usando FileUtils
        FileUtils.writeFile(tempInputFile, String.join("\n", rawSourceLines));

        // Processa as macros: o MacroProcessor lê o arquivo de entrada e gera o arquivo expandido
        macroProcessor.process(tempInputFile, "MASMAPRG.ASM");

        // Lê o conteúdo do arquivo expandido usando FileUtils
        String expandedContent = FileUtils.readFile(Constants.TEMP_DIR + "/" + "MASMAPRG.ASM");
        List<String> expanded = Arrays.asList(expandedContent.split("\\r?\\n"));
        System.out.println("Linhas expandidas (" + expanded.size() + "): " + expanded);
        return expanded;
    }

    /**
     * Monta o código fonte fornecido e retorna o arquivo objeto resultante.
     * @param rawSourceLines O código-fonte original, com as definições de macro (passado aqui para exibir na interface).
     * @param preProcessedSourceCode O código-fonte processado, sem as definições de macro e com todos os macros expandidos (que realmente é a base para a montagem).
     * @return O arquivo objeto resultante.
     * @throws IOException Se ocorrer um erro de I/O.
     */
    public ObjectFile assembleCode(List<String> rawSourceLines, List<String> preProcessedSourceCode) throws IOException {
        ObjectFile machineCode = assembler.assemble(rawSourceLines, preProcessedSourceCode);
        addAndSaveObjectFileToList(machineCode);

        return machineCode;
    }

    /**
     * Liga os arquivos objeto fornecidos e retorna o arquivo objeto resultante.
     * @param files Lista de arquivos objeto a serem ligados.
     * @param loadAddress Endereço de carga do programa.
     * @param fullRelocation Se o ligador deve fazer a realocação completa.
     * @return O arquivo objeto resultante.
     */
    public ObjectFile linkObjectFiles(List<ObjectFile> files, int loadAddress, boolean fullRelocation) {
        ObjectFile linkedObj = linker.linkModules(files, fullRelocation, loadAddress, "LinkedProgram");
        addAndSaveObjectFileToList(linkedObj);
        return linkedObj;
    }

    /// Controle de execução do programa

    /**
     * Chama o runCycle() da máquina para executar a próxima instrução.
     */
    public void runNextInstruction() {
        machine.runCycle();
    }

    /**
     * Aplica um delay conforme a velocidade de simulação atual.
     * Se a velocidade for 0, o retorna imediatamente.
     * Se a velocidade for 1 a 4, o dorme por um tempo proporcional à velocidade.
     * Esse delay só se aplica quando tentamos executar inteiramente o programa, sem passo a passo.
     */
    public void applyCycleDelay() {
        if (simulationSpeed > 0) {
            try {
                long delay = Mapper.simulationSpeedToCycleDelay(simulationSpeed);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.err.println("Execução interrompida: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Reinicia a máquina.
     * Isso limpa a memória e os registradores e redefine o estado da máquina.
     */
    public void restartMachine() {
        setCodeLoaded(false);
        setSimulationFinished(false);
        machine.reset();
    }

    /**
     * Carrega o programa do arquivo objeto fornecido na memória da máquina.
     * @param selectedFile O arquivo objeto a ser carregado.
     * @param baseAddress O endereço base de carga do programa.
     */
    public void loadProgramToMachine(ObjectFile selectedFile, int baseAddress) {
        if (selectedFile != null) {
            // Carrega o objeto na memória
            loader.loadObjectFile(selectedFile, machine.getMemory(), baseAddress);
            // Atualiza o PC para o startAddress definido no objeto final.
            // Assim, se o header indica E^000100, o PC passa a ser 0x100.
            machine.getControlUnit().setIntValuePC(selectedFile.getStartAddress());

            setCodeLoaded(true);
            lastLoadedCode = selectedFile;

            // Loga o estado logo após a carga para depuração.
            logDetailedState("Programa carregado em loadProgramToMachine()");

            notifyListeners();
        }
    }



    /// ===== Manipulação da lista de arquivos objeto =====

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

    public void deleteSavedProgram(ObjectFile objectFile) {
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

    /**
     * Loga o estado detalhado da máquina para auxiliar na depuração.
     * São registrados:
     * - A memória: endereços (a cada 3 bytes) que possuem valor diferente de zero,
     *   no formato "Endereço -> Valor".
     * - O estado de todos os registradores.
     * - O código objeto carregado (texto).
     * - A tabela de símbolos: mapeamento do símbolo para seu endereço.
     *
     * @param contextMessage Uma mensagem de contexto indicando onde o log foi invocado.
     */
    public void logDetailedState(String contextMessage) {
        String objectCodeText = "(Nenhum objeto carregado)";
        Map<String, Integer> symbolMap = new HashMap<>();
        String sourceCodeText = "(Nenhum código fonte disponível)";

        if (lastLoadedCode != null) {
            objectCodeText = lastLoadedCode.getObjectCodeAsString();

            // Converte o SymbolTable para Map<String, Integer>
            Map<String, sicxesimulator.data.Symbol> symbols = lastLoadedCode.getSymbolTable().getAllSymbols();
            for (Map.Entry<String, sicxesimulator.data.Symbol> entry : symbols.entrySet()) {
                symbolMap.put(entry.getKey(), entry.getValue().address);
            }

            // Junta o código-fonte (rawSourceCode) em uma única String
            List<String> rawSource = lastLoadedCode.getRawSourceCode();
            if (rawSource != null && !rawSource.isEmpty()) {
                sourceCodeText = String.join("\n", rawSource);
            }
        }

        // Captura o histórico da execução (caso haja)
        String executionOutput = machine.getControlUnit().getExecutionHistory();
        if (executionOutput == null || executionOutput.isEmpty()) {
            executionOutput = "(Sem saída de execução)";
        }

        DetailedLogger.logMachineState(
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
        for (ModelListener listener : listeners) {
            listener.onFilesUpdated();
        }
    }

}