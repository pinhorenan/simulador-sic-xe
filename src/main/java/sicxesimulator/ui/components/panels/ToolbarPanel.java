package sicxesimulator.ui.components.panels;

import javafx.scene.control.*;
import sicxesimulator.ui.controller.Controller;


public class ToolbarPanel {
    private final MenuBar menuBar;

    public ToolbarPanel(Controller controller) {
        this.menuBar = createMenuBar(controller);
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    private MenuBar createMenuBar(Controller controller) {
        MenuBar menuBar = new MenuBar();

        // Menu "Arquivo"
        Menu fileMenu = new Menu("Arquivo");
        MenuItem openAsmFile = new MenuItem("Abrir Arquivo .ASM");
        openAsmFile.setOnAction(e -> controller.handleImportASM());

        fileMenu.getItems().addAll(openAsmFile);

        // Menu do Montador
        Menu assemblerMenu = new Menu("Montador");

        MenuItem showObjectCode = new MenuItem("Mostrar código objeto");
        showObjectCode.setOnAction(e -> controller.handleShowObjectCode());

        assemblerMenu.getItems().add(showObjectCode);

        // Menu do Ligador
        Menu linkerMenu = new Menu("Ligador");
        MenuItem setLinkerMode = new MenuItem("Selecionar Modo");
        setLinkerMode.setOnAction(e -> controller.handleSetLinkerModeAction());

        linkerMenu.getItems().addAll(setLinkerMode);

        // Menu "Memória"
        Menu memoryMenu = new Menu("Memória");
        MenuItem clearMemoryItem = new MenuItem("Limpar Memória");
        clearMemoryItem.setOnAction(e -> controller.handleClearMemoryAction());

        MenuItem changeMemorySizeItem = new MenuItem("Tamanho da Memória");
        changeMemorySizeItem.setOnAction(e -> controller.handleChangeMemorySizeAction());

        memoryMenu.getItems().addAll(clearMemoryItem, changeMemorySizeItem);

        // Menu "Ajuda"
        Menu helpMenu = new Menu("Ajuda");
        MenuItem helpItem = new MenuItem("Abrir janela de ajuda");
        helpItem.setOnAction(e -> controller.handleHelpAction());

        helpMenu.getItems().add(helpItem);

        menuBar.getMenus().addAll(fileMenu, assemblerMenu, linkerMenu, memoryMenu, helpMenu);
        return menuBar;
    }
}
