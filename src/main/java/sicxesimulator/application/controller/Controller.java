package sicxesimulator.application.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import sicxesimulator.application.view.MainLayout;
import sicxesimulator.application.view.MainViewUpdater;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.application.model.Model;
import sicxesimulator.application.model.ObjectFileTableItem;
import sicxesimulator.application.model.records.MemoryEntry;

import sicxesimulator.application.model.records.RegisterEntry;
import sicxesimulator.application.model.records.SymbolEntry;
import sicxesimulator.utils.*;
import sicxesimulator.utils.Map;

import java.io.*;
import java.util.*;

public class Controller {
    private final Model model;
    private final MainLayout mainLayout;
    private final MainViewUpdater updater;

    public Controller(Model model, MainLayout mainLayout) {
        this.model = model;
        this.mainLayout = mainLayout;
        this.updater = new MainViewUpdater(this, mainLayout);
        this.model.addListener(this::initializeFilesView);
    }

    /// ===== Inicialização da Tabela de Arquivos Montados ===== ///

    public void initializeFilesView() {
        List<ObjectFile> files = loadSavedObjectFiles();
        var objectFileTable = mainLayout.getObjectFilePanel().getObjectFileTable();

        if (!files.isEmpty()) {
            objectFileTable.getItems().clear();
            for (ObjectFile file : files) {
                objectFileTable.addEntry(new ObjectFileTableItem(file));
            }
        }
    }

    private List<ObjectFile> loadSavedObjectFiles() {
        List<ObjectFile> objectFiles = new ArrayList<>();
        File savedDir = new File(Constants.SAVE_DIR);

        if (savedDir.exists() && savedDir.isDirectory()) {
            File[] objFiles = savedDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".obj"));
            if (objFiles != null) {
                for (File file : objFiles) {
                    try {
                        ObjectFile objectFile = ObjectFile.loadFromFile(file);
                        objectFiles.add(objectFile);
                    } catch (IOException e) {
                        DialogUtil.showError("Erro ao carregar arquivo salvo: " + file.getName());
                    }
                }
            }
        }
        return objectFiles;
    }

    /// ===== Controles de Montagem ===== ///

    public void handleAssembleAction() {
        String rawSourceText = mainLayout.getInputPanel().getInputText();
        List<String> rawSourceLines = Arrays.asList(rawSourceText.split("\\r?\\n"));

        try {
            List<String> processedSourceLines = model.processCodeMacros(rawSourceLines);
            ObjectFile objectFile = model.assembleCode(rawSourceLines, processedSourceLines);

            mainLayout.getOutputPanel().clearOutput();
            mainLayout.getInputPanel().setInputText(rawSourceText);

            mainLayout.getOutputPanel().getOutputArea().appendText("Programa montado com sucesso!\n" + objectFile + "\n");

            initializeFilesView();

            SimulatorLogger.logAssemblyCode(rawSourceText);
            SimulatorLogger.logMachineCode(objectFile.toString());
        } catch (IllegalArgumentException | IOException e) {
            DialogUtil.showError("Erro na montagem: " + e.getMessage());
            SimulatorLogger.logError("Erro na montagem", e);
        }
    }

    /// ===== Controles da Tabela e Arquivos Montados ===== ///

    public void handleLinkSelectedFilesAction() {
        List<ObjectFile> selectedFiles = mainLayout.getObjectFilePanel().getObjectFileTable().getSelectedFiles();

        if (selectedFiles.isEmpty()) {
            DialogUtil.showError("Selecione ao menos um programa para linkar!");
            return;
        }

        try {
            ObjectFile linkedObject = model.linkObjectFiles(selectedFiles, 0x300, true);
            DialogUtil.showAlert(Alert.AlertType.CONFIRMATION, "Arquivos Linkados",
                    "Arquivos linkados com sucesso!",
                    "O arquivo " + linkedObject.getFilename() + " foi criado com sucesso.");
            initializeFilesView();
        } catch (Exception e) {
            DialogUtil.showError("Erro na linkagem: " + e.getMessage());
        }
    }

    public void handleDeleteSelectedFilesAction() {
        var objectFileTable = mainLayout.getObjectFilePanel().getObjectFileTable();
        List<ObjectFileTableItem> selectedItems = new ArrayList<>(objectFileTable.getSelectionModel().getSelectedItems());

        if (selectedItems.isEmpty()) {
            DialogUtil.showError("Nenhum arquivo selecionado para exclusão.");
            return;
        }

        for (ObjectFileTableItem item : selectedItems) {
            ObjectFile objectFile = item.getObjectFile();
            model.removeAndDeleteObjectFileFromList(objectFile);
        }

        // Atualiza a tabela removendo os itens selecionados
        objectFileTable.getItems().removeAll(selectedItems);

        // Se a tabela ficou vazia, forçamos uma atualização manual
        if (objectFileTable.getItems().isEmpty()) {
            initializeFilesView();  // Isso garante que a tabela será atualizada corretamente.
        }
    }

    /// ===== Controles da Máquina ===== ///

    public void handleRunAction() {
        new Thread(() -> {
            while (!model.simulationFinishedProperty().get() && !model.simulationPausedProperty().get()) {
                try {
                    model.runNextInstruction();
                    Platform.runLater(() -> {
                        mainLayout.getOutputPanel().getOutputArea().appendText(model.getMachine().getControlUnit().getLastExecutionLog() + "\n");
                        updater.updateAllTables();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> DialogUtil.showError("Erro na execução: " + ex.getMessage()));
                    break;
                }
                model.applyCycleDelay();
            }
            Platform.runLater(() -> mainLayout.getOutputPanel().getOutputArea().appendText("Execução concluída!\n"));
        }).start();
    }

    public void handleNextAction() {
        if (model.codeLoadedProperty().get() && !model.simulationFinishedProperty().get()) {
            try {
                model.runNextInstruction();
                String log = model.getMachine().getControlUnit().getLastExecutionLog();
                mainLayout.getOutputPanel().getOutputArea().appendText(log + "\n");
                updater.updateAllTables();
                SimulatorLogger.logExecution(log);
                model.setSimulationFinished(model.getMachine().getControlUnit().isProcessorHalted());
            } catch (Exception e) {
                DialogUtil.showError("Erro na execução: " + e.getMessage());
                SimulatorLogger.logError("Erro na execução", e);
            }
        } else {
            DialogUtil.showError("Nenhum programa montado ou simulação já concluída!");
        }
    }

    public void handlePauseAction() {
        if (model.codeLoadedProperty().get()) {
            boolean isPaused = model.simulationPausedProperty().get();
            model.setSimulationPaused(!isPaused);

            String message = isPaused ? "Execução retomada!" : "Execução pausada!";
            mainLayout.getOutputPanel().getOutputArea().appendText(message + "\n");
            SimulatorLogger.logExecution(message);
        } else {
            DialogUtil.showError("Nenhum programa carregado para pausar!");
        }
    }

    public void handleRestartAction() {
        model.restartMachine();
        mainLayout.getOutputPanel().getOutputArea().appendText("Máquina reiniciada!\n");
    }

    public void handleLoadObjectFileAction() {
        List<ObjectFileTableItem> selectedItems = mainLayout.getObjectFilePanel().getObjectFileTable().getSelectionModel().getSelectedItems();

        // Não há checagem de arquivos selecionados, pois o botão só é habilitado se houver exatamente 1 arquivo selecionado

        ObjectFile selectedFile = selectedItems.getFirst().getObjectFile();

        model.loadProgramToMachine(selectedFile);
        mainLayout.getInputPanel().setInputText(String.join("\n", selectedFile.getRawSourceCode()));
        mainLayout.getOutputPanel().getOutputArea().appendText("Programa carregado: " + selectedFile.getFilename() + "\n");
    }

    /// ===== Controles de Entrada/Saída ===== ///

    public void handleClearOutputAction() {
        mainLayout.getOutputPanel().clearOutput();
    }

    /// ===== Métodos Getters ===== ///

    public Stage getStage() {
        return mainLayout.getRoot().getScene().getWindow() instanceof Stage ? (Stage) mainLayout.getRoot().getScene().getWindow() : null;
    }

    public Model getModel() {
        return model;
    }

    public String getAddressFormat() {
        return model.getViewConfig().getAddressFormat();
    }

    public int getMemorySize() {
        return model.getMemorySize();
    }

    public String getCycleDelay() {
        int simulationSpeed = model.getSimulationSpeed();
        return Map.simulationSpeedToCycleDelay(simulationSpeed)+ "ms";
    }

    public List<MemoryEntry> getMemoryEntries() {
       return model.getMemoryEntries();
    }

    public List<RegisterEntry> getRegisterEntries() {
        return model.getRegisterEntries();
    }

    public List<SymbolEntry> getSymbolEntries() {
        return model.getSymbolEntries();
    }

    public BooleanProperty getCodeLoadedProperty() {
        return model.codeLoadedProperty();
    }

    public BooleanProperty getSimulationFinishedProperty() {
        return model.simulationFinishedProperty();
    }

    /// ===== Controles de Configuração ===== ///

    public void loadInputField(String content) {
        mainLayout.getInputPanel().setInputText(content);
    }

    public List<String> getExpandedCode() throws IOException {
        return model.processCodeMacros(Arrays.asList(mainLayout.getInputPanel().getInputText().split("\\r?\\n")));
    }

    public byte[] getObjectFileBytes() {
        ObjectFile selectedFile = model.getLastLoadedCode();
        return (selectedFile != null) ? selectedFile.getMachineCode() : null;
    }

    public String getSuggestedFileName(String extension) {
        ObjectFile lastLoaded = model.getLastLoadedCode();
        return (lastLoaded != null) ? lastLoaded.getFilename() + extension : "Programa" + extension;
    }

    public void clearMemory() {
        model.getMachine().getMemory().clearMemory();
        updater.updateMemoryTableView();
        mainLayout.getOutputPanel().getOutputArea().appendText("Memória limpa!\n");
        // Atualiza a label da bottom bar
        if (mainLayout.getBottomBarPanel() != null) {
            mainLayout.getBottomBarPanel().updateMemoryLabel();
        }
    }

    public void changeMemorySize(int newSizeInBytes) {
        try {
            model.getMachine().changeMemorySize(newSizeInBytes);
            model.setMemorySize(newSizeInBytes);
            mainLayout.getOutputPanel().getOutputArea().appendText("Memória alterada para " + newSizeInBytes + " bytes.\n");
            updater.updateMemoryTableView();
            if (mainLayout.getBottomBarPanel() != null) {
                mainLayout.getBottomBarPanel().updateMemoryLabel();
            }
        } catch (Exception e) {
            DialogUtil.showError("Erro ao alterar o tamanho da memória: " + e.getMessage());
        }
    }

    public void setSimulationSpeed(int speed) {
        model.setSimulationSpeed(speed);
        if (mainLayout.getBottomBarPanel() != null) {
            mainLayout.getBottomBarPanel().updateSpeedLabel();
        }
    }

    public void setViewFormat(String format) {
        model.getViewConfig().setAddressFormat(format);
        if (mainLayout.getBottomBarPanel() != null) {
            mainLayout.getBottomBarPanel().updateFormatLabel();
        }
    }

    public void showHelpWindow() {
        DialogUtil.showInfo("Aqui está a ajuda do simulador SIC/XE. (nenhuma)");
    }

    public void updateAllTables() {
        updater.updateAllTables();
    }

    public void updateAllLabels() {
        updater.updateAllLabels();
    }

    public MainLayout getMainLayout() {
        return mainLayout;
    }

    public MainViewUpdater getUpdater() {
        return updater;
    }
}