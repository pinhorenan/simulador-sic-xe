package sicxesimulator.application.components.panels;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;

public class OutputPanel {
    private final TitledPane outputPane;
    private final TextArea outputArea;

    public OutputPanel() {
        this.outputArea = new TextArea();
        outputArea.setPromptText("Saída de mensagens...");
        outputArea.setEditable(false);
        outputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: green;");
        outputArea.setMaxHeight(Double.MAX_VALUE);

        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(new Label("Saída da Máquina"));

        this.outputPane = new TitledPane();
        outputPane.setText(null);
        outputPane.setGraphic(headerPane);
        outputPane.setContent(outputArea);
        outputPane.setCollapsible(false);
        outputPane.setMaxHeight(Double.MAX_VALUE);
    }

    public TitledPane getPane() {
        return outputPane;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public void clearOutput() {
        outputArea.clear();
    }
}
