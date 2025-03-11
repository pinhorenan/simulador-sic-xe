package sicxesimulator.application.components.buttons;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.view.MainLayout;

/**
 * Essa classe é responsável por criar e gerenciar os botões principais da interface gráfica.
 */
public class MainButtons {
    private final MainLayout mainLayout;
    private final Controller controller;

    private final Button runButton;
    private final Button pauseButton;
    private final Button nextButton;
    private final Button loadButton;
    private final Button restartButton;
    private final Button linkButton;
    private final Button deleteButton;
    private final Button assembleButton;
    private final Button clearOutputButton;

       /**
     * Construtor da classe. Recebe o controlador e a visão principal como parâmetros.
     * @param controller O controlador da aplicação
     * @param mainLayout O layout principal, para configuração de bindings
     */
    public MainButtons(Controller controller, MainLayout mainLayout) {
        this.mainLayout = mainLayout;
        this.controller = controller;

        this.runButton = ButtonFactory.createButton("Executar", controller::handleRunAction);
        this.pauseButton = ButtonFactory.createButton("Pausar", controller::handlePauseAction);
        this.nextButton = ButtonFactory.createButton("Próximo", controller::handleNextAction);
        this.loadButton = ButtonFactory.createButton("Carregar", controller::handleLoadObjectFileAction);
        this.restartButton = ButtonFactory.createButton("Reiniciar", controller::handleRestartAction);
        this.linkButton = ButtonFactory.createButton("Linkar", controller::handleLinkSelectedFilesAction);
        this.deleteButton = ButtonFactory.createButton("Deletar", controller::handleDeleteSelectedFilesAction);
        this.assembleButton = ButtonFactory.createButton("Montar", controller::handleAssembleAction);
        this.clearOutputButton = ButtonFactory.createButton("Limpar", controller::handleClearOutputAction);
    }

    /// ===== Métodos de configuração de bindings =====

    public void setupBindings() {

        // Binding para exatamente 1 item selecionado.
        BooleanBinding oneSelected = Bindings.size(
                mainLayout.getObjectFilePanel().getObjectFileTable().getSelectionModel().getSelectedItems()
        ).isEqualTo(1);

        // Binding para 2 ou mais itens selecionados.
        BooleanBinding twoOrMoreSelected = Bindings.size(
                mainLayout.getObjectFilePanel().getObjectFileTable().getSelectionModel().getSelectedItems()
        ).greaterThanOrEqualTo(2);

        // Binding para pelo menos 1 item selecionado.
        BooleanBinding atLeastOneSelected = Bindings.size(
                mainLayout.getObjectFilePanel().getObjectFileTable().getSelectionModel().getSelectedItems()
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

        // O botão "Montar" deve ser habilitado se houver código no campo de entrada.
        assembleButton.disableProperty().bind(
                mainLayout.getInputPanel().getInputArea().textProperty().isEmpty()
        );

        // Os botões de execução são habilitados se o código estiver carregado e a simulação não estiver finalizada.
        BooleanBinding executionAllowed = controller.getCodeLoadedProperty()
                .and(controller.getSimulationFinishedProperty().not());
        runButton.disableProperty().bind(executionAllowed.not());
        pauseButton.disableProperty().bind(executionAllowed.not());
        nextButton.disableProperty().bind(executionAllowed.not());
    }

    ///  ===== Métodos de acesso aos botões =====

    public Button getRunButton() { return runButton; }

    public Button getPauseButton() { return pauseButton; }

    public Button getNextButton() { return nextButton; }

    public Button getLoadButton() { return loadButton; }

    public Button getRestartButton() { return restartButton; }

    public Button getLinkButton() { return linkButton; }

    public Button getDeleteButton() { return deleteButton; }

    public Button getAssembleButton() { return assembleButton; }

    public Button getClearOutputButton() { return clearOutputButton; }
}