package sicxesimulator.ui.components.panels;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import sicxesimulator.ui.components.buttons.ExecutionButtons;
import sicxesimulator.ui.components.factories.TextAreaFactory;
import sicxesimulator.ui.components.factories.HBoxFactory;

public class ExecutionPanel {
    private final BorderPane pane = new BorderPane();
    private final TextArea machineOutput =
            TextAreaFactory.codeArea(false, "O trabalho duro supera o talento natural!", "green");
    private final HBox controls = HBoxFactory.createCenteredHBox(5, new Insets(10,0,0,0));

    public ExecutionPanel() {
        pane.setCenter(machineOutput);
        pane.setBottom(controls);
    }
    public void setButtons(ExecutionButtons b) {
        controls.getChildren().setAll(b.getLoadButton(), b.getRestartButton(), b.getNextButton());
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
