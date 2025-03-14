package sicxesimulator.application.components.panels;

import javafx.scene.control.*;
import sicxesimulator.application.controller.MenuBarController;
import sicxesimulator.utils.DialogUtil;

public class ToolbarPanel {
    private final MenuBar menuBar;

    public ToolbarPanel(MenuBarController menuBarController) {
        this.menuBar = createMenuBar(menuBarController);
    }

    private MenuBar createMenuBar(MenuBarController menuBarController) {
        MenuBar menuBar = new MenuBar();

        // Menu "Arquivo"
        Menu fileMenu = new Menu("Arquivo");

        // Esse é essencial.
        MenuItem openAsmFile = new MenuItem("Abrir Arquivo .ASM");
        openAsmFile.setOnAction(e -> menuBarController.handleImportASM());

        // TODO: Revisar a necessidade desse menu
        MenuItem exportExpandedCode = new MenuItem("Exportar .ASM Expandido");
        exportExpandedCode.setOnAction(e -> {
            try {
                menuBarController.handleExportASM();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erro ao exportar código ASM: " + ex.getMessage()).showAndWait();
            }
        });

        // TODO: Revisar a necessidade desse menu
        MenuItem exportObjFile = new MenuItem("Exportar Arquivo .OBJ");
        exportObjFile.setOnAction(e -> menuBarController.handleExportOBJ());

        MenuItem clearObjectDirectory = new MenuItem("Limpar arquivos salvos");
        clearObjectDirectory.setOnAction(e -> menuBarController.handleClearObjectDirectory());

        fileMenu.getItems().addAll(openAsmFile, exportExpandedCode, exportObjFile, clearObjectDirectory);

        // TODO: Menu do Processador de Macros
        Menu macroProcessorMenu = new Menu("Processador de Macros");
        MenuItem setMacroProcessorMode = new MenuItem("Modo de Processamento");
        setMacroProcessorMode.setOnAction(e -> DialogUtil.showAlert(Alert.AlertType.INFORMATION, "Modo de Processamento", "Modo de Processamento", "Nada implementado por aqui ainda."));

        macroProcessorMenu.getItems().addAll(setMacroProcessorMode);

        // TODO: Menu do Montador
        Menu assemblerMenu = new Menu("Montador");
        MenuItem setAssemblerMode = new MenuItem("Modo de Montagem");
        setAssemblerMode.setOnAction(e -> DialogUtil.showAlert(Alert.AlertType.INFORMATION, "Modo de Montagem", "Modo de Montagem", "Nada implementado por aqui ainda."));

        MenuItem showSymbolTable = new MenuItem("Mostrar tabela de símbolos");
        showSymbolTable.setOnAction(e -> DialogUtil.showAlert(Alert.AlertType.INFORMATION, "Modo de Montagem", "Modo de Montagem", "Nada implementado por aqui ainda."));

        MenuItem showObjectCode = new MenuItem("Mostrar código objeto");
        showObjectCode.setOnAction(e -> menuBarController.handleShowObjectCode());

        assemblerMenu.getItems().addAll(setAssemblerMode, showSymbolTable, showObjectCode);

        // TODO: Menu do Ligador
        Menu linkerMenu = new Menu("Ligador");
        MenuItem setLinkerMode = new MenuItem("Modo de Ligação");
        setLinkerMode.setOnAction(e -> menuBarController.handleSetLinkerModeAction());

        linkerMenu.getItems().addAll(setLinkerMode);

        // TODO: Menu do Carregador
        Menu loaderMenu = new Menu("Carregador");
        MenuItem setLoaderMode = new MenuItem("Modo de Carregamento");
        setLoaderMode.setOnAction(e -> DialogUtil.showAlert(Alert.AlertType.INFORMATION, "Modo de Carregamento", "Modo de Carregamento", "Nada implementado por aqui ainda."));

        loaderMenu.getItems().addAll(setLoaderMode);

        // Menu "Memória"
        Menu memoryMenu = new Menu("Memória");
        MenuItem clearMemoryItem = new MenuItem("Limpar Memória");
        clearMemoryItem.setOnAction(e -> menuBarController.handleClearMemoryAction());

        MenuItem changeMemorySizeItem = new MenuItem("Tamanho da Memória");
        changeMemorySizeItem.setOnAction(e -> menuBarController.handleChangeMemorySizeAction());

        memoryMenu.getItems().addAll(clearMemoryItem, changeMemorySizeItem);

        // Menu "Execução"
        Menu executionMenu = new Menu("Execução");
        MenuItem executionSpeedItem = new MenuItem("Velocidade de execução");
        executionSpeedItem.setOnAction(e -> menuBarController.handleChangeRunningSpeedAction(3));

        executionMenu.getItems().add(executionSpeedItem);

        // Menu "Exibição"
        Menu viewMenu = new Menu("Exibição");
        MenuItem hexView = new MenuItem("Hexadecimal");
        hexView.setOnAction(e -> menuBarController.handleSetHexViewAction());

        MenuItem octView = new MenuItem("Octal");
        octView.setOnAction(e -> menuBarController.handleSetOctalViewAction());

        MenuItem decView = new MenuItem("Decimal");
        decView.setOnAction(e -> menuBarController.handleSetDecimalViewAction());

        MenuItem binView = new MenuItem("Binário");
        binView.setOnAction(e -> menuBarController.handleSetBinaryViewAction());

        viewMenu.getItems().addAll(hexView, octView, decView, binView);

        // Menu "Ajuda"
        Menu helpMenu = new Menu("Ajuda");
        MenuItem helpItem = new MenuItem("Ajuda e Tutorial");
        helpItem.setOnAction(e -> menuBarController.handleHelpAction());

        helpMenu.getItems().add(helpItem);

        menuBar.getMenus().addAll(fileMenu, macroProcessorMenu, assemblerMenu, linkerMenu, loaderMenu,  memoryMenu, executionMenu, viewMenu, helpMenu);
        return menuBar;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }
}
