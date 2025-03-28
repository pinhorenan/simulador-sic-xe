package sicxesimulator.simulation.view;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import sicxesimulator.simulation.components.buttons.AssemblerButtons;
import sicxesimulator.simulation.components.buttons.ExecutionButtons;
import sicxesimulator.simulation.components.buttons.FileListButtons;
import sicxesimulator.simulation.controller.Controller;
import sicxesimulator.simulation.controller.MenuBarController;
import sicxesimulator.simulation.components.panels.*;

public class Layout {
    private final BorderPane root;
    private final MemoryPanel memoryPanel;
    private final RegisterPanel registerPanel;
    private final SymbolPanel symbolPanel;
    private final ExecutionPanel executionPanel;
    private final InputPanel inputPanel;
    private final HBox leftPane;

    private LabelsPanel labelsPanel;
    private FileListPanel objectFilePanel;

    private Controller mainController;
    private MenuBarController menuBarController;

    public Layout() {
        this.root = new BorderPane();

        this.inputPanel = new InputPanel();
        this.executionPanel = new ExecutionPanel();
        this.memoryPanel = new MemoryPanel();
        this.registerPanel = new RegisterPanel();
        this.symbolPanel = new SymbolPanel();

        HBox memoryAndRegisterTables = new HBox(10, memoryPanel.getPane(), registerPanel.getPane());
        VBox rightPane = new VBox(5, executionPanel.getPane(), memoryAndRegisterTables);
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
        this.mainController = mainController;
        this.objectFilePanel = new FileListPanel(mainController);

        VBox filesAndSymbols = new VBox(5, objectFilePanel.getPane(), symbolPanel.getPane());
        VBox.setVgrow(inputPanel.getPane(), Priority.ALWAYS);

        leftPane.getChildren().setAll(filesAndSymbols);
    }

    public void setMenuBarController(MenuBarController menuBarController) {
        this.menuBarController = menuBarController;
        updateToolbar();
    }

    private void updateToolbar() {
        if (menuBarController != null) {
            ToolbarPanel toolbarPanel = new ToolbarPanel(menuBarController);
            root.setTop(toolbarPanel.getMenuBar());
        }
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

    public void updateLabelsPanel() {
        if (labelsPanel == null) {
            labelsPanel = new LabelsPanel(mainController);
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
