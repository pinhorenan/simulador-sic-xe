package sicxesimulator.application.components.buttons;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.interfaces.ButtonBinding;
import sicxesimulator.application.util.ButtonFactory;
import sicxesimulator.application.view.Layout;

public class ExecutionButtons implements ButtonBinding {
    private final Layout mainLayout;
    private final Controller controller;

    private final Button runButton;
    private final Button pauseButton;
    private final Button nextButton;
    private final Button loadButton;
    private final Button restartButton;

    public ExecutionButtons(Controller controller, Layout mainLayout) {
        this.controller = controller;
        this.mainLayout = mainLayout;

        this.runButton = ButtonFactory.createButton("Executar", controller::handleRunAction);
        this.pauseButton = ButtonFactory.createButton("Pausar", controller::handlePauseAction);
        this.nextButton = ButtonFactory.createButton("Próximo", controller::handleNextAction);
        this.loadButton = ButtonFactory.createButton("Carregar", controller::handleLoadObjectFileAction);
        this.restartButton = ButtonFactory.createButton("Reiniciar", controller::handleRestartAction);
    }

    public void setupBindings() {
        // Binding para EXATAMENTE 1 item selecionado.
        BooleanBinding oneSelected = Bindings.size(
                mainLayout.getObjectFilePanel().getObjectFileTable().getSelectionModel().getSelectedItems()
        ).isEqualTo(1);

        // Binding para código CARREGADO && simulação NÂO FINALIZADA
        BooleanBinding executionAllowed = controller.getCodeLoadedProperty()
                .and(controller.getSimulationFinishedProperty().not());

        // Os botões: runButton, pauseButton, nextButton são habilitados quando o código foi carregado e a simulação não foi finalizada.
        runButton.disableProperty().bind(executionAllowed.not());
        pauseButton.disableProperty().bind(executionAllowed.not()); // TODO: Habilitar apenas quando a execução está "em andamento" (botão executar pressionado).
        nextButton.disableProperty().bind(executionAllowed.not());

        // O loadButton ("Carregar") deve ser habilitado se houver exatamente 1 item selecionado.
        loadButton.disableProperty().bind(
                oneSelected.not()
        );
    }

    public Button getRunButton() { return runButton; }

    public Button getPauseButton() { return pauseButton; }

    public Button getNextButton() { return nextButton;}

    public Button getLoadButton() { return loadButton; }

    public Button getRestartButton() { return restartButton; }
}
