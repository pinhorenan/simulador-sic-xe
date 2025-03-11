package sicxesimulator.application.components.panels;

import javafx.scene.control.*;
import sicxesimulator.application.controller.MenuBarController;
import sicxesimulator.application.components.buttons.MainButtons;

public class ToolbarPanel {
    private final MenuBar menuBar;
    private final MainButtons mainButtons;

    public ToolbarPanel(MenuBarController menuBarController, MainButtons simulationToolbar) {
        this.menuBar = createMenuBar(menuBarController);
        this.mainButtons = simulationToolbar;
    }

    private MenuBar createMenuBar(MenuBarController menuBarController) {
        MenuBar menuBar = new MenuBar();

        // Menu "Arquivo"
        Menu fileMenu = new Menu("Arquivo");
        MenuItem openAsmFile = new MenuItem("Abrir Arquivo .ASM");
        openAsmFile.setOnAction(e -> menuBarController.handleImportASM());

        MenuItem exportExpandedCode = new MenuItem("Exportar .ASM Expandido");
        exportExpandedCode.setOnAction(e -> {
            try {
                menuBarController.handleExportASM();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erro ao exportar código ASM: " + ex.getMessage()).showAndWait();
            }
        });

        MenuItem exportObjFile = new MenuItem("Exportar Arquivo .OBJ");
        exportObjFile.setOnAction(e -> menuBarController.handleExportOBJ());

        fileMenu.getItems().addAll(openAsmFile, exportExpandedCode, exportObjFile);

        // Menu "Memória"
        Menu memoryMenu = new Menu("Memória");
        MenuItem clearMemoryItem = new MenuItem("Limpar Memória");
        clearMemoryItem.setOnAction(e -> menuBarController.handleClearMemoryAction());

        memoryMenu.getItems().add(clearMemoryItem);

        // Menu "Execução"
        Menu executionMenu = new Menu("Execução");
        MenuItem executionSpeedItem = new MenuItem("Velocidade de execução");
        executionSpeedItem.setOnAction(e -> menuBarController.handleChangeRunningSpeedAction(3));

        executionMenu.getItems().add(executionSpeedItem);

        // Menu "Exibição"
        Menu viewMenu = new Menu("Exibição");
        MenuItem hexView = new MenuItem("Hexadecimal");
        hexView.setOnAction(e -> menuBarController.handleSetHexViewAction());

        MenuItem decView = new MenuItem("Decimal");
        decView.setOnAction(e -> menuBarController.handleSetDecimalViewAction());

        viewMenu.getItems().addAll(hexView, decView);

        // Menu "Ajuda"
        Menu helpMenu = new Menu("Ajuda");
        MenuItem helpItem = new MenuItem("Ajuda e Tutorial");
        helpItem.setOnAction(e -> menuBarController.handleHelpAction());

        helpMenu.getItems().add(helpItem);

        menuBar.getMenus().addAll(fileMenu, memoryMenu, executionMenu, viewMenu, helpMenu);
        return menuBar;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public MainButtons getMainButtons() {
        return mainButtons;
    }
}
