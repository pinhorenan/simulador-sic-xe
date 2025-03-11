package sicxesimulator.application.view.components;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.view.MainView;

/**
 * Essa classe é responsável por criar e gerenciar os botões principais da interface gráfica.
 */
public class SimulationToolbar extends HBox {
    private final MainView mainView;
    private final Controller controller;

    /// ===== Botões de controle da máquina =====
    protected Button runButton;
    protected Button pauseButton;
    protected Button nextButton;
    protected Button loadButton;
    protected Button restartMachineButton;

    /// ===== Botões de controle de arquivo =====
    protected Button linkButton;
    protected Button deleteButton;

    /// ===== Botões de controle de montagem =====
    protected Button assembleButton; /// Por enquanto usado individualmente

    /// ===== Botões de controle de reset =====
    @SuppressWarnings("unused")
    protected Button resetButton; /// Por enquanto usado individualmente

    /// ===== Botões de controle de saída =====
    protected Button clearOutputButton; /// Por enquanto usado individualmente

    /// ===== Containers de botões =====
    protected final HBox objectFileTableViewButtons; /// Botões de controle de arquivo (Linkar, Deletar)
    protected final HBox machineControlButtons; /// Botões de controle de execução (Executar, Pausar, Próximo, Carregar Programa, Reiniciar Máquina)

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
        this.objectFileTableViewButtons = createObjectFileTableViewButtons();
        this.machineControlButtons = createMachineControlButtons();
        this.assembleButton = createAssembleButton();
        this.clearOutputButton = createClearOutputButton();
    }

    /// ===== Métodos de criação de botões =====

    /**
     * Cria um botão compacto com o texto especificado.
     * @param text O texto do botão
     * @return O botão criado
     */
    private Button createCompactButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(80, 25); // Define um tamanho fixo compacto
        button.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // Evita que ele fique menor
        button.setMaxHeight(Double.NEGATIVE_INFINITY); // Impede crescimento vertical
        return button;
    }

    /**
     * Cria o HBox que contém os botões de controle de arquivo. (Linkar, Deletar)
     * @return HBox com os botões de controle de arquivo
     */
    private HBox createObjectFileTableViewButtons() {
        linkButton = createCompactButton("Linkar");
        linkButton.setOnAction(e -> controller.handleLinkSelectedFilesAction());

        deleteButton = createCompactButton("Deletar");
        deleteButton.setOnAction(e -> controller.handleDeleteSelectedFilesAction());

        HBox fileControls = new HBox(10, linkButton, deleteButton);
        fileControls.setAlignment(Pos.CENTER_RIGHT);
        return fileControls;
    }

    /**
     * Cria o HBox que contém os controles de execução. (Executar, Pausar, Próximo, Carregar)
     * É utilizado para criar o layout da interface gráfica exclusivamente dentro do construtor da classe.
     * @return HBox com os controles de execução
     */
    private HBox createMachineControlButtons() {
        runButton = new Button("Executar");
        runButton.setOnAction(e -> controller.handleRunAction());

        pauseButton = new Button("Pausar");
        pauseButton.setOnAction(e -> controller.handlePauseAction());

        nextButton = new Button("Próximo");
        nextButton.setOnAction(e -> controller.handleNextAction());

        loadButton = new Button("Carregar");
        loadButton.setOnAction(e -> controller.handleLoadObjectFileAction());

        restartMachineButton = new Button("Reiniciar Máquina");
        restartMachineButton.setOnAction(e -> controller.handleRestartAction());

        HBox machineControlButtonsHBox = new HBox(10, runButton, pauseButton, nextButton, loadButton, restartMachineButton);
        machineControlButtonsHBox.setAlignment(Pos.CENTER);
        return machineControlButtonsHBox;
    }

    public Button createClearOutputButton() {
        clearOutputButton = createCompactButton("Limpar Saída");
        clearOutputButton.setOnAction(e -> controller.handleClearOutputAction());
        return clearOutputButton;
    }

    public Button createAssembleButton() {
        assembleButton = createCompactButton("Montar");
        assembleButton.setOnAction(e -> controller.handleAssembleAction());
        return assembleButton;
    }


    /// ===== Métodos de configuração de bindings =====

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

        // O botão "Carregar" deve ser habilitado se houver exatamente 1 item selecionado.
        loadButton.disableProperty().bind(
                oneSelected.not()
        );

        // O botão "Linkar" deve ser habilitado se houver 2 ou mais itens selecionados.
        linkButton.disableProperty().bind(
                twoOrMoreSelected.not()
        );

        // Os botões de execução são habilitados se o código estiver carregado e a simulação não estiver finalizada.
        BooleanBinding executionAllowed = controller.getCodeLoadedProperty()
                .and(controller.getSimulationFinishedProperty().not());
        runButton.disableProperty().bind(executionAllowed.not());
        pauseButton.disableProperty().bind(executionAllowed.not());
        nextButton.disableProperty().bind(executionAllowed.not());
    }

    ///  ===== Métodos de acesso aos botões =====

    public HBox getMachineControlButtonsHBox() {
        return machineControlButtons;
    }

    public HBox getObjectFileTableViewButtonsHBox() {
        return objectFileTableViewButtons;
    }

    public Button getAssembleButton() {
        return assembleButton;
    }

    public Button getClearMachineOutputButton() {
        return clearOutputButton;
    }
}