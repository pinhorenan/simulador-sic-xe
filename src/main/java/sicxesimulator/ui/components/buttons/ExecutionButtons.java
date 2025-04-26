package sicxesimulator.ui.components.buttons;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import sicxesimulator.ui.controller.Controller;
import sicxesimulator.ui.interfaces.ButtonBinding;
import sicxesimulator.ui.util.ButtonFactory;
import sicxesimulator.ui.view.Layout;

public class ExecutionButtons implements ButtonBinding {
    private final Layout mainLayout;
    private final Controller controller;

    private final Button nextButton;
    private final Button loadButton;
    private final Button restartButton;

    public ExecutionButtons(Controller controller, Layout mainLayout) {
        this.controller = controller;
        this.mainLayout = mainLayout;

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

        // O botão "Próximo" deve ser habilitado se a execução for permitida.
        nextButton.disableProperty().bind(executionAllowed.not());

        // O botão "Carregar" deve ser habilitado se EXATAMENTE 1 item estiver selecionado.
        loadButton.disableProperty().bind(
                oneSelected.not()
        );
    }

    public Button getNextButton() { return nextButton;}

    public Button getLoadButton() { return loadButton; }

    public Button getRestartButton() { return restartButton; }
}
