package sicxesimulator.simulator.view;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

class InputOutputPane extends VBox {
    private final TextArea inputField;
    private final TextArea outputArea;

    public InputOutputPane() {
        this.setSpacing(10);
        this.setPadding(new Insets(0));

        // Criação e configuração do campo de entrada
        inputField = new TextArea();
        inputField.setPromptText("Insira seu código assembly aqui...");
        inputField.setWrapText(false);
        inputField.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");
        inputField.setMaxWidth(Double.MAX_VALUE);

        // Criação e configuração do campo de saída
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(false);
        outputArea.setStyle("-fx-font-family: monospace; -fx-text-fill: #006400; -fx-font-size: 14;");
        outputArea.setMaxWidth(Double.MAX_VALUE);

        // Cria ScrollPanes para permitir scroll e ajustar a largura
        ScrollPane inputScroll = new ScrollPane(inputField);
        inputScroll.setFitToWidth(true);
        ScrollPane outputScroll = new ScrollPane(outputArea);
        outputScroll.setFitToWidth(true);

        // Cria TitledPanes e define o tamanho máximo para preencher o espaço
        TitledPane inputPane = new TitledPane("Código Assembly", inputScroll);
        inputPane.setMaxWidth(Double.MAX_VALUE);
        TitledPane outputPane = new TitledPane("Saída", outputScroll);
        outputPane.setMaxWidth(Double.MAX_VALUE);

        // Adiciona os componentes à VBox
        this.getChildren().addAll(inputPane, outputPane);
    }

    public TextArea getInputField() {
        return inputField;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }
}
