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
        try {
            // Executa um ciclo de instrução repetidamente (aqui, de forma simplificada)
            while (!model.isFinished()) {
                model.runNextInstruction();
                // Você pode inserir um delay ou lógica para parar após um número de ciclos
            }
            view.updateRegisterTable();
            view.updateMemoryTable();
            view.appendOutput("Execução concluída!");
        } catch (Exception e) {
            view.showError("Erro na execução: " + e.getMessage());
        }
    }

    public void handleNextAction() {
        model.runNextInstruction();
        view.updateRegisterTable();
        view.updateMemoryTable();
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


    public SimulationModel getSimulationModel() {
        return model;
    }

    public Assembler getAssembler() {
        return model.getAssembler();
    }
}
