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

    public void handleAssembleAction(List<String> sourceLines) {
        try {
            model.assembleAndLoadProgram(sourceLines);
            view.updateAllTables();
            byte [] objectCode = model.getLastObjectCode();

            // Formata o código objeto
            String formattedCode = getAssembler().formatObjectCode(objectCode);
            view.appendOutput("Programa montado e carregado com sucesso!");
            view.appendOutput(formattedCode);

        } catch (IOException | IllegalArgumentException e) {
            view.showError("Erro na montagem: " + e.getMessage());
        }
    }

    public void handleShowObjectCodeAction() {
        if (model.hasAssembledCode()) {
            String formattedCode = model.getAssembler().formatObjectCode(model.getLastObjectCode());
            view.getOutputArea().setText(formattedCode);
        } else {
            String inputText = view.getInputField().getText();
            if (!inputText.trim().isEmpty()) {
                List<String> sourceLines = Arrays.asList(inputText.split("\\r?\\n"));
                try {
                    byte[] objectCode = model.getAssembler().assemble(sourceLines);
                    String formattedCode = model.getAssembler().formatObjectCode(objectCode);
                    view.getOutputArea().setText(formattedCode);
                } catch (Exception e) {
                    view.showError("Erro ao montar código: " + e.getMessage());
                }
            }
        }
    }

    public void handleRunAction() {
        if (model.hasAssembledCode()) {
            if (!model.isFinished()) {
                Task<Void> runTask = new Task<>() {
                    @Override
                    protected Void call() {
                        // Enquanto o programa não terminar ou estiver pausado
                        while (!model.isFinished() && !model.isPaused()) {
                            model.runNextInstruction();
                            String log = model.getMachine().getControlUnit().getLastExecutionLog();
                            Platform.runLater(() -> {
                                view.appendOutput(log);
                                view.updateAllTables();
                            });
                            model.applyCycleDelay();
                        }
                        // Indica o fim da execução na UI
                        if (model.isFinished()) Platform.runLater(() -> view.appendOutput("Execução concluída!"));
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
        if(model.hasAssembledCode()) {
            if (!model.isFinished()) {
                try {
                    model.runNextInstruction();
                    String log = model.getMachine().getControlUnit().getLastExecutionLog();
                    view.appendOutput(log);
                    view.updateAllTables();
                } catch (Exception e) {
                    view.showError("Erro na execução: " + e.getMessage());
                }
            }
            else view.showError("Fim do programa!");
        } else view.showError("Nenhum programa montado!");

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
        // Limpa o código assembly no campo de entrada
        view.getInputField().clear();

        // Limpa as tabelas
        view.getRegisterTable().getItems().clear();
        view.getMemoryTable().getItems().clear();
        view.getSymbolTable().getItems().clear();

        // Reseta o estado do modelo de simulação
        model.reset();

        // Atualiza as tabelas após o reset
        view.updateAllTables();

        // Limpa a área de saída
        view.getOutputArea().clear();

        // Se necessário, você pode definir o título da janela ou exibir uma mensagem
        view.getStage().setTitle("SIC/XE Simulator v2.1");

        // Exibe uma mensagem informando que a simulação foi resetada
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
            // Altera o tamanho da memória na máquina
            model.getMachine().changeMemorySize(newSize);
            view.appendOutput("Memória alterada para " + newSize + " bytes.");
            // Atualiza as tabelas se necessário
            view.updateMemoryTable();
        } catch (Exception e) {
            view.showError("Erro ao alterar o tamanho da memória: " + e.getMessage());
        }
    }

    public void handleChangeRunningSpeedAction(int speedValue) {
        model.setCycleSpeed(speedValue);
    }

    public void handleHexViewAction() {
        view.setViewFormatToHex();
        view.updateAllTables();
    }

    public void handleOctalViewAction() {
        view.setViewFormatToOctal();
        view.updateAllTables();
    }

    public void handleDecimalViewAction() {
        view.setViewFormatToDecimal();
        view.updateAllTables();
    }

    public void handleHelpAction() {
        view.showHelpWindow();
    }

    ///  GETTERS

    public SimulationModel getSimulationModel() {
        return model;
    }

    public Assembler getAssembler() {
        return model.getAssembler();
    }

}
