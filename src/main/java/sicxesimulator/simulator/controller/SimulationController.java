package sicxesimulator.simulator.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
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
            model.assembleCode(sourceLines);
            view.updateAllTables();
            String formattedCode = model.getLastObjectFile().toString();
            view.appendOutput("Programa montado e carregado com sucesso!");
            view.appendOutput(formattedCode);
        } catch (IllegalArgumentException e) {
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
        int pc = model.getMachine().getControlUnit().getIntValuePC();
        System.out.println("PC: " + pc);
        byte[] nextWord = model.getMachine().getMemory().readWord(pc / 3);
        System.out.println("Proxima instrucao: " + Arrays.toString(nextWord));

        if (model.hasAssembledCode()) {
            if (!model.isFinished()) {
                Task<Void> runTask = new Task<>() {
                    @Override
                    protected Void call() {
                        while (!model.isFinished() && !model.isPaused()) {
                            try {
                                model.runNextInstruction();
                            } catch (Exception ex) {
                                // Caso ocorra uma exceção, exiba detalhes (mesmo que a mensagem seja nula)
                                String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
                                Platform.runLater(() -> view.showError("Erro na execução: " + errorMsg));
                                break;
                            }
                            String log = model.getMachine().getControlUnit().getLastExecutionLog();
                            if (log == null) {
                                log = "Log de execução não disponível.";
                            }
                            String finalLog = log;
                            Platform.runLater(() -> {
                                view.appendOutput(finalLog);
                                view.updateAllTables();
                            });
                            model.applyCycleDelay();
                        }
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
        int pc = model.getMachine().getControlUnit().getIntValuePC();
        System.out.println("PC: " + pc);
        byte[] nextWord = model.getMachine().getMemory().readWord(pc / 3);
        System.out.println("Próxima instrução: " + Arrays.toString(nextWord));

        if (model.hasAssembledCode()) {
            if (!model.isFinished()) {
                try {
                    model.runNextInstruction();
                    String log = model.getMachine().getControlUnit().getLastExecutionLog();
                    if (log == null) {
                        log = "Log de execução não disponível.";
                    }
                    view.appendOutput(log);
                    view.updateAllTables();
                } catch (Exception e) {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
                    view.showError("Erro na execução: " + errorMsg);
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
        view.generateStateLog();
        view.getInputField().clear();
        view.getRegisterTable().getItems().clear();
        view.getMemoryTable().getItems().clear();
        view.getSymbolTable().getItems().clear();
        model.reset();
        view.updateAllTables();
        view.getOutputArea().clear();
        view.getStage().setTitle("Simulador SIC/XE");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reset");
        alert.setHeaderText("Simulação resetada");
        alert.setContentText("O estado da simulação foi completamente resetado.");
        alert.showAndWait();
    }

    public void handleLoadObjectFileAction(ObjectFile selectedFile) {
        if (selectedFile == null) {
            view.showError("Nenhum arquivo selecionado!");
            return;
        }

        model.loadObjectFile(selectedFile);
        view.enableControls();
        view.appendOutput("Arquivo montado carregado: " + selectedFile.getFilename());
        view.updateAllTables();
    }

    public void handleLoadSampleCodeAction(String sampleCode, String title) {
        model.loadSampleCode(sampleCode, view, title);
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
