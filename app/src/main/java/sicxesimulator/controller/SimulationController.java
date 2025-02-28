package sicxesimulator.controller;

import sicxesimulator.model.SimulationModel;
import sicxesimulator.view.SimulationApp;

import java.io.IOException;
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
        model.reset();
        view.updateRegisterTable();
        view.updateMemoryTable();
        // Caso seja necessário, descarregar o arquivo objeto carregado.
    }

    public SimulationModel getSimulationModel() {
        return model;
    }
}
