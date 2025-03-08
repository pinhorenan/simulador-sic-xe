package sicxesimulator.simulator.view.components;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Painel que exibe duas áreas de texto lado a lado:
 * - A área da esquerda é para o código assembly de entrada.
 * - A área da direita exibe o código expandido, após processamento de macros.
 */
public class InputExpandedPane extends HBox {
    private final TextArea inputArea;
    private final TextArea expandedArea;

    public InputExpandedPane() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));

        // Área de entrada para o código assembly (à esquerda, ocupa metade do espaço)
        inputArea = new TextArea();
        inputArea.setPromptText("Insira seu código assembly aqui...");
        inputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");
        inputArea.setWrapText(false);


        // Área de saída para o código expandido (à direita, ocupa metade do espaço)
        expandedArea = new TextArea();
        expandedArea.setPromptText("Código expandido...");
        expandedArea.setEditable(false);
        expandedArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: #006400;");
        expandedArea.setWrapText(false);

        // Área de saída para o código expandido (em baixo, ocupa o espaço inteiro)

        // Adiciona ScrollPane para cada área, se necessário
        ScrollPane inputScroll = new ScrollPane(inputArea);
        inputScroll.setFitToWidth(true);
        ScrollPane expandedScroll = new ScrollPane(expandedArea);
        expandedScroll.setFitToWidth(true);

        // Define que ambas as áreas crescem igualmente
        HBox.setHgrow(inputScroll, Priority.ALWAYS);
        HBox.setHgrow(expandedScroll, Priority.ALWAYS);

        this.getChildren().addAll(inputScroll, expandedScroll);
    }

    public TextArea getInputArea() {
        return inputArea;
    }

    public TextArea getExpandedArea() {
        return expandedArea;
    }
}
