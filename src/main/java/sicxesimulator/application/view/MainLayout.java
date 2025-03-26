package sicxesimulator.application.view;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import sicxesimulator.application.components.buttons.AssemblerButtons;
import sicxesimulator.application.components.buttons.ExecutionButtons;
import sicxesimulator.application.components.buttons.FileListButtons;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.controller.MenuBarController;
import sicxesimulator.application.components.panels.*;

public class MainLayout {
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

    public MainLayout() {
        this.root = new BorderPane();

        // Criamos os painéis que não dependem do Controller
        this.inputPanel = new InputPanel();
        this.executionPanel = new ExecutionPanel();
        this.memoryPanel = new MemoryPanel();
        this.registerPanel = new RegisterPanel();
        this.symbolPanel = new SymbolPanel();

        // Criamos o container rightPane (que não depende do ObjectFilePanel)
        HBox memoryAndRegisterTables = new HBox(10, memoryPanel.getPane(), registerPanel.getPane());
        VBox rightPane = new VBox(5, executionPanel.getPane(), memoryAndRegisterTables);
        rightPane.setPrefWidth(400);
        VBox.setVgrow(executionPanel.getPane(), Priority.ALWAYS);

        // Criar um centerPane para o inputPanel
        VBox inputContainer = new VBox(inputPanel.getPane());
        HBox centerPane = new HBox(inputContainer);

        // Criamos o leftPane inicialmente sem o ObjectFilePanel
        // Usamos um VBox para agrupar (temporariamente) o symbolPanel; depois, quando o Controller for definido,
        // substituiremos esse grupo por um VBox contendo o ObjectFilePanel e o symbolPanel.
        VBox filesAndSymbols = new VBox(symbolPanel.getPane());
        leftPane = new HBox(filesAndSymbols);

        HBox mainContent = new HBox(5, leftPane, centerPane, rightPane);
        mainContent.setPadding(new Insets(10));

        root.setCenter(mainContent);
        root.setPadding(new Insets(0));
    }

    public void setController(Controller mainController) {
        this.mainController = mainController;
        // Cria o ObjectFilePanel usando o Controller já criado
        this.objectFilePanel = new FileListPanel(mainController);

        // Atualiza o container que agrupa o ObjectFilePanel e o SymbolPanel
        VBox filesAndSymbols = new VBox(5, objectFilePanel.getPane(), symbolPanel.getPane());
        VBox.setVgrow(inputPanel.getPane(), Priority.ALWAYS);

        // Atualiza o leftPane com os novos componentes, mantendo o padding original
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
            // Atualizamos o inputPanel com os MainButtons também
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
