package sicxesimulator.ui.components.panels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import sicxesimulator.ui.components.buttons.ExecutionButtons;

public class ExecutionPanel {
    private final BorderPane pane;
    private final TextArea machineOutput;
    private final HBox controlsPane;

    public ExecutionPanel() {
        pane = new BorderPane();

        // Área de saída da máquina
        machineOutput = new TextArea();
        machineOutput.setPromptText("O trabalho duro supera o talento natural!");
        machineOutput.setEditable(false);
        machineOutput.setWrapText(true);
        machineOutput.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: green;");
        pane.setCenter(machineOutput);

        // Painel inferior para os botões de controle
        controlsPane = new HBox(5);
        controlsPane.setPadding(new Insets(10, 0, 0, 0));
        controlsPane.setAlignment(Pos.CENTER);
        pane.setBottom(controlsPane);
    }

    /**
     * Configura os botões de execução
     * Utiliza os botões definidos na classe MainButtons
     * @param executionButtons Instância de ExecutionButtons
     */
    public void setButtons(ExecutionButtons executionButtons) {
        Button nextButton = executionButtons.getNextButton();
        Button loadButton = executionButtons.getLoadButton();
        Button restartButton = executionButtons.getRestartButton();
        controlsPane.getChildren().setAll(loadButton, restartButton, nextButton);
    }

    public BorderPane getPane() {
        return pane;
    }

    public TextArea getMachineOutput() {
        return machineOutput;
    }

    public void clearOutput() {
        machineOutput.clear();
    }
}
