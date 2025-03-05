package sicxesimulator.simulator.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import sicxesimulator.assembler.Assembler;
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

    /**
     * Lê o código do campo de entrada, monta e carrega o programa.
     */
    public void handleAssembleAction() {
        // Obtém o texto de entrada e divide em linhas
        String sourceText = view.getInputText();
        List<String> sourceLines = Arrays.asList(sourceText.split("\\r?\\n"));
        try {
            model.assembleAndLoadProgram(sourceLines);
            view.updateAllTables();

            // Formata o código objeto
            String formattedCode = model.getLastObjectFile().toString();
            view.appendOutput("Programa montado e carregado com sucesso!");
            view.appendOutput(formattedCode);
        } catch (IOException | IllegalArgumentException e) {
            view.showError("Erro na montagem: " + e.getMessage());
        }
    }

    public void handleShowObjectCodeAction() {
        if (model.getLastObjectFile() != null) {
            String formattedCode = model.getLastObjectFile().toString();
            view.appendOutput(formattedCode);
        } else {
            view.appendOutput("Nenhum programa montado!");
        }
    }

    public void handleRunAction() {
        if (model.hasAssembledCode()) {
            if (!model.isFinished()) {
                Task<Void> runTask = new Task<>() {
                    @Override
                    protected Void call() {
                        // Executa instruções enquanto o programa não terminar ou estiver pausado
                        while (!model.isFinished() && !model.isPaused()) {
                            model.runNextInstruction();
                            String log = model.getMachine().getControlUnit().getLastExecutionLog();
                            Platform.runLater(() -> {
                                view.appendOutput(log);
                                view.updateAllTables();
                            });
                            model.applyCycleDelay();
                        }
                        // Indica o fim da execução na interface
                        if (model.isFinished()) {
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
                } catch (Exception e) {
                    view.showError("Erro na execução: " + e.getMessage());
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
            model.unpause();
        } else {
            view.appendOutput("Execução pausada!");
            model.pause();
        }
    }

    public void handleResetAction() {
        // Limpa o campo de entrada e as tabelas
        view.getInputField().clear();
        view.getRegisterTable().getItems().clear();
        view.getMemoryTable().getItems().clear();
        view.getSymbolTable().getItems().clear();

        // Reseta o modelo e atualiza a interface
        model.reset();
        view.updateAllTables();
        view.getOutputArea().clear();
        view.getStage().setTitle("SIC/XE Simulator v2.1");

        // Exibe um alerta informativo sobre o reset
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset");
        alert.setHeaderText("Simulação resetada");
        alert.setContentText("O estado da simulação foi completamente resetado.");
        alert.showAndWait();
    }

    public void handleLoadSampleCodeAction() {
        model.loadSambleCode(view);
    }

    public void handleChangeMemorySizeAction(int newSize) {
        try {
            // Altera o tamanho da memória na máquina e atualiza a interface
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

    public void handleHelpAction() {
        view.showHelpWindow();
    }

    // GETTERS
    public SimulationModel getSimulationModel() {
        return model;
    }

    public Assembler getAssembler() {
        return model.getAssembler();
    }
}
