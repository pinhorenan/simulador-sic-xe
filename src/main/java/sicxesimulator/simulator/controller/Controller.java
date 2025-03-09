package sicxesimulator.simulator.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.stage.FileChooser;
import sicxesimulator.logger.SimulatorLogger;
import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.simulator.model.Model;
import sicxesimulator.simulator.view.MainView;
import sicxesimulator.simulator.view.components.MemoryEntry;
import sicxesimulator.simulator.view.components.RegisterEntry;
import sicxesimulator.simulator.view.components.SymbolEntry;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.Mapper;
import sicxesimulator.utils.ValueFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Controller {
    private final Model model;
    private final MainView mainView;


    public Controller(Model model, MainView mainView) {
        this.model = model;
        this.mainView = mainView;
        this.model.addListener(mainView::initializeView);
    }

    ///  ============== BOTÕES PRINCIPAIS =================== ///

    ///  Grupo Superior. SimulatorControls

    public void handleAssembleAction() {
        String sourceText = mainView.getInputField().getText();
        List<String> sourceLines = Arrays.asList(sourceText.split("\\r?\\n"));
        try {
            // Processa as macros e obtém o código expandido
            List<String> expandedSource = model.expandMacros(sourceLines);

            // Limpa a área de saída e a área de macros
            mainView.clearOutputArea();
            mainView.clearMacroOutArea();
            handleUpdateExpandedCode();

            // Agora passa o código expandido para o Assembler
            model.assembleCode(expandedSource);
            mainView.updateAllTables();
            model.setCodeAssembled(true);

            String formattedCode = model.getMostRecentObjectFile().toString();
            mainView.appendOutput("Programa montado e carregado com sucesso!");
            mainView.appendOutput(formattedCode);

            // Registra logs de montagem e código objeto
            SimulatorLogger.logAssemblyCode(sourceText);
            SimulatorLogger.logMachineCode(formattedCode);
        } catch (IllegalArgumentException | IOException e) {
            mainView.showError("Erro na montagem: " + e.getMessage());
            SimulatorLogger.logError("Erro na montagem", e);
        }
    }

    public void handleLoadObjectFileAction() {
        List<ObjectFile> objectFiles = model.getObjectFilesList();
        if (objectFiles.isEmpty()) {
            mainView.showError("Nenhum código foi montado ainda.");
            return;
        }
        ChoiceDialog<ObjectFile> dialog = new ChoiceDialog<>(objectFiles.getLast(), objectFiles);
        dialog.setTitle("Carregar Arquivo Montado");
        dialog.setHeaderText("Escolha um arquivo para carregar");
        dialog.setContentText("Arquivos disponíveis:");

        // Customiza o DialogPane com estilos CSS
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-background-color: #f0f0f0;");

        dialog.showAndWait().ifPresent(selected -> {
            model.loadObjectFile(selected);
            model.setCodeLoaded(true);
            mainView.appendOutput("Arquivo montado carregado: " + selected.getFilename());
            mainView.updateAllTables();
            handleUpdateExpandedCode();
        });

    }

    public void handleUpdateExpandedCode() {
        try {
            List<String> sourceLines = Arrays.asList(mainView.getInputField().getText().split("\\r?\\n"));
            List<String> expanded = model.expandMacros(sourceLines);

            // Atualiza o expandedArea com o conteúdo expandido
            mainView.getExpandedArea().setText(String.join("\n", expanded));
        } catch (IOException ex) {
            mainView.showError("Erro ao expandir macros: " + ex.getMessage());
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
                                    mainView.appendOutput(finalLog);
                                    mainView.updateAllTables();
                                });

                            } catch (Exception ex) {
                                String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
                                SimulatorLogger.logError("Erro durante execução. PC: "
                                        + model.getMachine().getControlUnit().getIntValuePC(), ex);
                                Platform.runLater(() -> mainView.showError("Erro na execução: " + errorMsg));
                                break;
                            }
                            model.applyCycleDelay();
                        }
                        if (model.simulationFinishedProperty().get()) {
                            SimulatorLogger.logExecution("Execução concluída!");
                            Platform.runLater(() -> mainView.appendOutput("Execução concluída!"));
                        }
                        return null;
                    }
                };
                new Thread(runTask).start();
            } else {
                mainView.showError("Fim do programa!");
            }
        } else {
            mainView.showError("Nenhum programa montado!");
        }

        if (model.simulationFinishedProperty().get()) {
            Platform.runLater(() -> mainView.showAlert(Alert.AlertType.INFORMATION,
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
                    mainView.appendOutput(log);
                    mainView.updateAllTables();
                    SimulatorLogger.logExecution(log);
                    model.setSimulationFinished(model.getMachine().getControlUnit().isHalted());
                } catch (Exception e) {
                    mainView.showError("Erro na execução: " + e.getMessage());

                    // Registra no log
                    SimulatorLogger.logError("Erro na execução", e);
                }
            } else {
                mainView.showError("Fim do programa!");
            }
        } else {
            mainView.showError("Nenhum programa montado!");
        }
    }

    public void handlePauseAction() {
        if (!model.codeLoadedProperty().get()) {
            mainView.showError("Nenhum programa em execução para pausar!");
            return;
        }
        if (model.codeLoadedProperty().get()) {
            mainView.appendOutput("Execução retomada!");
            SimulatorLogger.logExecution("Execução retomada.");
            model.setSimulationPaused(false);
        } else {
            mainView.appendOutput("Execução pausada!");
            SimulatorLogger.logExecution("Execução pausada.");
            model.setSimulationPaused(true);
        }
    }

    public void handleResetAction() {
        // Limpa o conteúdo de todas tabelas
        mainView.clearTables();

        // Atualiza as tabelas
        mainView.updateAllTables();

        // Limpa o conteúdo de todos TextArea
        mainView.getInputField().clear();
        mainView.getOutputArea().clear();
        mainView.getExpandedArea().clear();

        // Atualiza todos os labels
        mainView.updateAllLabels();


        // Reseta o modelo
        model.reset();

        // Reseta o título da janela
        mainView.setWindowTitle("Simulador SIC/XE");

        // Exibe a mensagem de boas-vindas
        mainView.showWelcomeMessage();

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

    /// 1) menuBar Arquivo

    public void handleImportASM() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly", "*.asm"));
        File file = fileChooser.showOpenDialog(mainView.getStage());

        if (file != null) {
            try {
                StringBuilder content = new StringBuilder();
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine()).append("\n");
                }
                // Aqui chamamos o método da View para atualizar o TextArea com o conteúdo do arquivo
                mainView.getInputField().setText(content.toString());
                scanner.close();
            } catch (FileNotFoundException e) {
                mainView.showError("Erro ao importar arquivo ASM: " + e.getMessage());
                SimulatorLogger.logError("Erro ao importar arquivo ASM", e);
            }
        }
    }

    public void handleExportASM() throws IOException {
        // Pega o código fonte do campo de entrada e processa os macros
        List<String> sourceLines = Arrays.asList(mainView.getInputField().getText().split("\\r?\\n"));
        List<String> expanded = model.expandMacros(sourceLines);

        // Abrir o FileChooser para salvar o arquivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly Expandido", "*.asm"));
        File file = fileChooser.showSaveDialog(mainView.getStage());

        if (file != null) {
            // Concatena as linhas da lista de strings em uma única string
            String expandedCode = String.join("\n", expanded);

            // Tenta escrever o código expandido no arquivo
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(expandedCode);  // Escreve o código expandido no arquivo
                System.out.println("Arquivo .ASM Expandido exportado com sucesso!");
            } catch (IOException e) {
                mainView.showError("Erro ao tentar escrever o código expandido: " + e.getMessage());
                SimulatorLogger.logError("Erro ao tentar escrever o código expandido: ", e);
            }
        }
    }

    public void handleExportOBJ() {
        // Obtém o arquivo selecionado na ListView
        String selectedFileName = mainView.getObjectFileListView().getSelectionModel().getSelectedItem();

        if (selectedFileName == null) {
            // Exibe a mensagem "Sem arquivos montados" se não houver arquivos selecionados
            mainView.showNoFilesMessage();
            return;  // Se o usuário não selecionou nenhum arquivo, sai do método
        }

        // Obter o objeto `ObjectFile` correspondente ao nome selecionado
        ObjectFile selectedFile = model.getObjectFileByName(selectedFileName);

        // Obtém o conteúdo do código objeto
        byte[] objFileContent = selectedFile.getObjectCode();  // Pega o código objeto

        // Abrir o FileChooser para salvar o arquivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos .OBJ", "*.obj"));
        File file = fileChooser.showSaveDialog(mainView.getStage());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Escreve o conteúdo do código objeto no arquivo
                for (byte b : objFileContent) {
                    writer.write(String.format("%02X ", b));  // Escreve o byte como hex
                }
                System.out.println("Arquivo .OBJ exportado com sucesso!");
            } catch (IOException e) {
                mainView.showError("Erro ao expandir macros: " + e.getMessage());
                SimulatorLogger.logError("Erro ao expandir macros", e);
            }
        }
    }

    ///  2) menuBar Códigos Exemplo

    public void handleLoadSampleCodeAction(String sampleCode, String title) throws IOException {
        model.loadSampleCode(sampleCode, mainView, title);
        mainView.showAlert(Alert.AlertType.INFORMATION,
                "Código Carregado",
                "Exemplo Carregado",
                "O código de exemplo foi carregado com sucesso!");
        handleUpdateExpandedCode();
    }

    ///  3) menuBar Memória

    public void handleClearMemoryAction() {
        model.getMachine().getMemory().clearMemory();
        mainView.updateMemoryTable();
        mainView.appendOutput("Memória limpa!");
    }

    public void handleChangeMemorySizeAction(int newSizeInBytes) {
        try {
            model.getMachine().changeMemorySize(newSizeInBytes);
            model.setMemorySize(newSizeInBytes);

            mainView.appendOutput("Memória alterada para " + newSizeInBytes + " bytes.");
            mainView.updateMemoryTable();
        } catch (Exception e) {
            mainView.showError("Erro ao alterar o tamanho da memória: " + e.getMessage());
        }
    }

    ///  4) menuBar Execução

    public void handleChangeRunningSpeedAction(int newSimulationSpeed) {
        model.setSimulationSpeed(newSimulationSpeed);
    }

    ///  5) menuBar Exibir

    public void handleSetHexViewAction() {
        mainView.setViewFormat("HEX");
        mainView.updateViewFormatLabel();
    }

    public void handleSetOctalViewAction() {
        mainView.setViewFormat("OCT");
        mainView.updateViewFormatLabel();
    }

    public void handleSetDecimalViewAction() {
        mainView.setViewFormat("DEC");
        mainView.updateViewFormatLabel();
    }

    public void handleSetBinaryViewAction() {
        mainView.setViewFormat("BIN");
        mainView.updateViewFormatLabel();
    }

    ///  6) menuBar Ajuda

    public void handleHelpAction() {
        mainView.showHelpWindow();
    }

    ///  ============== MÉTODOS AUXILIARES =================== ///

    public int getMemorySize() {
        return model.getMemorySize();
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

    public List<String> getObjectFileNames() {
        return model.getObjectFileNames();
    }

    public String getCycleDelay() {
        int simulationSpeed = model.getSimulationSpeed();
        return Mapper.mapSimulationSpeedToCycleDelay(simulationSpeed)+ "ms";

    }

    // Retorne a propriedade codeAssembled do Model
    public BooleanProperty getCodeAssembledProperty() {
        return model.codeAssembledProperty();
    }

    public BooleanProperty getCodeLoadedProperty() {
        return model.codeLoadedProperty();
    }

    public BooleanProperty getSimulationFinishedProperty() {
        return model.simulationFinishedProperty();
    }

}
