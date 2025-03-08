package sicxesimulator.simulator.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import sicxesimulator.logger.SimulatorLogger;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.simulator.model.SimulationModel;
import sicxesimulator.simulator.view.SimulationApp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SimulationController {
    private final SimulationModel model;
    private final SimulationApp view;

    public SimulationController(SimulationModel model, SimulationApp view) {
        this.model = model;
        this.view = view;
    }

    // Handlers
    public void handleAssembleAction() {
        String sourceText = view.getInputText();
        List<String> sourceLines = Arrays.asList(sourceText.split("\\r?\\n"));
        try {
            // Processa as macros e obtém o código expandido
            List<String> expandedSource = model.processMacros(sourceLines);

            // Opcional: exibir o código expandido na saída (ou em uma janela separada)
            view.clearOutput();
            view.clearExpandedCode();
            handleUpdateExpandedCode();

            // Agora passa o código expandido para o Assembler
            model.assembleCode(expandedSource);
            view.updateAllTables();

            String formattedCode = model.getLastObjectFile().toString();
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

    public void handleUpdateExpandedCode() {
        try {
            List<String> sourceLines = Arrays.asList(view.getInputField().getText().split("\\r?\\n"));
            List<String> expanded = model.processMacros(sourceLines);
            // Atualiza o expandedArea com o conteúdo expandido
            view.getExpandedArea().setText(String.join("\n", expanded));
        } catch (IOException ex) {
            view.showError("Erro ao expandir macros: " + ex.getMessage());
        }
    }

    public void handleRunAction() {
        int pc = model.getMachine().getControlUnit().getIntValuePC();
        SimulatorLogger.logExecution("Início do ciclo de execução. PC inicial: " + String.format("%06X", pc));

        if (model.hasAssembledCode()) {
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
        if (model.hasAssembledCode()) {
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
        if (!model.hasAssembledCode()) {
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

    public void handleLoadObjectFileAction() {
        List<ObjectFile> objectFiles = model.getAssembler().getGeneratedObjectFiles();
        if (objectFiles.isEmpty()) {
            view.showError("Nenhum código foi montado ainda.");
            return;
        }
        ChoiceDialog<ObjectFile> dialog = new ChoiceDialog<>(objectFiles.get(objectFiles.size()-1), objectFiles);
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


    public void handleLoadSampleCodeAction(String sampleCode, String title) throws IOException {
        model.loadSampleCode(sampleCode, view, title);
        handleUpdateExpandedCode();
    }

    public void handleChangeMemorySizeAction(int newSize) {
        try {
            model.getMachine().changeMemorySize(newSize);
            view.appendOutput("Memória alterada para " + newSize + " bytes.");
            view.updateMemoryTable();
        } catch (Exception e) {
            view.showError("Erro ao alterar o tamanho da memória: " + e.getMessage());
        }
    }

    public void handleChangeRunningSpeedAction(int speedValue) {
        model.setCycleSpeed(speedValue);
    }

    public void handleHexViewAction() {
        view.setViewFormat("HEX");
        view.updateViewFormatLabel("Hexadecimal");
    }

    public void handleOctalViewAction() {
        view.setViewFormat("OCT");
        view.updateViewFormatLabel("Octal");
    }

    public void handleDecimalViewAction() {
        view.setViewFormat("DEC");
        view.updateViewFormatLabel("Decimal");
    }

    public void handleBinaryViewAction() {
        view.setViewFormat("BIN");
        view.updateViewFormatLabel("Binário");
    }

    public void handleHelpAction() {
        view.showHelpWindow();
    }

    // Getters

    public SimulationModel getSimulationModel() {
        return model;
    }

    public Assembler getAssembler() {
        return model.getAssembler();
    }

}
