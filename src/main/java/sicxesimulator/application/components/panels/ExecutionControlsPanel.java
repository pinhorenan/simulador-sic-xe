package sicxesimulator.application.components.panels;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import sicxesimulator.application.components.buttons.MainButtons;

public class ExecutionControlsPanel {
    private final HBox pane;

    public ExecutionControlsPanel() {
        // Criamos um painel vazio inicialmente, os botões serão adicionados depois
        pane = new HBox(5);
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(50);
    }

    public void setMainButtons(MainButtons mainButtons) {
        // Agora usamos diretamente os botões de MainButtons para manter os bindings
        Button runButton = mainButtons.getRunButton();
        Button pauseButton = mainButtons.getPauseButton();
        Button nextButton = mainButtons.getNextButton();
        Button loadButton = mainButtons.getLoadButton();
        Button restartButton = mainButtons.getRestartButton();

        // Atualizamos o painel com os botões reais, mantendo os bindings
        pane.getChildren().setAll(runButton, pauseButton, nextButton, loadButton, restartButton);
    }

    public HBox getPane() {
        return pane;
    }
}
