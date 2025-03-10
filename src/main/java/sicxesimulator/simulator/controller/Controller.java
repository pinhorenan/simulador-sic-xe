package sicxesimulator.simulator.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import sicxesimulator.logger.SimulatorLogger;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.simulator.model.Model;
import sicxesimulator.simulator.view.MainView;
import sicxesimulator.simulator.view.components.tables.ObjectFileTableItem;
import sicxesimulator.simulator.view.records.MemoryEntry;

import sicxesimulator.simulator.view.records.RegisterEntry;
import sicxesimulator.simulator.view.records.SymbolEntry;
import sicxesimulator.utils.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    private final Model model;
    private final MainView view;

    public Controller(Model model, MainView view) {
        this.model = model;
        this.view = view;
        this.model.addListener(view::initializeView);
    }

    ///  ============== BOTÕES PRINCIPAIS =================== ///

    ///  Grupo Superior. SimulatorControls

    public void handleAssembleAction() {
        String sourceText = view.getInputField().getText();
        List<String> sourceLines = Arrays.asList(sourceText.split("\\r?\\n"));
        try {
            // Processa as macros e obtém o código expandido
            List<String> expandedSource = model.expandMacros(sourceLines);

            // Limpa a área de saída e a área de macros
            view.clearOutputArea();
            view.clearMacroOutArea();
            handleUpdateExpandedCode();

            // Agora passa o código expandido para o Assembler
            model.assembleCode(expandedSource);
            view.updateAllTables();

            // Pega o código objeto gerado

            ObjectFile objectFile = model.getMostRecentObjectFile();

            // Salva o código objeto
            saveObjectFile(objectFile);

            String formattedCode = model.getMostRecentObjectFile().toString();
            view.appendOutput("Programa montado e carregado com sucesso!");
            view.appendOutput(formattedCode);

            // Registra logs de montagem e código objeto
            SimulatorLogger.logAssemblyCode(sourceText);
            SimulatorLogger.logMachineCode(formattedCode);
        } catch (IllegalArgumentException | IOException e) {
            DialogUtil.showError("Erro na montagem: " + e.getMessage());
            SimulatorLogger.logError("Erro na montagem", e);
        }
    }

    // Método de carregamento de ObjectFile sem parâmetros
    public void handleLoadObjectFileAction() {
        // Obtenha os itens selecionados via selection model
        List<ObjectFileTableItem> selectedItems = view.getObjectFileTableView().getSelectionModel().getSelectedItems();

        if (selectedItems.size() == 1) {
            ObjectFile selectedFile = selectedItems.get(0).getObjectFile();
            handleLoadObjectFileAction(selectedFile);
        } else {
            DialogUtil.showError("Selecione apenas um arquivo para carregar!");
        }
    }



    // Sobrecarga com um parâmetro ObjectFile
    public void handleLoadObjectFileAction(ObjectFile selectedFile) {
        if (selectedFile == null) {
            DialogUtil.showError("Nenhum código foi montado ainda.");
            return;
        }
        model.loadObjectFile(selectedFile);
        model.setCodeLoaded(true);
        view.appendOutput("Arquivo montado carregado: " + selectedFile.getFilename());
        view.updateAllTables();
        handleUpdateExpandedCode();
    }

    public void handleUpdateExpandedCode() {
        try {
            List<String> sourceLines = Arrays.asList(view.getInputField().getText().split("\\r?\\n"));
            List<String> expanded = model.expandMacros(sourceLines);

            // Atualiza o expandedArea com o conteúdo expandido
            view.getMacroArea().setText(String.join("\n", expanded));
        } catch (IOException ex) {
            DialogUtil.showError("Erro ao expandir macros: " + ex.getMessage());
        }
    }

    public void handleLinkSelectedFilesAction() {
        // Obtém os itens selecionados via selection model
        List<ObjectFileTableItem> selectedItems = view.getObjectFileTableView().getSelectionModel().getSelectedItems();

        if (selectedItems.size() < 2) {
            DialogUtil.showError("Selecione ao menos 2 arquivos para linkar!");
            return;
        }

        // Converte os itens selecionados em ObjectFile
        List<ObjectFile> selectedFiles = selectedItems.stream()
                .map(ObjectFileTableItem::getObjectFile)
                .collect(Collectors.toList());

        // Realiza a linkagem
        handleLinkAction(selectedFiles.stream().map(ObjectFile::getFilename).collect(Collectors.toList()));
    }



    public void handleLinkAction(List<String> selectedFileNames) {
        if (selectedFileNames == null || selectedFileNames.isEmpty()) {
            DialogUtil.showError("Selecione ao menos um programa para linkar!");
            return;
        }

        List<ObjectFile> selectedObjectFiles = new ArrayList<>();
        for (String fileName : selectedFileNames) {
            ObjectFile obj = model.getObjectFileByName(fileName);
            if (obj != null) {
                selectedObjectFiles.add(obj);
            }
        }

        if (selectedObjectFiles.isEmpty()) {
            DialogUtil.showError("Nenhum ObjectFile válido foi encontrado para linkagem!");
            return;
        }

        try {
            int loadAddress = 0x300; // ou permitir que o usuário defina esse valor
            boolean fullRelocation = true; // conforme a necessidade
            ObjectFile linkedObject = model.linkProgram(selectedObjectFiles, loadAddress, fullRelocation);
            view.appendOutput("Linkagem concluída! Programa linkado: " + linkedObject.getFilename());
            view.updateAllTables();
        } catch (Exception e) {
            DialogUtil.showError("Erro durante a linkagem: " + e.getMessage());
        }
    }

    /// Grupo Inferior. ExecutionControls

    public void handleRunAction() {
        int pc = model.getMachine().getControlUnit().getIntValuePC();
        SimulatorLogger.logExecution("Início do ciclo de execução. PC inicial: " + String.format("%06X", pc));

        if (model.codeLoadedProperty().get()) {
            if (!model.simulationFinishedProperty().get()) {
                Task<Void> runTask = new Task<>() {
                    @Override
                    protected Void call() {
                        while (!model.simulationFinishedProperty().get() && !model.simulationPausedProperty().get()) {
                            try {
                                // Capture o valor do PC em uma variável final
                                final int currentPC = model.getMachine().getControlUnit().getIntValuePC();
                                SimulatorLogger.logExecution("Antes da instrução. PC: " + String.format("%06X", currentPC));

                                model.runNextInstruction();

                                String log = model.getMachine().getControlUnit().getLastExecutionLog();
                                if (log == null) {
                                    log = "Log de execução não disponível.";
                                }
                                SimulatorLogger.logExecution("Instrução executada: " + log);
                                model.setSimulationFinished(model.getMachine().getControlUnit().isHalted());
                                // Captura o log em uma variável final para uso na lambda
                                final String finalLog = log;
                                Platform.runLater(() -> {
                                    view.appendOutput(finalLog);
                                    view.updateAllTables();
                                });

                            } catch (Exception ex) {
                                String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
                                SimulatorLogger.logError("Erro durante execução. PC: "
                                        + model.getMachine().getControlUnit().getIntValuePC(), ex);
                                Platform.runLater(() -> DialogUtil.showError("Erro na execução: " + errorMsg));
                                break;
                            }
                            model.applyCycleDelay();
                        }
                        if (model.simulationFinishedProperty().get()) {
                            SimulatorLogger.logExecution("Execução concluída!");
                            Platform.runLater(() -> view.appendOutput("Execução concluída!"));
                        }
                        return null;
                    }
                };
                new Thread(runTask).start();
            } else {
                DialogUtil.showError("Fim do programa!");
            }
        } else {
            DialogUtil.showError("Nenhum programa montado!");
        }

        if (model.simulationFinishedProperty().get()) {
            Platform.runLater(() -> DialogUtil.showAlert(Alert.AlertType.INFORMATION,
                    "Execução Concluída",
                    "Simulação Finalizada",
                    "A simulação foi concluída com sucesso!"));
        }
    }

    public void handleNextAction() {
        if (model.codeLoadedProperty().get()) {
            if (!model.simulationFinishedProperty().get()) {
                try {
                    model.runNextInstruction();
                    String log = model.getMachine().getControlUnit().getLastExecutionLog();
                    view.appendOutput(log);
                    view.updateAllTables();
                    SimulatorLogger.logExecution(log);
                    model.setSimulationFinished(model.getMachine().getControlUnit().isHalted());
                } catch (Exception e) {
                    DialogUtil.showError("Erro na execução: " + e.getMessage());

                    // Registra no log
                    SimulatorLogger.logError("Erro na execução", e);
                }
            } else {
                DialogUtil.showError("Fim do programa!");
            }
        } else {
            DialogUtil.showError("Nenhum programa montado!");
        }
    }

    public void handlePauseAction() {
        if (!model.codeLoadedProperty().get()) {
            DialogUtil.showError("Nenhum programa em execução para pausar!");
            return;
        }
        if (model.codeLoadedProperty().get()) {
            view.appendOutput("Execução retomada!");
            SimulatorLogger.logExecution("Execução retomada.");
            model.setSimulationPaused(false);
        } else {
            view.appendOutput("Execução pausada!");
            SimulatorLogger.logExecution("Execução pausada.");
            model.setSimulationPaused(true);
        }
    }

    public void handleResetAction() {
        // Limpa o conteúdo de todas tabelas
        view.clearTables();

        // Atualiza as tabelas
        view.updateAllTables();

        // Limpa o conteúdo de todos TextArea
        view.getInputField().clear();
        view.getOutputArea().clear();
        view.getMacroArea().clear();

        // Atualiza todos os labels
        view.updateAllLabels();

        // Reseta o modelo
        model.reset();

        // Reseta o título da janela
        view.setWindowTitle("Simulador SIC/XE");

        // Exibe a mensagem de boas-vindas
        view.showWelcomeMessage();

        // Registra no log
        String resetMsg = "Simulação resetada. PC, registradores e memória reiniciados.";
        SimulatorLogger.logExecution(resetMsg);

        // Exibe um alerta de confirmação
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset");
        alert.setHeaderText("Simulação resetada");
        alert.setContentText("O estado da simulação foi completamente resetado.");
        alert.showAndWait();
    }

    ///  ============== MENU SUPERIOR =================== ///

    public void handleImportASM() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly", "*.asm"));

        // Define o diretório inicial
        File initialDir = new File(System.getProperty("user.dir") + "/src/main/resources/samples");
        if (initialDir.exists() && initialDir.isDirectory()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File file = fileChooser.showOpenDialog(view.getStage());

        if (file != null) {
            try {
                // Usa FileUtils para ler o conteúdo do arquivo inteiro com UTF-8
                String content = FileUtils.readFile(file.getAbsolutePath());
                // Atualiza o TextArea com o conteúdo lido
                view.getInputField().setText(content);
            } catch (IOException e) {
                DialogUtil.showError("Erro ao importar arquivo ASM: " + e.getMessage());
                SimulatorLogger.logError("Erro ao importar arquivo ASM", e);
            }
        }
    }

    public void handleExportASM() throws IOException {
        // Pega o código fonte do campo de entrada e processa os macros
        List<String> sourceLines = Arrays.asList(view.getInputField().getText().split("\\r?\\n"));
        List<String> expanded = model.expandMacros(sourceLines);

        // Configura o FileChooser com o diretório inicial desejado
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly Expandido", "*.asm"));

        // Define o diretório inicial para exportação
        File outputDir = new File(System.getProperty("user.dir") + "/src/main/resources/output");
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // Cria o diretório, se não existir
        }
        fileChooser.setInitialDirectory(outputDir);

        // Define o nome padrão: se houver um objeto montado, usa o programName, senão "Programa.asm"
        String defaultName = "Programa.asm";
        if (model.getMostRecentObjectFile() != null && model.getMostRecentObjectFile().getFilename() != null) {
            defaultName = model.getMostRecentObjectFile().getFilename() + ".asm";
        }
        fileChooser.setInitialFileName(defaultName);

        File file = fileChooser.showSaveDialog(view.getStage());
        if (file != null) {
            // Concatena as linhas em uma única string
            String expandedCode = String.join("\n", expanded);
            try {
                FileUtils.writeFile(file.getAbsolutePath(), expandedCode);
                System.out.println("Arquivo .ASM Expandido exportado com sucesso!");
            } catch (IOException e) {
                DialogUtil.showError("Erro ao tentar escrever o código expandido: " + e.getMessage());
                SimulatorLogger.logError("Erro ao tentar escrever o código expandido:", e);
            }
        }
    }

    public void handleExportOBJ() {
        // Obtém a entrada selecionada na TableView de arquivos montados
        ObjectFileTableItem selectedItem = view.getObjectFileTableView().getSelectionModel().getSelectedItem();

        // Se nenhum item foi selecionado, utiliza o objeto mais recente, se existir
        ObjectFile selectedFile;
        if (selectedItem == null) {
            if (model.getMostRecentObjectFile() != null) {
                selectedFile = model.getMostRecentObjectFile();
            } else {
                DialogUtil.showError("Nenhum código foi montado ainda.");
                return;
            }
        } else {
            selectedFile = selectedItem.getObjectFile(); // Obtém o ObjectFile a partir do ObjectFileTableItem
        }

        // Verifica se o ObjectFile foi encontrado
        if (selectedFile == null) {
            DialogUtil.showError("Arquivo de objeto não encontrado.");
            return;
        }

        // Obtém o conteúdo do código objeto
        byte[] objFileContent = selectedFile.getObjectCode();

        // Configura o FileChooser para salvar o arquivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos .OBJ", "*.obj"));
        File outputDir = new File(System.getProperty("user.dir") + "/src/main/resources/output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        fileChooser.setInitialDirectory(outputDir);

        // Define o nome padrão para o OBJ: usa o filename do ObjectFile
        String defaultName = selectedFile.getFilename();
        if (!defaultName.toLowerCase().endsWith(".obj")) {
            defaultName += ".obj";
        }
        fileChooser.setInitialFileName(defaultName);

        // Abre o FileChooser para o usuário escolher onde salvar
        File file = fileChooser.showSaveDialog(view.getStage());
        if (file != null) {
            try {
                // Converte o conteúdo do arquivo OBJ para hexadecimal
                StringBuilder hexContent = new StringBuilder();
                for (byte b : objFileContent) {
                    hexContent.append(String.format("%02X ", b));
                }
                // Escreve o conteúdo hexadecimal no arquivo
                FileUtils.writeFile(file.getAbsolutePath(), hexContent.toString());
                System.out.println("Arquivo .OBJ exportado com sucesso!");
            } catch (IOException e) {
                // Caso ocorra algum erro durante a escrita do arquivo
                DialogUtil.showError("Erro ao exportar arquivo OBJ: " + e.getMessage());
                SimulatorLogger.logError("Erro ao exportar arquivo OBJ", e);
            }
        }
    }


    ///  2. menuBar Montador

    ///  3) menuBar Memória

    public void handleClearMemoryAction() {
        model.getMachine().getMemory().clearMemory();
        view.updateMemoryTable();
        view.appendOutput("Memória limpa!");
    }

    public void handleChangeMemorySizeAction(int newSizeInBytes) {
        try {
            model.getMachine().changeMemorySize(newSizeInBytes);
            model.setMemorySize(newSizeInBytes);

            view.appendOutput("Memória alterada para " + newSizeInBytes + " bytes.");
            view.updateMemoryTable();
        } catch (Exception e) {
            DialogUtil.showError("Erro ao alterar o tamanho da memória: " + e.getMessage());
        }
    }

    ///  4) menuBar Execução

    public void handleChangeRunningSpeedAction(int newSimulationSpeed) {
        model.setSimulationSpeed(newSimulationSpeed);
    }

    ///  5) menuBar Exibir

    public void handleSetHexViewAction() {
        view.setViewFormat("HEX");
        view.updateViewFormatLabel();
    }

    public void handleSetOctalViewAction() {
        view.setViewFormat("OCT");
        view.updateViewFormatLabel();
    }

    public void handleSetDecimalViewAction() {
        view.setViewFormat("DEC");
        view.updateViewFormatLabel();
    }

    public void handleSetBinaryViewAction() {
        view.setViewFormat("BIN");
        view.updateViewFormatLabel();
    }

    ///  6) menuBar Ajuda

    public void handleHelpAction() {
        view.showHelpWindow();
    }

    ///  ============== MÉTODOS AUXILIARES =================== ///

    public int getMemorySize() {
        return model.getMemorySize();
    }

    public String getCycleDelay() {
        int simulationSpeed = model.getSimulationSpeed();
        return Mapper.mapSimulationSpeedToCycleDelay(simulationSpeed)+ "ms";
    }

    public List<MemoryEntry> getMemoryEntries() {
        List<MemoryEntry> entries = new ArrayList<>();
        var memory = model.getMachine().getMemory();
        for (int wordIndex = 0; wordIndex < memory.getAddressRange(); wordIndex++) {
            byte[] word = memory.readWord(wordIndex);
            int byteAddress = wordIndex * 3;
            String formattedAddress = ValueFormatter.formatAddress(byteAddress, model.getViewConfig().getAddressFormat());
            entries.add(new MemoryEntry(formattedAddress, Convert.bytesToHex(word)));
        }
        return entries;
    }

    public List<RegisterEntry> getRegisterEntries() {
        List<RegisterEntry> entries = new ArrayList<>();
        var regs = model.getMachine().getControlUnit().getRegisterSet().getAllRegisters();
        for (Register r : regs) {
            String value = ValueFormatter.formatRegisterValue(r, model.getViewConfig().getAddressFormat());
            entries.add(new RegisterEntry(r.getName(), value));
        }
        return entries;
    }

    public List<SymbolEntry> getSymbolEntries() {
        List<SymbolEntry> entries = new ArrayList<>();
        if (model.getMostRecentObjectFile() != null) {
            var symbols = model.getMostRecentObjectFile().getSymbolTable().getSymbols();
            symbols.forEach((name, wordAddress) -> {
                int byteAddr = wordAddress * 3;
                String formattedAddress = ValueFormatter.formatAddress(byteAddr, model.getViewConfig().getAddressFormat());
                entries.add(new SymbolEntry(name, formattedAddress));
            });
        }
        return entries;
    }

    public List<ObjectFile> getLinkableObjectFiles() {
        return model.getObjectFilesList();
    }

    /// ==================== MÉTODOS DE PROPRIEDADE ==================== ///

    public BooleanProperty getCodeLoadedProperty() {
        return model.codeLoadedProperty();
    }

    public BooleanProperty getSimulationFinishedProperty() {
        return model.simulationFinishedProperty();
    }

    public void saveObjectFile(ObjectFile objectFile) {
        // Cria o diretório "saved" caso não exista
        File savedDir = new File("src/main/resources/saved");
        if (!savedDir.exists()) {
            savedDir.mkdirs();
        }

        // Define o nome do arquivo como o nome do programa com a extensão.obj
        File file = new File(savedDir, objectFile.getFilename() + ".obj");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Escreve o ObjectFile no arquivo
            oos.writeObject(objectFile);
        } catch (IOException e) {
            DialogUtil.showError("Erro ao salvar o arquivo: " + e.getMessage());
        }
    }

    public List<ObjectFile> loadSavedObjectFiles() {
        List<ObjectFile> loadedFiles = new ArrayList<>();
        File savedDir = new File("src/main/resources/saved");

        // Verifica se o diretório existe
        if (savedDir.exists() && savedDir.isDirectory()) {
            File[] files = savedDir.listFiles((dir, name) -> name.endsWith(".obj"));  // Filtra arquivos .obj
            if (files != null) {
                for (File file : files) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        // Carrega o ObjectFile do arquivo
                        ObjectFile objectFile = (ObjectFile) ois.readObject();
                        loadedFiles.add(objectFile);
                    } catch (IOException | ClassNotFoundException e) {
                        DialogUtil.showError("Erro ao carregar arquivo: " + e.getMessage());
                    }
                }
            }
        }
        return loadedFiles;
    }

    public void handleInitializeObjectFiles() {
        // Carrega os arquivos salvos
        List<ObjectFile> savedFiles = loadSavedObjectFiles();

        // Converte os ObjectFiles para ObjectFileTableItems
        List<ObjectFileTableItem> tableItems = Convert.objectFileToObjectFileTableItem(savedFiles);

        // Atualiza a tabela de ObjectFiles na View
        view.updateObjectFileTableView(tableItems);
    }

    public void handleDeleteSelectedFilesAction() {
        // Obtém os itens selecionados via selection model
        List<ObjectFileTableItem> selectedItems = view.getObjectFileTableView().getSelectionModel().getSelectedItems();
        if (selectedItems == null || selectedItems.isEmpty()) {
            DialogUtil.showError("Nenhum arquivo selecionado para exclusão.");
            return;
        }

        // Itera sobre os itens selecionados e os remove
        for (ObjectFileTableItem item : selectedItems) {
            ObjectFile objectFile = item.getObjectFile();
            // Remove o ObjectFile do modelo
            model.removeObjectFile(objectFile);

            // Remove o arquivo físico do diretório "saved"
            File file = new File("src/main/resources/saved", objectFile.getFilename() + ".obj");
            if (file.exists()) {
                file.delete();
            }
        }

        // Atualiza a tabela com os arquivos restantes
        List<ObjectFileTableItem> tableItems = new ArrayList<>();
        for (ObjectFile file : model.getObjectFilesList()) {
            tableItems.add(new ObjectFileTableItem(file));
        }
        view.updateObjectFileTableView(tableItems);
    }
}