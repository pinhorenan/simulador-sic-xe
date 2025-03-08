package sicxesimulator.simulator.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.stage.FileChooser;
import sicxesimulator.logger.SimulatorLogger;
import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.simulator.model.MainModel;
import sicxesimulator.simulator.view.MainApp;
import sicxesimulator.simulator.view.MemoryEntry;
import sicxesimulator.simulator.view.RegisterEntry;
import sicxesimulator.simulator.view.SymbolEntry;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.ValueFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MainController {
    private final MainModel model;
    private final MainApp view;


    public MainController(MainModel model, MainApp view) {
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

            String formattedCode = model.getMostRecentObjectFile().toString();
            view.appendOutput("Programa montado e carregado com sucesso!");
            view.appendOutput(formattedCode);

            // Registra logs de montagem e código objeto
            SimulatorLogger.logAssemblyCode(sourceText);
            SimulatorLogger.logMachineCode(formattedCode);
        } catch (IllegalArgumentException | IOException e) {
            view.showError("Erro na montagem: " + e.getMessage());
            SimulatorLogger.logError("Erro na montagem", e);
        }
    }

    public void handleLoadObjectFileAction() {
        List<ObjectFile> objectFiles = model.getObjectFilesList();
        if (objectFiles.isEmpty()) {
            view.showError("Nenhum código foi montado ainda.");
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
            view.appendOutput("Arquivo montado carregado: " + selected.getFilename());
            view.enableControls();
            view.updateAllTables();
            handleUpdateExpandedCode();
        });
    }

    public void handleUpdateExpandedCode() {
        try {
            List<String> sourceLines = Arrays.asList(view.getInputField().getText().split("\\r?\\n"));
            List<String> expanded = model.expandMacros(sourceLines);

            // Atualiza o expandedArea com o conteúdo expandido
            view.getExpandedArea().setText(String.join("\n", expanded));
        } catch (IOException ex) {
            view.showError("Erro ao expandir macros: " + ex.getMessage());
        }
    }

    /// Grupo Inferior. ExecutionControls

    public void handleRunAction() {
        int pc = model.getMachine().getControlUnit().getIntValuePC();
        SimulatorLogger.logExecution("Início do ciclo de execução. PC inicial: " + String.format("%06X", pc));

        if (model.hasLoadedCode()) {
            if (!model.isFinished()) {
                Task<Void> runTask = new Task<>() {
                    @Override
                    protected Void call() {
                        while (!model.isFinished() && !model.isPaused()) {
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
                                Platform.runLater(() -> view.showError("Erro na execução: " + errorMsg));
                                break;
                            }
                            model.applyCycleDelay();
                        }
                        if (model.isFinished()) {
                            SimulatorLogger.logExecution("Execução concluída!");
                            Platform.runLater(() -> view.appendOutput("Execução concluída!"));
                        }
                        return null;
                    }
                };
                new Thread(runTask).start();
            } else {
                view.showError("Fim do programa!");
            }
        } else {
            view.showError("Nenhum programa montado!");
        }
    }

    public void handleNextAction() {
        if (model.hasLoadedCode()) {
            if (!model.isFinished()) {
                try {
                    model.runNextInstruction();
                    String log = model.getMachine().getControlUnit().getLastExecutionLog();
                    view.appendOutput(log);
                    view.updateAllTables();
                    SimulatorLogger.logExecution(log);
                } catch (Exception e) {
                    view.showError("Erro na execução: " + e.getMessage());

                    // Registra no log
                    SimulatorLogger.logError("Erro na execução", e);
                }
            } else {
                view.showError("Fim do programa!");
            }
        } else {
            view.showError("Nenhum programa montado!");
        }
    }

    public void handlePauseAction() {
        if (!model.hasLoadedCode()) {
            view.showError("Nenhum programa em execução para pausar!");
            return;
        }
        if (model.isPaused()) {
            view.appendOutput("Execução retomada!");
            SimulatorLogger.logExecution("Execução retomada.");
            model.unpause();
        } else {
            view.appendOutput("Execução pausada!");
            SimulatorLogger.logExecution("Execução pausada.");
            model.pause();
        }
    }

    public void handleResetAction() {
        view.getRegisterTable().getItems().clear();
        view.getMemoryTable().getItems().clear();
        view.getSymbolTable().getItems().clear();
        view.disableControls();
        model.reset();
        view.updateAllTables();
        view.getInputField().clear();
        view.getOutputArea().clear();
        view.getExpandedArea().clear();
        view.getStage().setTitle("Simulador SIC/XE");

        String resetMsg = "Simulação resetada. PC, registradores e memória reiniciados.";
        SimulatorLogger.logExecution(resetMsg);

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
        File file = fileChooser.showOpenDialog(view.getStage());

        if (file != null) {
            try {
                StringBuilder content = new StringBuilder();
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine()).append("\n");
                }
                // Aqui chamamos o método da View para atualizar o TextArea com o conteúdo do arquivo
                view.getInputField().setText(content.toString());
                scanner.close();
            } catch (FileNotFoundException e) {
                view.showError("Erro ao importar arquivo ASM: " + e.getMessage());
                SimulatorLogger.logError("Erro ao importar arquivo ASM", e);
            }
        }
    }

    public void handleExportASM() throws IOException {
        // Pega o código fonte do campo de entrada e processa os macros
        List<String> sourceLines = Arrays.asList(view.getInputField().getText().split("\\r?\\n"));
        List<String> expanded = model.expandMacros(sourceLines);

        // Abrir o FileChooser para salvar o arquivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Assembly Expandido", "*.asm"));
        File file = fileChooser.showSaveDialog(view.getStage());

        if (file != null) {
            // Concatena as linhas da lista de strings em uma única string
            String expandedCode = String.join("\n", expanded);

            // Tenta escrever o código expandido no arquivo
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(expandedCode);  // Escreve o código expandido no arquivo
                System.out.println("Arquivo .ASM Expandido exportado com sucesso!");
            } catch (IOException e) {
                view.showError("Erro ao tentar escrever o código expandido: " + e.getMessage());
                SimulatorLogger.logError("Erro ao tentar escrever o código expandido: ", e);
            }
        }
    }

    public void handleExportOBJ() {
        // Obtém o arquivo selecionado na ListView
        String selectedFileName = view.getObjectFileListView().getSelectionModel().getSelectedItem();

        if (selectedFileName == null) {
            // Exibe a mensagem "Sem arquivos montados" se não houver arquivos selecionados
            view.showNoFilesMessage();
            return;  // Se o usuário não selecionou nenhum arquivo, sai do método
        }

        // Obter o objeto `ObjectFile` correspondente ao nome selecionado
        ObjectFile selectedFile = model.getObjectFileByName(selectedFileName);

        // Obtém o conteúdo do código objeto
        byte[] objFileContent = selectedFile.getObjectCode();  // Pega o código objeto

        // Abrir o FileChooser para salvar o arquivo
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos .OBJ", "*.obj"));
        File file = fileChooser.showSaveDialog(view.getStage());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Escreve o conteúdo do código objeto no arquivo
                for (byte b : objFileContent) {
                    writer.write(String.format("%02X ", b));  // Escreve o byte como hex
                }
                System.out.println("Arquivo .OBJ exportado com sucesso!");
            } catch (IOException e) {
                view.showError("Erro ao expandir macros: " + e.getMessage());
                SimulatorLogger.logError("Erro ao expandir macros", e);
            }
        }
    }

    ///  2) menuBar Códigos Exemplo

    public void handleLoadSampleCodeAction(String sampleCode, String title) throws IOException {
        model.loadSampleCode(sampleCode, view, title);
        handleUpdateExpandedCode();
    }

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
            view.showError("Erro ao alterar o tamanho da memória: " + e.getMessage());
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
    } // Menu Exibir->Octal

    public void handleSetDecimalViewAction() {
        view.setViewFormat("DEC");
        view.updateViewFormatLabel();
    } // Menu Exibir->Decimal

    public void handleSetBinaryViewAction() {
        view.setViewFormat("BIN");
        view.updateViewFormatLabel();
    } // Menu Exibir->Binário

    ///  6) menuBar Ajuda

    public void handleHelpAction() {
        view.showHelpWindow();
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
        List<ObjectFile> objFiles = model.getObjectFilesList();
        return objFiles.stream()
                .map(ObjectFile::getFilename)
                .collect(Collectors.toList());
    }

    public void loadObjFilesToListView() {
        // Obter a lista de arquivos de objeto do model
        List<ObjectFile> objFiles = model.getObjectFilesList();

        // Limpar os itens existentes na ListView
        view.getObjectFileListView().getItems().clear();

        // Se não houver arquivos, exibe uma mensagem
        if (objFiles.isEmpty()) {
            view.showNoFilesMessage(); // Adiciona a mensagem de "sem arquivos"
        } else {
            // Adiciona os arquivos ao ListView (apenas os nomes dos arquivos)
            for (ObjectFile objFile : objFiles) {
                view.getObjectFileListView().getItems().add(objFile.getFilename());  // Adiciona o nome do arquivo
            }

            // Se você quiser selecionar o primeiro arquivo da lista automaticamente
            view.getObjectFileListView().getSelectionModel().selectFirst();  // Seleciona o primeiro arquivo
        }
    }

    public String getCycleDelay() {
        return null;
    }

    public List<ObjectFile> getObjectFilesList() {
        // TODO: Implementar
        return null;
    }

    public void reset() {
    }
}
