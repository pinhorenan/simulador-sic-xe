package sicxesimulator.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.model.SimulationModel;
import sicxesimulator.view.SimulationApp;

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

            view.appendOutput("Programa montado e carregado com sucesso!");
        } catch (IOException | IllegalArgumentException e) {
            view.showError("Erro na montagem: " + e.getMessage());
        }
    }

    public void handleShowObjectCodeAction() {
        String inputText = view.getInputField().getText();
        if (!inputText.trim().isEmpty()) {
            List<String> sourceLines = Arrays.asList(inputText.split("\\r?\\n"));
            // Monta o código objeto
            byte[] objectCode = getAssembler().assemble(sourceLines);
            // Formata o código objeto
            String formattedCode = getAssembler().formatObjectCode(objectCode);
            // Exibe o código objeto no campo de saída
            view.getOutputArea().setText(formattedCode);
        }
    }

    public void handleRunAction() {
        if (model.hasAssembledCode()) {
            if (!model.isFinished()) {
                Task<Void> runTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        // Enquanto o programa não terminar ou estiver pausado
                        while (!model.isFinished() && !model.isPaused()) {
                            model.runNextInstruction();
                            // Captura o log da última instrução
                            final String log = model.getMachine().getControlUnit().getLastExecutionLog();
                            // Atualiza interface na thread JavaFX
                            Platform.runLater(() -> {
                                view.appendOutput(log);
                                view.updateRegisterTable();
                                view.updateMemoryTable();
                            });
                            // Pequeno delay para evitar consumo excessivo de CPU e dar chance à UI de atualizar
                            //noinspection BusyWait
                            Thread.sleep(50);
                        }
                        // Indica o fim da execução na UI
                        Platform.runLater(() -> view.appendOutput("Execução concluída!"));
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


    /**public void handleRunAction() {
        if (model.hasAssembledCode() && !model.finished()) {
            new Thread(() -> {
                while (!model.finished() && !model.isPaused()) {
                    Platform.runLater(() -> {
                        model.runNextInstruction();
                        view.updateAllTables();
                    });
                    try {
                        Thread.sleep(model.getMachine().getCycleSpeed());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }).start();
        }
    }
     */ // TODO: Esse método não travaria a UI.

    public void handleNextAction() {
        if(model.hasAssembledCode()) {
            if (!model.isFinished()) {
                try {
                    model.runNextInstruction();
                    String log = model.getMachine().getControlUnit().getLastExecutionLog();
                    view.appendOutput(log);
                    view.updateRegisterTable();
                    view.updateMemoryTable();
                } catch (Exception e) {
                    view.showError("Erro na execução: " + e.getMessage());
                }
            }
            else view.showError("Fim do programa!");
        } else view.showError("Nenhum programa montado!");

    }

    public void handlePauseAction() {
        if (model.isPaused()) {
            model.unpause();
        } else {
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

    public void handleChangeMemorySizeAction() {
        // Apresentar caixa de input para digitar APENAS INTEIROS.
        // Realizar checagem do valor recebido.

        // Ao selecionar aplicar a alteração na máquina.
        model.getMachine().changeMemorySize(1024); // TODO: O valor 1024 é o padrão e placeholder aqui.
    } // TODO

    public void handleChangeRunningSpeedAction() {
        // Apresentar as opções: Tempo real, rápido, médio, lento, muito lento (os delays relativos)

        // Ao selecionar aplicar a alteração na máquina.
        model.setCycleSpeed(0); } // TODO: O valor atual está como placeholder, os valores deverão ser 0, 1, 2, 3 ou 4.

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

    public void handleHelpAction() {} // TODO: Abrir janela mostrando funcionalidades suportadas, comandos e tutorial.

    public void handleError(String Message) {} // TODO

    ///  GETTERS

    public SimulationModel getSimulationModel() {
        return model;
    }

    public Assembler getAssembler() {
        return model.getAssembler();
    }

}
