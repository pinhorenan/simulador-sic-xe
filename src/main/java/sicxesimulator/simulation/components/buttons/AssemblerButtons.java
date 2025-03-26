package sicxesimulator.simulation.components.buttons;

import javafx.scene.control.Button;
import sicxesimulator.simulation.controller.Controller;
import sicxesimulator.simulation.interfaces.ButtonBinding;
import sicxesimulator.simulation.util.ButtonFactory;
import sicxesimulator.simulation.view.Layout;

public class AssemblerButtons implements ButtonBinding {
    private final Layout mainLayout;

    private final Button assembleButton;

    public AssemblerButtons(Controller controller, Layout mainLayout) {
        this.mainLayout = mainLayout;

        this.assembleButton = ButtonFactory.createButton("Montar", controller::handleAssembleAction);
    }

    public void setupBindings() {
        // O botão "Montar" deve ser habilitado se houver código no campo de entrada.
        assembleButton.disableProperty().bind(
                mainLayout.getInputPanel().getInputArea().textProperty().isEmpty()
        );
    }

    public Button getAssembleButton() {
        return assembleButton;
    }
}
