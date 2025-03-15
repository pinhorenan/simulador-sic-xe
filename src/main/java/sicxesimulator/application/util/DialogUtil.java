package sicxesimulator.application.util;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.Optional;

/**
 * DialogUtil centraliza a criação e configuração de diálogos customizados.
 * Todos os métodos são estáticos e a classe é final com construtor privado,
 * seguindo o padrão de classes utilitárias em Java.
 */
public abstract class DialogUtil {

    /**
     * Exibe um alerta de informação com título, cabeçalho e mensagem.
     *
     * @param title   Título do alerta.
     * @param header  Cabeçalho do alerta.
     * @param message Mensagem de informação.
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
     * Exibe um alerta genérico com os parâmetros fornecidos.
     *
     * @param type    Tipo do alerta.
     * @param title   Título do alerta.
     * @param header  Cabeçalho do alerta.
     * @param content Conteúdo do alerta.
     */
    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Exibe uma caixa de diálogo informativa.
     *
     * @param message Mensagem a ser exibida na caixa de diálogo.
     */
    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Exibe uma caixa de diálogo de erro.
     *
     * @param message Mensagem do erro.
     */
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Ocorreu um erro");
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Exibe um diálogo solicitando ao usuário um valor inteiro.
     * Se o usuário clicar em Cancelar ou fechar o diálogo, lança IOException ou retorna -1 (conforme design).
     *
     * @param title  Título do diálogo
     * @param header Cabeçalho do diálogo
     * @param content Mensagem ou instrução ao usuário
     * @return O valor inteiro inserido pelo usuário
     * @throws IOException se o usuário cancelar ou inserir valor inválido
     */
    public static int askForInteger(String title, String header, String content) throws IOException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        // Configura para mostrar o diálogo
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            // Usuário cancelou
            throw new IOException("Usuário cancelou a entrada de inteiro.");
        }
        // Tenta parsear
        String input = result.get().trim();
        try {
            // Exemplo: permitir hexadecimal se começar com 0x?
            // A implementação a seguir é decimal. Ajuste conforme precisar de hex.
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IOException("Valor inválido para inteiro (hex): " + input, e);
        }
    }

    /**
     * Exibe um diálogo (sim/não) para perguntar algo booleano.
     * Se o usuário clicar em "OK", retorna true; se clicar em "Cancelar", retorna false.
     *
     * @param title   Título do diálogo
     * @param content Mensagem ou pergunta ao usuário
     * @return true se o usuário clicar OK, false se clicar Cancelar ou fechar o diálogo
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

}
