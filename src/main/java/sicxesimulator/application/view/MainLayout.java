package sicxesimulator.application.view;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.controller.MenuBarController;
import sicxesimulator.application.components.panels.*;
import sicxesimulator.application.components.buttons.MainButtons;

public class MainLayout {
    private final BorderPane root;
    private final MemoryPanel memoryPanel;
    private final RegisterPanel registerPanel;
    private final SymbolPanel symbolPanel;
    private final OutputPanel outputPanel;
    private final InputPanel inputPanel;
    private final ExecutionControlsPanel executionControlsPanel;
    private BottomBarPanel bottomBarPanel;
    private ObjectFilePanel objectFilePanel; // Será definido depois, via setController

    private HBox leftPane;
    private VBox rightPane;
    private HBox mainContent;

    private MenuBarController menuBarController;
    private Controller mainController;
    private MainButtons mainButtons;

    public MainLayout() {
        this.root = new BorderPane();

        // Criamos os painéis que não dependem do Controller
        this.inputPanel = new InputPanel();
        this.outputPanel = new OutputPanel();
        this.memoryPanel = new MemoryPanel();
        this.registerPanel = new RegisterPanel();
        this.symbolPanel = new SymbolPanel();
        this.executionControlsPanel = new ExecutionControlsPanel();

        // Criamos o container rightPane (que não depende do ObjectFilePanel)
        HBox memoryAndRegisterTables = new HBox(10, memoryPanel.getPane(), registerPanel.getPane());
        rightPane = new VBox(5, outputPanel.getPane(), executionControlsPanel.getPane(), memoryAndRegisterTables);
        rightPane.setPrefWidth(400);
        VBox.setVgrow(outputPanel.getPane(), Priority.ALWAYS);

        // Criamos o leftPane inicialmente sem o ObjectFilePanel
        // Usamos um VBox para agrupar (temporariamente) o symbolPanel; depois, quando o Controller for definido,
        // substituiremos esse grupo por um VBox contendo o ObjectFilePanel e o symbolPanel.
        VBox filesAndSymbols = new VBox(5, symbolPanel.getPane());
        VBox inputContainer = new VBox(inputPanel.getPane());

        leftPane = new HBox(5, filesAndSymbols, inputContainer);
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        mainContent = new HBox(10, leftPane, rightPane);
        mainContent.setPadding(new Insets(10));

        root.setCenter(mainContent);
        root.setPadding(new Insets(0));
    }

    /**
     * Define o Controller e, a partir dele, instancia o ObjectFilePanel e atualiza o leftPane.
     */
    public void setController(Controller mainController) {
        this.mainController = mainController;
        // Cria o ObjectFilePanel usando o Controller já criado
        this.objectFilePanel = new ObjectFilePanel(mainController);

        // Atualiza o container que agrupa o ObjectFilePanel e o SymbolPanel
        VBox filesAndSymbols = new VBox(5, objectFilePanel.getPane(), symbolPanel.getPane());
        VBox inputContainer = new VBox(inputPanel.getPane());
        VBox.setVgrow(inputPanel.getPane(), Priority.ALWAYS);

        // Atualiza o leftPane com os novos componentes, mantendo o padding original
        leftPane.getChildren().setAll(filesAndSymbols, inputContainer);
    }

    public void setMenuBarController(MenuBarController menuBarController) {
        this.menuBarController = menuBarController;
        updateToolbar();
    }

    public void setMainButtons(MainButtons mainButtons) {
        this.mainButtons = mainButtons;
        updateToolbar();
        this.executionControlsPanel.setMainButtons(mainButtons);
        if (objectFilePanel != null) {
            this.objectFilePanel.setMainButtons(mainButtons);
        }
        this.inputPanel.setMainButtons(mainButtons);
    }

    private void updateToolbar() {
        if (menuBarController != null && mainButtons != null) {
            ToolbarPanel toolbarPanel = new ToolbarPanel(menuBarController, mainButtons);
            root.setTop(toolbarPanel.getMenuBar());
            // Atualizamos o inputPanel com os MainButtons também
            this.inputPanel.setMainButtons(mainButtons);
        }
    }

    public void updateBottomBar() {
        if (bottomBarPanel == null) {
            bottomBarPanel = new BottomBarPanel(mainController);
            root.setBottom(bottomBarPanel.getPane());
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

    public ObjectFilePanel getObjectFilePanel() {
        return objectFilePanel;
    }

    public OutputPanel getOutputPanel() {
        return outputPanel;
    }

    public InputPanel getInputPanel() {
        return inputPanel;
    }

    public ExecutionControlsPanel getExecutionControlsPanel() {
        return executionControlsPanel;
    }

    public BottomBarPanel getBottomBarPanel() {
        return bottomBarPanel;
    }
}
