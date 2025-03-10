package sicxesimulator.simulator.view.components;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import sicxesimulator.simulator.controller.Controller;
import sicxesimulator.simulator.view.MainView;

/**
 * Essa classe é responsável por criar e gerenciar os botões principais da interface gráfica.
 */
public class SimulationToolbar extends HBox {
    private final MainView mainView;
    private final Controller controller;

    // Botões
    protected Button runButton;
    protected Button pauseButton;
    protected Button nextButton;
    protected Button assembleButton;
    protected Button loadButton;
    protected Button linkButton;
    protected Button resetButton;
    protected Button deleteButton;
    protected final VBox fileControls;
    protected final HBox executionControls;
    protected final HBox resetControl;


    /**
     * Construtor da classe. Recebe o controlador e a visão principal como parâmetros.
     * @param controller O controlador da aplicação
     * @param mainView A visão principal da aplicação
     */
    public SimulationToolbar(Controller controller, MainView mainView) {
        this.mainView = mainView;
        this.controller = controller;
        this.setSpacing(10);

        // Inicializa todos os controles
        this.fileControls = createFileControls();
        this.executionControls = createExecutionControls();
        this.resetControl = createResetButton();  // agora sim chamado corretamente

        // TODO: Temporário
        assembleButton = new Button("Montar");
        assembleButton.setOnAction(e -> controller.handleAssembleAction());


        // Adiciona os controles ao layout
        this.getChildren().addAll(fileControls, executionControls, resetControl);
    }

    /**
     * Cria o VBox que contém os controles de arquivo. (Montar, Linkar, Deletar)
     * É utilizado para criar o layout da interface gráfica exclusivamente dentro do construtor da classe.
     * @return VBox com os controles de arquivo
     */
    private VBox createFileControls() {

        linkButton = new Button("Linkar");
        linkButton.setOnAction(e -> controller.handleLinkSelectedFilesAction());

        deleteButton = new Button("Deletar");
        deleteButton.setOnAction(e -> controller.handleDeleteSelectedFilesAction());

        VBox fileControls = new VBox(10, linkButton, deleteButton);
        fileControls.setAlignment(Pos.CENTER);
        return fileControls;
    }

    /**
     * Cria o HBox que contém os controles de execução. (Executar, Pausar, Próximo, Carregar)
     * É utilizado para criar o layout da interface gráfica exclusivamente dentro do construtor da classe.
     * @return HBox com os controles de execução
     */
    private HBox createExecutionControls() {
        runButton = new Button("Executar");
        runButton.setOnAction(e -> controller.handleRunAction());

        pauseButton = new Button("Pausar");
        pauseButton.setOnAction(e -> controller.handlePauseAction());

        nextButton = new Button("Próximo");
        nextButton.setOnAction(e -> controller.handleNextAction());

        loadButton = new Button("Carregar");
        loadButton.setOnAction(e -> controller.handleLoadObjectFileAction());

        HBox executionControls = new HBox(10, runButton, pauseButton, nextButton, loadButton);
        executionControls.setAlignment(Pos.CENTER);
        return executionControls;
    }

    /**
     * Cria o HBox que contém o botão de reset.
     * É utilizado para criar o layout da interface gráfica exclusivamente dentro do construtor da classe.
     * @return HBox com o botão de reset
     */
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

    /**
     * Configura as propriedades de desabilitação dos botões.
     * É chamada pelo MainView após a inicialização dos controles.
     */
    public void setupBindings() {
        // Desabilita o botão "Montar" se o campo de entrada estiver vazio.
        assembleButton.disableProperty().bind(
                mainView.getInputField().textProperty().isEmpty()
        );

        // Binding para exatamente 1 item selecionado.
        BooleanBinding oneSelected = Bindings.size(
                mainView.getObjectFileTableView().getSelectionModel().getSelectedItems()
        ).isEqualTo(1);


        // Binding para 2 ou mais itens selecionados.
        BooleanBinding twoOrMoreSelected = Bindings.size(
                mainView.getObjectFileTableView().getSelectionModel().getSelectedItems()
        ).greaterThanOrEqualTo(2);


        // Binding para pelo menos 1 item selecionado.
        BooleanBinding atLeastOneSelected = Bindings.size(
                mainView.getObjectFileTableView().getSelectionModel().getSelectedItems()
        ).greaterThan(0);

        // O botão "Deletar" deve ser habilitado se houver ao menos 1 item selecionado.
        deleteButton.disableProperty().bind(
                atLeastOneSelected.not()
        );

        // Os botões de execução são habilitados se o código estiver carregado e a simulação não estiver finalizada.
        BooleanBinding executionAllowed = controller.getCodeLoadedProperty()
                .and(controller.getSimulationFinishedProperty().not());
        runButton.disableProperty().bind(executionAllowed.not());
        pauseButton.disableProperty().bind(executionAllowed.not());
        nextButton.disableProperty().bind(executionAllowed.not());
    }

    /**
     * Retorna o VBox que contém os controles de arquivo. (Montar, Linkar, Deletar)
     * @return VBox com os controles de arquivo
     */
    public VBox getFileControls() {
        return fileControls;
    }

    /**
     * Retorna o HBox que contém os controles de execução. (Executar, Pausar, Próximo, Carregar)
     * @return HBox com os controles de execução
     */
    public HBox getExecutionControls() {
        return executionControls;
    }

    /**
     * Retorna o HBox que contém o botão de reset.
     * @return HBox com o botão de reset
     */
    public HBox getResetControl() {
        return resetControl;
    }

    public Button getAssembleButton() {
        return assembleButton;
    }

    public Button getLinkButton() {
        return linkButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }
}