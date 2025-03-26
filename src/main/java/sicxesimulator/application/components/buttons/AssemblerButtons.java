package sicxesimulator.application.components.buttons;

import javafx.scene.control.Button;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.interfaces.ButtonBinding;
import sicxesimulator.application.util.ButtonFactory;
import sicxesimulator.application.view.MainLayout;

public class AssemblerButtons implements ButtonBinding {
    private final MainLayout mainLayout;

    private final Button assembleButton;

    public AssemblerButtons(Controller controller, MainLayout mainLayout) {
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
