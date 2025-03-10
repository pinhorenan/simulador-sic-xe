package sicxesimulator.utils;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.Optional;

// Ajuste a importação de ObjectFile conforme seu projeto:


/**
 * DialogUtil centraliza a criação e configuração de diálogos customizados.
 * Todos os métodos são estáticos e a classe é final com construtor privado,
 * seguindo o padrão de classes utilitárias em Java.
 */
public final class DialogUtil {

    // Construtor privado para evitar instância
    private DialogUtil() {
        throw new UnsupportedOperationException("Esta classe não pode ser instanciada.");
    }


    /**
     * Exibe um alerta de erro com título, cabeçalho e mensagem.
     *
     * @param title   Título do alerta.
     * @param header  Cabeçalho do alerta.
     * @param message Mensagem de erro.
     */
    public static void showErrorDialog(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

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
     * Exibe um diálogo de escolha e retorna a opção selecionada.
     *
     * @param title   Título do diálogo.
     * @param header  Cabeçalho do diálogo.
     * @param content Texto explicativo para o usuário.
     * @param options Lista de opções disponíveis.
     * @return Um Optional contendo a opção escolhida.
     */
    public static Optional<String> showChoiceDialog(String title, String header, String content, List<String> options) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.getFirst(), options);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
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
     * Exibe um alerta de erro genérico com o conteúdo fornecido.
     *
     * @param message Mensagem de erro.
     */
    public static void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Simulação");
            alert.setHeaderText("Ocorreu um erro durante a execução");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Exibe um diálogo de entrada de texto e retorna o valor inserido.
     * @param title Título do diálogo
     * @param header Cabeçalho do diálogo
     * @param content Texto explicativo
     * @param defaultValue Valor padrão
     * @return Um Optional contendo o texto inserido ou vazio se o diálogo for cancelado.
     */
    public static Optional<String> showTextInputDialog(String title, String header, String content, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }

}
