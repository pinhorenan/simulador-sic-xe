package sicxesimulator.application.util;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.Optional;

public abstract class DialogUtil {

    public static void showInfoDialog(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Ocorreu um erro");
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static int askForInteger(String title, String header, String content) throws IOException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        // Exibe o diálogo
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            throw new IOException("Usuário cancelou a entrada de inteiro.");
        }
        String input = result.get().trim();
        try {
            if (input.startsWith("0x") || input.startsWith("0X")) {
                return Integer.parseInt(input.substring(2), 16);
            } else {
                return Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            throw new IOException("Valor inválido para inteiro: " + input, e);
        }
    }

    public static boolean askForBoolean(String title, String content) {
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

    public static String askForString(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        // Exibe o diálogo
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}
