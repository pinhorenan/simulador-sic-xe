package sicxesimulator.ui.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

public class InfoDialog {
    public static void show(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
