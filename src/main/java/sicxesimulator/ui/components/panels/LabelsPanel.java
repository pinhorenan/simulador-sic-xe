package sicxesimulator.ui.components.panels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import sicxesimulator.ui.controller.Controller;

public class LabelsPanel {
    private final HBox pane;
    private final Controller controller;
    private final Label memorySizeLabel;
    private final Label linkerModeLabel;

    public LabelsPanel(Controller controller) {
        this.controller = controller;

        memorySizeLabel = new Label("Tamanho da Memória: ");
        linkerModeLabel = new Label("Modo Ligador: ");

        pane = new HBox(20, memorySizeLabel, linkerModeLabel);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.setStyle("-fx-background-color: #EEE; -fx-border-color: #CCC; -fx-padding: 5px;");
    }

    public HBox getPane() {
        return pane;
    }

    public void updateLinkerModeLabel() {
        var mode = controller.getModel().getLinkerMode();
        switch (mode) {
            case ABSOLUTO -> linkerModeLabel.setText("Modo Ligador: ABSOLUTO");
            case RELOCAVEL -> linkerModeLabel.setText("Modo Ligador: RELOCÁVEL");
        }
    }

    public void updateMemorySizeLabel() {
        int memorySize = controller.getMemorySize();
        memorySizeLabel.setText("Tamanho da memória: " + memorySize + " bytes");
    }
}
