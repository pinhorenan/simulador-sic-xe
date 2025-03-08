package sicxesimulator.simulator.view;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.*;
import sicxesimulator.simulator.controller.SimulationController;
import sicxesimulator.assembler.models.ObjectFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SimulationToolbar extends HBox {
    private final SimulationApp view;
    private final Button assembleButton;
    private final Button loadButton;
    private final Button showExpandedButton;
    private final Button runButton;
    private final Button pauseButton;
    private final Button nextButton;

    public SimulationToolbar(SimulationController controller, SimulationApp view) {
        this.view = view;
        this.setSpacing(10);

        this.assembleButton = createAssembleButton(controller);
        this.loadButton = createLoadButton(controller);
        // Novo botão para ver o código expandido após macroexpansão
        this.showExpandedButton = createShowExpandedCodeButton(controller);
        this.runButton = createRunButton(controller);
        this.pauseButton = createPauseButton(controller);
        this.nextButton = createNextButton(controller);
        Button resetButton = createResetButton(controller);

        this.getChildren().addAll(
                assembleButton,
                loadButton,
                showExpandedButton,
                runButton,
                pauseButton,
                nextButton,
                resetButton
        );
    }

    /**
     * Habilita os botões de execução (Rodar, Pausar e Próximo).
     */
    public void enableExecutionButtons() {
        runButton.setDisable(false);
        pauseButton.setDisable(false);
        nextButton.setDisable(false);
        String style = "-fx-opacity: 1";
        runButton.setStyle(style);
        pauseButton.setStyle(style);
        nextButton.setStyle(style);
    }

    /**
     * Desabilita os botões de execução (Rodar, Pausar e Próximo).
     */
    public void disableExecutionButtons() {
        runButton.setDisable(true);
        pauseButton.setDisable(true);
        nextButton.setDisable(true);
        String style = "-fx-opacity: 0.5;";
        runButton.setStyle(style);
        pauseButton.setStyle(style);
        nextButton.setStyle(style);
    }

    private Button createAssembleButton(SimulationController controller) {
        Button button = new Button("Montar");
        button.setOnAction(e -> controller.handleAssembleAction());
        return button;
    }

    private Button createLoadButton(SimulationController controller) {
        Button button = new Button("Carregar");
        button.setOnAction(e -> showLoadDialog(controller));
        return button;
    }

    private Button createShowExpandedCodeButton(SimulationController controller) {
        Button button = new Button("Ver Código Expandido");
        button.setOnAction(e -> {
            String sourceText = view.getInputField().getText();
            List<String> sourceLines = List.of(sourceText.split("\\r?\\n"));
            try {
                // Processa as macros e obtém o código expandido
                List<String> expanded = controller.getSimulationModel().processMacros(sourceLines);
                // Exibe o código expandido em um diálogo
                TextArea textArea = new TextArea(String.join("\n", expanded));
                textArea.setEditable(false);
                textArea.setWrapText(false);
                ScrollPane scrollPane = new ScrollPane(textArea);
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefWidth(600);
                scrollPane.setPrefHeight(400);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Código Expandido");
                alert.setHeaderText("Veja o código após a expansão de macros:");
                alert.getDialogPane().setContent(scrollPane);
                alert.showAndWait();
            } catch (IOException ex) {
                showAlert("Erro", "Erro ao expandir macros: " + ex.getMessage());
            }
        });
        return button;
    }

    private Button createRunButton(SimulationController controller) {
        Button button = new Button("Executar");
        button.setOnAction(e -> controller.handleRunAction());
        return button;
    }

    private Button createPauseButton(SimulationController controller) {
        Button button = new Button("Pausar");
        button.setOnAction(e -> controller.handlePauseAction());
        return button;
    }

    private Button createNextButton(SimulationController controller) {
        Button button = new Button("Próximo");
        button.setOnAction(e -> controller.handleNextAction());
        return button;
    }

    private Button createResetButton(SimulationController controller) {
        Button button = new Button("Resetar");
        button.setOnAction(e -> controller.handleResetAction());
        return button;
    }

    /**
     * Exibe um diálogo para carregar um arquivo objeto.
     */
    private void showLoadDialog(SimulationController controller) {
        List<ObjectFile> objectFiles = controller.getSimulationModel().getAssembler().getGeneratedObjectFiles();

        if (objectFiles.isEmpty()) {
            showAlert("Nenhum arquivo montado", "Nenhum código foi montado ainda.");
            return;
        }

        // Cria a lista de wrappers usando o record ObjectFileOption
        List<ObjectFileOption> options = objectFiles.stream()
                .map(ObjectFileOption::new)
                .toList();

        // Define o item padrão (por exemplo, o último da lista)
        ObjectFileOption defaultOption = options.get(options.size() - 1);

        ChoiceDialog<ObjectFileOption> dialog = new ChoiceDialog<>(defaultOption, options);
        dialog.setTitle("Carregar Arquivo Montado");
        dialog.setHeaderText("Escolha um arquivo para carregar");
        dialog.setContentText("Arquivos disponíveis:");

        dialog.showAndWait().ifPresent(selected ->
                controller.handleLoadObjectFileAction()
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Record para opções de arquivo objeto.
     */
    private record ObjectFileOption(ObjectFile objectFile) {
        @Override
        public String toString() {
            return objectFile.getFilename();
        }
    }
}
