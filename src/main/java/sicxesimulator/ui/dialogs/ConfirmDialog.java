package sicxesimulator.ui.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class ConfirmDialog {
    public static boolean ask(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        ButtonType btnYes = new ButtonType("Sim", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo  = new ButtonType("Não", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnYes, btnNo);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYes;
    }
}
