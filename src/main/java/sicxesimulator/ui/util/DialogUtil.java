package sicxesimulator.ui.util;

import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.Optional;

/**
 * Classe utilitária para exibição de diálogos na interface JavaFX.
 *
 * <p>Oferece métodos estáticos para mostrar alertas de erro, informações, solicitações de entrada de
 * inteiros, strings e confirmações booleanas.</p>
 *
 * <p>Ideal para facilitar a comunicação com o usuário durante a simulação.</p>
 */
public abstract class DialogUtil {

    /**
     * Exibe um diálogo de informação com título, cabeçalho e mensagem.
     */
    public static void showInfoDialog(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Exibe um alerta de erro com a mensagem informada.
     */
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Ocorreu um erro");
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Solicita ao usuário um valor inteiro (decimal ou hexadecimal).
     *
     * @throws IOException Se o usuário cancelar ou inserir valor inválido.
     * @return Valor inteiro digitado.
     */
    public static int askForInteger(String title, String header, String content) throws IOException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        // Exibe o diálogo
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            throw new IOException("Usuario cancelou a entrada de inteiro.");
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

    /**
     * Mostra um diálogo de confirmação (Sim/Não) e retorna a escolha do usuário.
     *
     * @return true se o usuário escolher "Sim", false caso contrário.
     */
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

    /**
     * Solicita ao usuário uma entrada textual simples.
     *
     * @return String digitada ou null se cancelado.
     */
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
