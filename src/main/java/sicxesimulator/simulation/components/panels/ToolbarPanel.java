package sicxesimulator.simulation.components.panels;

import javafx.scene.control.*;
import sicxesimulator.simulation.controller.MenuBarController;

public class ToolbarPanel {
    private final MenuBar menuBar;

    public ToolbarPanel(MenuBarController menuBarController) {
        this.menuBar = createMenuBar(menuBarController);
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    private MenuBar createMenuBar(MenuBarController menuBarController) {
        MenuBar menuBar = new MenuBar();

        // Menu "Arquivo"
        Menu fileMenu = new Menu("Arquivo");
        MenuItem openAsmFile = new MenuItem("Abrir Arquivo .ASM");
        openAsmFile.setOnAction(e -> menuBarController.handleImportASM());

        fileMenu.getItems().addAll(openAsmFile);

        // Menu do Montador
        Menu assemblerMenu = new Menu("Montador");

        MenuItem showObjectCode = new MenuItem("Mostrar código objeto");
        showObjectCode.setOnAction(e -> menuBarController.handleShowObjectCode());

        assemblerMenu.getItems().add(showObjectCode);

        // Menu do Ligador
        Menu linkerMenu = new Menu("Ligador");
        MenuItem setLinkerMode = new MenuItem("Selecionar Modo");
        setLinkerMode.setOnAction(e -> menuBarController.handleSetLinkerModeAction());

        linkerMenu.getItems().addAll(setLinkerMode);

        // Menu "Memória"
        Menu memoryMenu = new Menu("Memória");
        MenuItem clearMemoryItem = new MenuItem("Limpar Memória");
        clearMemoryItem.setOnAction(e -> menuBarController.handleClearMemoryAction());

        MenuItem changeMemorySizeItem = new MenuItem("Tamanho da Memória");
        changeMemorySizeItem.setOnAction(e -> menuBarController.handleChangeMemorySizeAction());

        memoryMenu.getItems().addAll(clearMemoryItem, changeMemorySizeItem);

        // Menu "Ajuda"
        Menu helpMenu = new Menu("Ajuda");
        MenuItem helpItem = new MenuItem("Abrir janela de ajuda");
        helpItem.setOnAction(e -> menuBarController.handleHelpAction());

        helpMenu.getItems().add(helpItem);

        menuBar.getMenus().addAll(fileMenu, assemblerMenu, linkerMenu, memoryMenu, helpMenu);
        return menuBar;
    }
}
