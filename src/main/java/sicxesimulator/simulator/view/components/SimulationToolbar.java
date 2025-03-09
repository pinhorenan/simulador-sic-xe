package sicxesimulator.simulator.view.components;

import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import sicxesimulator.simulator.controller.Controller;
import sicxesimulator.simulator.view.MainView;

public class SimulationToolbar extends HBox {
    private final MainView mainView;
    private final Controller controller;

    protected Button runButton;
    protected Button pauseButton;
    protected Button nextButton;
    protected Button assembleButton;
    protected Button loadButton;
    protected Button updateExpandedButton;
    protected Button resetButton;

    protected HBox fileControls;
    protected HBox executionControls;
    protected HBox resetControl;

    public SimulationToolbar(Controller controller, MainView mainView) {
        this.mainView = mainView;
        this.controller = controller;
        this.setSpacing(10);

        // Inicializa todos os controles
        this.fileControls = createFileControls();
        this.executionControls = createExecutionControls();
        this.resetControl = createResetButton();  // agora sim chamado corretamente


        // Adiciona os controles ao layout
        this.getChildren().addAll(fileControls, executionControls, resetControl);
    }

    private HBox createFileControls() {
        assembleButton = new Button("Montar");
        assembleButton.setOnAction(e -> controller.handleAssembleAction());

        loadButton = new Button("Carregar");
        loadButton.setOnAction(e -> controller.handleLoadObjectFileAction());

        updateExpandedButton = new Button("Atualizar/Expandir Código");
        updateExpandedButton.setOnAction(e -> controller.handleUpdateExpandedCode());

        HBox fileControls = new HBox(10, assembleButton, loadButton, updateExpandedButton);
        fileControls.setAlignment(Pos.CENTER);
        return fileControls;
    }

    private HBox createExecutionControls() {
        runButton = new Button("Executar");
        runButton.setOnAction(e -> controller.handleRunAction());

        pauseButton = new Button("Pausar");
        pauseButton.setOnAction(e -> controller.handlePauseAction());

        nextButton = new Button("Próximo");
        nextButton.setOnAction(e -> controller.handleNextAction());

        HBox executionControls = new HBox(10, runButton, pauseButton, nextButton);
        executionControls.setAlignment(Pos.CENTER);
        return executionControls;
    }

    private HBox createResetButton() {
        resetButton = new Button("Reset");
        resetButton.setMaxWidth(Double.MAX_VALUE); // Faz o botão expandir horizontalmente

        resetButton.setOnAction(e -> controller.handleResetAction());

        HBox resetBox = new HBox(resetButton);
        resetBox.setAlignment(Pos.CENTER);
        resetBox.setFillHeight(true);
        HBox.setHgrow(resetButton, Priority.ALWAYS); // O botão ocupa o espaço disponível

        return resetBox;
    }

    public void setupBindings() {
        assembleButton.disableProperty().bind(
                mainView.getInputField().textProperty().isEmpty()
        );

        loadButton.disableProperty().bind(
                controller.getCodeAssembledProperty().not()
        );

        updateExpandedButton.disableProperty().bind(
                controller.getCodeAssembledProperty().not()
        );

        BooleanBinding executionAllowed = controller.getCodeLoadedProperty()
                .and(controller.getSimulationFinishedProperty().not());

        runButton.disableProperty().bind(executionAllowed.not());
        pauseButton.disableProperty().bind(executionAllowed.not());
        nextButton.disableProperty().bind(executionAllowed.not());
    }

    public HBox getFileControls() {
        return fileControls;
    }

    public HBox getExecutionControls() {
        return executionControls;
    }

    public HBox getResetControl() {
        return resetControl;
    }

    public MainView getView() {
        return mainView;
    }
}
