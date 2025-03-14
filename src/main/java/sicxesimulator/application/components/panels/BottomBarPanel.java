package sicxesimulator.application.components.panels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import sicxesimulator.application.controller.Controller;

public class BottomBarPanel {
    private final HBox pane;
    private final Controller controller;
    private final Label memoryLabel;
    private final Label formatLabel;
    private final Label speedLabel;

    public BottomBarPanel(Controller controller) {
        this.controller = controller;

        memoryLabel = new Label("Memória: ");
        formatLabel = new Label("Formato: ");
        speedLabel = new Label("Velocidade: ");

        pane = new HBox(20, memoryLabel, formatLabel, speedLabel);
        pane.setPadding(new Insets(10));
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.setStyle("-fx-background-color: #EEE; -fx-border-color: #CCC; -fx-padding: 5px;");
    }

    public HBox getPane() {
        return pane;
    }

    public void updateMemoryLabel() {
        int memorySize = controller.getMemorySize();
        memoryLabel.setText("Tamanho da memória: " + memorySize + " bytes");
    }

    public void updateFormatLabel() {
        String addressFormat = controller.getAddressFormat();
        formatLabel.setText("Formato de exibição de endereços: " + addressFormat);
    }

    public void updateSpeedLabel() {
        String cycleDelay = controller.getCycleDelay();
        speedLabel.setText("Atraso de Ciclo: " + cycleDelay);
    }
}
