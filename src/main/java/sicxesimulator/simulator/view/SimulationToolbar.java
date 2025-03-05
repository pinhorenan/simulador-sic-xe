package sicxesimulator.simulator.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import sicxesimulator.simulator.controller.SimulationController;

public class SimulationToolbar extends HBox {
    private final Button assembleButton;
    private final Button runButton;
    private final Button nextButton;

    public SimulationToolbar(SimulationController controller) {
        this.setSpacing(10);
        this.setPadding(new Insets(0));

        // Atribuição aos botões de instância para possibilitar manipulação posterior
        this.assembleButton = createAssembleButton(controller);
        Button showObjectCodeButton = createShowObjectCodeButton(controller);
        this.runButton = createRunButton(controller);
        Button pauseButton = createPauseButton(controller);
        this.nextButton = createNextButton(controller);
        Button resetButton = createResetButton(controller);


        // Adiciona todos os botões à toolbar
        this.getChildren().addAll(
                assembleButton,
                showObjectCodeButton,
                runButton,
                pauseButton,
                nextButton,
                resetButton
        );
    }

    /**
     * Desabilita ou habilita os controles essenciais.
     * Neste exemplo, apenas os botões de montar, executar e próximo são controlados.
     *
     * @param disable true para desabilitar, false para habilitar.
     */
    public void disableControls(boolean disable) {
        assembleButton.setDisable(disable);
        runButton.setDisable(disable);
        nextButton.setDisable(disable);
    }

    private Button createAssembleButton(SimulationController controller) {
        Button button = new Button("Montar");
        button.setOnAction(e -> controller.handleAssembleAction());
        return button;
    }

    private Button createShowObjectCodeButton(SimulationController controller) {
        Button button = new Button("Mostrar Código Objeto");
        button.setOnAction(e -> controller.handleShowObjectCodeAction());
        return button;
    }

    private Button createRunButton(SimulationController controller) {
        Button button = new Button("Executar");
        button.setOnAction(e -> controller.handleRunAction());
        return button;
    }

    private Button createPauseButton(SimulationController controller) {
        Button button = new Button("Pausar");
        button.setOnAction(e -> controller.handlePauseAction());
        return button;
    }

    private Button createNextButton(SimulationController controller) {
        Button button = new Button("Próximo");
        button.setOnAction(e -> controller.handleNextAction());
        return button;
    }

    private Button createResetButton(SimulationController controller) {
        Button button = new Button("Resetar");
        button.setOnAction(e -> controller.handleResetAction());
        return button;
    }
}
