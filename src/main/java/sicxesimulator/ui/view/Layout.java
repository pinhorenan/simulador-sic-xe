package sicxesimulator.ui.view;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import sicxesimulator.ui.components.buttons.AssemblerButtons;
import sicxesimulator.ui.components.buttons.ExecutionButtons;
import sicxesimulator.ui.components.buttons.FileListButtons;
import sicxesimulator.ui.controller.Controller;
import sicxesimulator.ui.components.panels.*;

/**
 * Responsável por construir e organizar os painéis gráficos principais da interface.
 *
 * <p>Define a estrutura visual do simulador, conectando os componentes da interface
 * como painéis de memória, registradores, símbolos, entrada, execução, barra de ferramentas
 * e lista de arquivos.</p>
 *
 * <p>Fornece métodos para integração com controladores, botões e atualização de painéis.</p>
 */
public class Layout {
    private final BorderPane root;
    private final MemoryPanel memoryPanel;
    private final RegisterPanel registerPanel;
    private final SymbolPanel symbolPanel;
    private final ExecutionPanel executionPanel;
    private final InputPanel inputPanel;
    private final HBox leftPane;

    private StackPane executionStackPane;
    private VBox splashContainer;

    private LabelsPanel labelsPanel;
    private FileListPanel objectFilePanel;

    private Controller controller;
    public Layout() {
        this.root = new BorderPane();

        this.inputPanel = new InputPanel();
        this.executionPanel = new ExecutionPanel();
        this.memoryPanel = new MemoryPanel();
        this.registerPanel = new RegisterPanel();
        this.symbolPanel = new SymbolPanel();

        HBox memoryAndRegisterTables = new HBox(10, memoryPanel.getPane(), registerPanel.getPane());

        VBox.setVgrow(executionStackPane, Priority.ALWAYS);

        VBox rightPane = new VBox(5, executionStackPane, memoryAndRegisterTables);
        rightPane.setPrefWidth(400);
        VBox.setVgrow(executionPanel.getPane(), Priority.ALWAYS);

        VBox inputContainer = new VBox(inputPanel.getPane());
        HBox centerPane = new HBox(inputContainer);

        VBox filesAndSymbols = new VBox(symbolPanel.getPane());
        leftPane = new HBox(filesAndSymbols);

        HBox mainContent = new HBox(5, leftPane, centerPane, rightPane);
        mainContent.setPadding(new Insets(10));

        root.setCenter(mainContent);
        root.setPadding(new Insets(0));
    }

    public void setController(Controller mainController) {
        this.controller = mainController;
        this.objectFilePanel = new FileListPanel(mainController);

        VBox filesAndSymbols = new VBox(5, objectFilePanel.getPane(), symbolPanel.getPane());
        VBox.setVgrow(inputPanel.getPane(), Priority.ALWAYS);

        leftPane.getChildren().setAll(filesAndSymbols);
    }

    public void setButtons(FileListButtons fileListButtons, ExecutionButtons executionButtons, AssemblerButtons assemblerButtons) {
        updateToolbar();
        if (objectFilePanel != null) {
            this.objectFilePanel.setButtons(fileListButtons);
        }
        if (executionPanel != null) {
            this.executionPanel.setButtons(executionButtons);
        }
        if (inputPanel != null) {
            this.inputPanel.setButtons(assemblerButtons);
        }
    }

    public void updateToolbar() {
        if (controller != null) {
            ToolbarPanel toolbarPanel = new ToolbarPanel(controller);
            root.setTop(toolbarPanel.getMenuBar());
        }
    }

    public void updateLabelsPanel() {
        if (labelsPanel == null) {
            labelsPanel = new LabelsPanel(controller);
            root.setBottom(labelsPanel.getPane());
        }
    }

    public BorderPane getRoot() {
        return root;
    }

    public MemoryPanel getMemoryPanel() {
        return memoryPanel;
    }

    public RegisterPanel getRegisterPanel() {
        return registerPanel;
    }

    public SymbolPanel getSymbolPanel() {
        return symbolPanel;
    }

    public FileListPanel getObjectFilePanel() {
        return objectFilePanel;
    }

    public ExecutionPanel getExecutionPanel() {
        return executionPanel;
    }

    public InputPanel getInputPanel() {
        return inputPanel;
    }

    public LabelsPanel getLabelsPanel() {
        return labelsPanel;
    }
}