package sicxesimulator.controller;

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
        if(!model.finished()) {
            try {
                // Executa um ciclo de instrução repetidamente (aqui, de forma simplificada)
                while (!model.finished() & !model.isPaused) {
                    model.runNextInstruction();

                    view.updateRegisterTable();
                    view.updateMemoryTable();
                    // TODO: Inserir um delay parametrizável para permitir a visualização da execução de cada instrução.
                }
                view.appendOutput("Execução concluída!");
            } catch (Exception e) {
                view.showError("Erro na execução: " + e.getMessage());
            }
        }
        else view.showError("Fim do programa!");
    }

    public void handleNextAction() {
        if (!model.finished()) {
            try {
                model.runNextInstruction();
                view.updateRegisterTable();
                view.updateMemoryTable();
            } catch (Exception e) {
                view.showError("Erro na execução: " + e.getMessage());
            }
        }
        else view.showError("Fim do programa!");
    }

    public void handlePauseAction() {
        if (model.isPaused) {
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
        // Código de exemplo
        String exampleCode =
                """
                        COPY START 1000
                        FIRST  LDA   FIVE
                               ADD   FOUR
                               STA   RESULT
                               RSUB
                        FIVE   WORD  5
                        FOUR   WORD  4
                        RESULT RESW  1""";

        // Coloca o código exemplo no campo de entrada
        view.getInputField().setText(exampleCode);

        // Atualiza o título da janela (opcional)
        view.getStage().setTitle("Simulador SIC/XE - Exemplo Carregado");

        // Exibe uma mensagem (opcional)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Código de Exemplo");
        alert.setHeaderText("Código Assembly de Exemplo Carregado");
        alert.setContentText("O código de exemplo foi carregado no campo de entrada.");
        alert.showAndWait();
        }

    public void handleImportAsmAction() {} // TODO

    public void handleChangeMemorySizeAction() {
        // Apresentar caixa de input para digitar APENAS INTEIROS.
        // Realizar checagem do valor recebido.

        // Ao selecionar aplicar a alteração na máquina.
        model.getMachine().changeMemorySize(1024); // TODO: O valor 1024 é o padrão e placeholder aqui.
    } // TODO

    public void handleChangeRunningSpeedAction() {
        // Apresentar as opções: Tempo real, rápido, médio, lento, muito lento (os delays relativos)

        // Ao selecionar aplicar a alteração na máquina.
        model.getMachine().setCycleSpeed(0); // TODO: O valor atual está como placeholder, os valores deverão ser 0, 1, 2, 3 ou 4.
    } // TODO

    public void handleHexViewAction() { view.setViewFormatToHex();} // TODO: Atualizar todas tabelas para que exibam valores em Hexadecimal.

    public void handleOctalViewAction() { view.setViewFormatToOctal();} // TODO: Atualizar todas tabelas para que exibam valores em Octal.

    public void handleDecimalViewAction() { view.setViewFormatToOctal();} // TODO: Atualizar todas tabelas para que exibam valores em Decimal.

    public void handleHelpAction() {} // TODO: Abrir janela mostrando funcionalidades suportadas, comandos e tutorial.

    ///  GETTERS

    public SimulationModel getSimulationModel() {
        return model;
    }

    public Assembler getAssembler() {
        return model.getAssembler();
    }

}
