package sicxesimulator.ui.dialogs;

import javafx.scene.control.Alert;

public class ErrorDialog {
    public static void show(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Ocorreu um erro");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
