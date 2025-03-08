package sicxesimulator.simulator.view.components;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import sicxesimulator.simulator.controller.MainController;
import sicxesimulator.simulator.view.MainApp;

public class SimulationToolbar extends HBox {
    private final MainApp view;
    private final MainController controller;
    protected Button runButton;
    protected Button pauseButton;
    protected Button nextButton;
    protected Button assembleButton;
    protected Button loadButton;
    protected Button updateExpandedButton;
    protected Button resetButton;
    protected HBox fileControls;
    protected HBox executionControls;

    public SimulationToolbar(MainController controller, MainApp view) {
        this.view = view;
        this.controller = controller;
        this.setSpacing(10);

        // Inicializa os controles de arquivo e de execução
        this.fileControls = createFileControls();
        this.executionControls = createExecutionControls();

        // Desabilita os botões de execução inicialmente
        disableExecutionButtons();

        // Adiciona os HBox com os botões ao layout
        this.getChildren().addAll(
                fileControls,   // Controles de arquivo
                executionControls // Controles de execução
        );
    }


    public HBox createFileControls() {
        // Cria os botões
        Button montar = new Button("Montar");
        assembleButton = montar;
        montar.setOnAction(e -> controller.handleAssembleAction());

        Button carregar = new Button("Carregar");
        loadButton = carregar;
        carregar.setOnAction(e -> controller.handleLoadObjectFileAction());

        Button verExp = new Button("Atualizar/Expandir Código");
        updateExpandedButton = verExp;
        verExp.setOnAction(e -> controller.handleUpdateExpandedCode());

        // Cria o HBox com os botões
        HBox simulationControls = new HBox(10, montar, carregar, verExp);
        simulationControls.setAlignment(Pos.CENTER);
        return simulationControls;
    }


    public HBox createExecutionControls() {
        Button executar = new Button("Executar");
        runButton = executar;
        executar.setOnAction(e -> controller.handleRunAction());

        Button pausar = new Button("Pausar");
        pauseButton = pausar;
        pausar.setOnAction(e -> controller.handlePauseAction());

        Button proximo = new Button("Próximo");
        nextButton = proximo;
        proximo.setOnAction(e -> controller.handleNextAction());

        Button reset = new Button("Reset");
        resetButton = reset;
        reset.setOnAction(e -> controller.handleResetAction());

        HBox executionControls = new HBox(10, executar, pausar, proximo, reset);
        executionControls.setAlignment(Pos.CENTER);
        return executionControls;
    }

    /**
     * Habilita os botões de execução (Rodar, Pausar e Próximo).
     */
    public void enableExecutionButtons() {
        runButton.setDisable(false);
        pauseButton.setDisable(false);
        nextButton.setDisable(false);
        String style = "-fx-opacity: 1";
        runButton.setStyle(style);
        pauseButton.setStyle(style);
        nextButton.setStyle(style);
    }

    /**
     * Desabilita os botões de execução (Rodar, Pausar e Próximo).
     */
    public void disableExecutionButtons() {
        runButton.setDisable(true);
        pauseButton.setDisable(true);
        nextButton.setDisable(true);
        String style = "-fx-opacity: 0.5;";
        runButton.setStyle(style);
        pauseButton.setStyle(style);
        nextButton.setStyle(style);
    }

    public HBox getFileControls() {
        return fileControls;
    }

    public HBox getExecutionControls() {
        return executionControls;
    }

    public MainApp getView() {
        return view;
    }
}
