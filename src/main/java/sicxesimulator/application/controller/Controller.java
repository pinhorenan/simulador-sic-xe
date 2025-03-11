package sicxesimulator.application.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import sicxesimulator.utils.SimulatorLogger;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.application.model.Model;
import sicxesimulator.application.view.MainView;
import sicxesimulator.application.model.ObjectFileTableItem;
import sicxesimulator.application.model.records.MemoryEntry;

import sicxesimulator.application.model.records.RegisterEntry;
import sicxesimulator.application.model.records.SymbolEntry;
import sicxesimulator.utils.*;
import sicxesimulator.utils.Map;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
public class Controller {
    private final Model model;
    private final MainView view;

    public Controller(Model model, MainView view) {
        this.model = model;
        this.view = view;
        this.model.addListener(view::initializeFilesView);
    }

    /// ===== Controles de Montagem ===== ///

    // Ação do botão "Montar"
    public void handleAssembleAction() {
        String rawSourceText = view.getInputField().getText();
        List<String> rawSourceLines = Arrays.asList(rawSourceText.split("\\r?\\n"));
        List<String> processedSourceLines;

        try {
            // Processa as macros e obtém o código expandido
            processedSourceLines = model.processCodeMacros(rawSourceLines);


            // Pega o código objeto gerado
            ObjectFile objectFile = model.assembleCode(processedSourceLines);

            // Define o código fonte e o código processado no objeto
            objectFile.setRawSourceCode(rawSourceLines);
            objectFile.setProcessedSourceCode(processedSourceLines);

            // Limpa a área de saída e a área de macros
            view.clearOutputArea();
            view.clearMacroOutArea();
            view.updateMacroOutputContent();

            // Imprime mensagem de confirmação e String do ObjectFile na saída da máquina
            view.appendOutput("Programa montado com sucesso!");
            view.appendOutput(objectFile.toString());

            // Atualiza as tabelas da view
            view.updateAllTables();

            // Registra logs de montagem e código objeto
            SimulatorLogger.logAssemblyCode(rawSourceText);
            SimulatorLogger.logMachineCode(objectFile.toString());
        } catch (IllegalArgumentException | IOException e) {
            DialogUtil.showError("Erro na montagem: " + e.getMessage());
            SimulatorLogger.logError("Erro na montagem", e);
        }
    }

    // Ação do botão "Linkar"
    public void handleLinkSelectedFilesAction() {
        List<ObjectFile> selectedFiles = view.getObjectFileTableView().getSelectionModel()
                .getSelectedItems()
                .stream()
                .map(ObjectFileTableItem::getObjectFile)
                .collect(Collectors.toList());

        if (selectedFiles.isEmpty()) {
            DialogUtil.showError("Selecione ao menos um programa para linkar!");
            return;
        }

        try {
            int loadAddress = 0x300; // endereço de carga padrão
            boolean fullRelocation = true;

            // Realiza a linkagem e obtém o objeto resultante
            ObjectFile linkedObject = model.linkObjectFiles(selectedFiles, loadAddress, fullRelocation);

            view.appendOutput("Linkagem concluída! Programa linkado: " + linkedObject.getFilename());
            view.updateAllTables();
        } catch (Exception e) {
            DialogUtil.showError("Erro durante a linkagem: " + e.getMessage());
        }
    }

    // Ação do botão "Deletar"
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
            model.removeAndDeleteObjectFileFromList(objectFile);
        }
        view.updateObjectFileTableView();
    }

    /// ===== Controles da Máquina ===== ///

    // Ação do botão "Executar"
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
                                model.setSimulationFinished(model.getMachine().getControlUnit().isProcessorHalted());
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

    // Ação do botão "Próximo"
    public void handleNextAction() {
        if (model.codeLoadedProperty().get()) {
            if (!model.simulationFinishedProperty().get()) {
                try {
                    model.runNextInstruction();
                    String log = model.getMachine().getControlUnit().getLastExecutionLog();
                    view.appendOutput(log);
                    view.updateAllTables();
                    SimulatorLogger.logExecution(log);
                    model.setSimulationFinished(model.getMachine().getControlUnit().isProcessorHalted());
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

    // Ação do botão "Pausar/Retomar"; TODO: Mudar texto conforme o estado do modelo.
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

    // Ação do botão "Reiniciar"
    public void handleRestartAction() {
        view.appendOutput(model.restartMachine());
        view.updateAllTables();
    }

    // Ação do botão "Carregar "
    public void handleLoadObjectFileAction() {
        // Obtém os itens selecionados via selection model
        List<ObjectFileTableItem> selectedItems = view.getObjectFileTableView().getSelectionModel().getSelectedItems();

        if (selectedItems.size() != 1) {
            DialogUtil.showError("Selecione apenas um arquivo para carregar!");
            return;
        }

        ObjectFile selectedFile = selectedItems.getFirst().getObjectFile();

        if (selectedFile == null) {
            DialogUtil.showError("Nenhum código foi montado ainda.");
            return;
        }

        model.loadProgramToMachine(selectedFile);
        model.setCodeLoaded(true);
        model.setSimulationFinished(false);

        view.getInputField().clear();
        view.getInputField().setText(String.join("\n", selectedFile.getRawSourceCode()));

        view.getMacroArea().clear();
        view.getMacroArea().setText(String.join("\n", selectedFile.getProcessedSourceCode()));

        view.updateAllTables();
        view.appendOutput("Programa carregado: " + selectedFile.getFilename());

        view.updateMacroOutputContent();
    }

    /// ===== Controles de Entrada/Saída ===== ///

    // Ação do botão "Limpar Saída"
    public void handleClearOutputAction() {
        view.clearOutputArea();
    }

    ///  ============== MÉTODOS AUXILIARES =================== ///

    public MainView getView() {
        return view;
    }

    public Model getModel() {
        return model;
    }

    public int getMemorySize() {
        return model.getMemorySize();
    }

    public String getCycleDelay() {
        int simulationSpeed = model.getSimulationSpeed();
        return Map.simulationSpeedToCycleDelay(simulationSpeed)+ "ms";
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
        ObjectFile objectFile = model.getLastLoadedCode();

        var symbols = objectFile.getSymbolTable().getSymbols();
        symbols.forEach((name, wordAddress) -> {
                int byteAddr = wordAddress * 3;
                String formattedAddress = ValueFormatter.formatAddress(byteAddr, model.getViewConfig().getAddressFormat());
                entries.add(new SymbolEntry(name, formattedAddress));
            });

        return entries;
    }

    public List<ObjectFile> getObjectFilesFromModel() {
        return model.getObjectFilesList();
    }

    public List<String> getMacroExpandedCode() {
        List<String> sourceLines = Arrays.asList(view.getInputField().getText().split("\\r?\\n"));
        try {
            return model.processCodeMacros(sourceLines);
        } catch (IOException e) {
            DialogUtil.showError("Erro ao expandir macros: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /// ==================== MÉTODOS DE PROPRIEDADE ==================== ///

    public BooleanProperty getCodeLoadedProperty() {
        return model.codeLoadedProperty();
    }

    public BooleanProperty getSimulationFinishedProperty() {
        return model.simulationFinishedProperty();
    }
}