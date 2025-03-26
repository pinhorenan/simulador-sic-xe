package sicxesimulator.application.components.buttons;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.interfaces.ButtonBinding;
import sicxesimulator.application.util.ButtonFactory;
import sicxesimulator.application.view.Layout;

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

        // O botão "Deletar" deve ser habilitado se houver ao menos 1 item selecionado.
        deleteButton.disableProperty().bind(
                atLeastOneSelected.not()
        );

        // O botão "Linkar" deve ser habilitado se houver 2 ou mais itens selecionados.
        linkButton.disableProperty().bind(
                twoOrMoreSelected.not()
        );
    }

    public Button getLinkButton() { return linkButton; }

    public Button getDeleteButton() { return deleteButton; }
}
