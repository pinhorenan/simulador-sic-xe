package sicxesimulator.ui.components.buttons;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import sicxesimulator.ui.components.factories.ButtonFactory;
import sicxesimulator.ui.core.controller.Controller;
import sicxesimulator.ui.core.bindings.ButtonBinding;
import sicxesimulator.ui.core.view.Layout;

public class FileListButtons implements ButtonBinding {
    private final Layout mainLayout;

    private final Button linkButton;
    private final Button deleteButton;

    public FileListButtons(Controller controller, Layout mainLayout) {
        this.mainLayout = mainLayout;

        this.linkButton = ButtonFactory.createButton("Linkar", controller::handleLinkSelectedFilesAction);
        this.deleteButton = ButtonFactory.createButton("Deletar", controller::handleDeleteSelectedFilesAction);
    }

    public void setupBindings() {
        // Binding para 2 ou mais itens selecionados.
        BooleanBinding twoOrMoreSelected = Bindings.size(
                mainLayout.getObjectFilePanel().getObjectFileTable().getSelectionModel().getSelectedItems()
        ).greaterThanOrEqualTo(2);

        // Binding para pelo menos 1 item selecionado.
        BooleanBinding atLeastOneSelected = Bindings.size(
                mainLayout.getObjectFilePanel().getObjectFileTable().getSelectionModel().getSelectedItems()
        ).greaterThan(0);

        // "Deletar" deve ser habilitado se houver ao menos 1 item selecionado.
        deleteButton.disableProperty().bind(
                atLeastOneSelected.not()
        );

        // "Linkar" deve ser habilitado se houver 2 ou mais itens selecionados.
        linkButton.disableProperty().bind(
                twoOrMoreSelected.not()
        );
    }

    public Button getLinkButton() { return linkButton; }

    public Button getDeleteButton() { return deleteButton; }
}
