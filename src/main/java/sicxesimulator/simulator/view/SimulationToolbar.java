package sicxesimulator.simulator.view;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import sicxesimulator.simulator.controller.SimulationController;
import sicxesimulator.assembler.models.ObjectFile;
import java.util.List;

public class SimulationToolbar extends HBox {
    private final Button assembleButton;
    private final Button loadButton;
    private final Button runButton;
    private final Button nextButton;

    public SimulationToolbar(SimulationController controller) {
        this.setSpacing(10);

        this.assembleButton = createAssembleButton(controller);
        this.loadButton = createLoadButton(controller);
        Button showObjectCodeButton = createShowObjectCodeButton(controller);
        this.runButton = createRunButton(controller);
        Button pauseButton = createPauseButton(controller);
        this.nextButton = createNextButton(controller);
        Button resetButton = createResetButton(controller);

        this.getChildren().addAll(
                assembleButton,
                loadButton,
                showObjectCodeButton,
                runButton,
                pauseButton,
                nextButton,
                resetButton
        );
    }

    public void disableControls(boolean disable) {
        assembleButton.setDisable(disable);
        loadButton.setDisable(disable);
        runButton.setDisable(disable);
        nextButton.setDisable(disable);
    }

    private Button createAssembleButton(SimulationController controller) {
        Button button = new Button("Montar");
        button.setOnAction(e -> controller.handleAssembleAction());
        return button;
    }

    private Button createLoadButton(SimulationController controller) {
        Button button = new Button("Carregar");
        button.setOnAction(e -> showLoadDialog(controller));
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

    private void showLoadDialog(SimulationController controller) {
        List<ObjectFile> objectFiles = controller.getSimulationModel().getAssembler().getGeneratedObjectFiles();

        if (objectFiles.isEmpty()) {
            showAlert("Nenhum arquivo montado", "Nenhum código foi montado ainda.");
            return;
        }

        ChoiceDialog<ObjectFile> dialog = new ChoiceDialog<>(objectFiles.get(objectFiles.size() - 1), objectFiles);
        dialog.setTitle("Carregar Arquivo Montado");
        dialog.setHeaderText("Escolha um arquivo para carregar");
        dialog.setContentText("Arquivos disponíveis:");

        dialog.showAndWait().ifPresent(controller::handleLoadObjectFileAction);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
