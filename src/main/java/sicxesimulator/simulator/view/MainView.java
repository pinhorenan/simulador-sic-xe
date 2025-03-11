package sicxesimulator.simulator.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.simulator.controller.Controller;
import sicxesimulator.simulator.controller.MenuBarController;
import sicxesimulator.simulator.model.Model;
import sicxesimulator.simulator.view.components.SimulationToolbar;
import sicxesimulator.simulator.view.components.tables.*;
import sicxesimulator.simulator.view.records.MemoryEntry;
import sicxesimulator.simulator.view.records.RegisterEntry;
import sicxesimulator.simulator.view.records.SymbolEntry;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.DialogUtil;
import sicxesimulator.utils.ViewConfig;

import java.io.IOException;
import java.util.*;

public class MainView extends javafx.application.Application {
    private Stage primaryStage;
    private SimulationToolbar simulationToolbar;

    private Controller mainController;
    private MenuBarController menuBarController;

    // ===== Injeção de dependência do Model =====
    private static Model injectedModel;

    // Configurações de exibição
    private ViewConfig viewConfig;

    // Componentes
    private TextArea inputArea;
    private TextArea macroOutArea;
    private TextArea outputArea;

    // Componente para exibição dos ObjectFiles montados (TableView customizado)
    private ObjectFileTableView objectFileTableView;

    // Componentes para exibição de tabelas de dados
    private MemoryTableView memoryTable;
    private RegisterTableView registerTable;
    private SymbolTableView symbolTable;

    // Labels de status
    private Label executionSpeedLabel;
    private Label memorySizeLabel;
    private Label viewFormatLabel;

    /**
     * Inicializa a aplicação JavaFX.
     * @param primaryStage O palco principal da aplicação
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Configurações da janela
        primaryStage.setResizable(false);
        primaryStage.setTitle("Simulador SIC/XE");

        // Verifica se o model foi injetado
        if (injectedModel == null) {
            throw new IllegalStateException("O model não foi injetado! Utilize MainApp.setModel(model) antes de chamar launch().");
        }
        Model model = injectedModel;

        // Inicializa o controlador e a barra de ferramentas
        mainController = new Controller(model, this);
        menuBarController = new MenuBarController(mainController);
        simulationToolbar = new SimulationToolbar(mainController, this);

        // Configurações de exibição
        viewConfig = model.getViewConfig();
        viewConfig.addFormatChangeListener(newFormat -> updateAllTables());

        // Cria o layout principal
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(createMainContentHBox());
        root.setBottom(createBottomBarHBox());

        // Cria a cena e exibe a janela
        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setScene(scene);
        primaryStage.show();

        simulationToolbar.setupBindings();

        initializeUI();
    }

    /// ===== Métodos de criação de componentes =====

    private TitledPane createMemoryTableTitledPane() {
        // Cria a tabela de memória
        memoryTable = new MemoryTableView();

        // Cria o TitledPane e define o conteúdo (a tabela)
        TitledPane memoryTitled = new TitledPane("Memória", memoryTable);
        memoryTitled.setCollapsible(false);

        return memoryTitled;
    }

    private TitledPane createRegisterTableTitledPane() {
        // Cria a tabela de registradores
        registerTable = new RegisterTableView();

        // Cria o TitledPane e define o conteúdo (a tabela)
        TitledPane registerTitled = new TitledPane("Registradores", registerTable);
        registerTitled.setCollapsible(false);

        return registerTitled;
    }

    private TitledPane createSymbolTableTitledPane() {
        // Cria a tabela de símbolos
        symbolTable = new SymbolTableView();

        // Cria o TitledPane e define o conteúdo (a tabela)
        TitledPane symbolTitled = new TitledPane("Símbolos", symbolTable);
        symbolTitled.setCollapsible(false);

        return symbolTitled;
    }

    private TitledPane createObjectFileTableTitledPane() {
        // Instancia a ObjectFileTableView
        objectFileTableView = new ObjectFileTableView();
        objectFileTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        objectFileTableView.setMaxWidth(Double.MAX_VALUE);

        // Obtém o container de botões (já configurado na toolbar)
        HBox fileControlButtonsHBox = simulationToolbar.getObjectFileTableViewButtonsHBox();

        // Rótulo do cabeçalho
        Label headerLabel = new Label("Arquivos Montados");

        // Substitui o HBox por um BorderPane para posicionar label e botões
        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(headerLabel);
        headerPane.setRight(fileControlButtonsHBox);
        // Se quiser algum espaçamento interno, pode fazer:
        // headerPane.setPadding(new Insets(5));

        // Cria o TitledPane e configura o cabeçalho customizado e o conteúdo
        TitledPane fileListTitled = new TitledPane();
        fileListTitled.setText(null); // Desativa o texto padrão
        fileListTitled.setGraphic(headerPane); // Usa nosso BorderPane como “título”
        fileListTitled.setContent(objectFileTableView);
        fileListTitled.setCollapsible(false);
        fileListTitled.setMaxHeight(Double.MAX_VALUE);
        fileListTitled.setMaxWidth(Double.MAX_VALUE);

        return fileListTitled;
    }

    private TitledPane createInputAreaTitledPane() {
        // Área de texto
        inputArea = new TextArea();
        inputArea.setPromptText("Insira seu código assembly aqui...");
        inputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");
        inputArea.setPrefHeight(300);

        // Cria um rótulo
        Label titleLabel = new Label("Código de Entrada");

        // Botão “Montar”
        Button assembleButton = simulationToolbar.getAssembleButton();

        // Em vez de HBox, usamos BorderPane
        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(titleLabel);
        headerPane.setRight(assembleButton);

        // Cria o TitledPane
        TitledPane inputTitled = new TitledPane();
        inputTitled.setContent(inputArea);
        inputTitled.setText(null);
        inputTitled.setCollapsible(false);
        inputTitled.setMinHeight(300);
        // Define o BorderPane como “graphic”, para servir de cabeçalho
        inputTitled.setGraphic(headerPane);

        return inputTitled;
    }

    private TitledPane createMacroOutputTitledPane() {
        // Área de código expandido (Macros)
        macroOutArea = new TextArea();
        macroOutArea.setPromptText("Macros expandidos");
        macroOutArea.setEditable(false);
        macroOutArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: #006400;");
        macroOutArea.setMaxHeight(Double.MAX_VALUE);
        macroOutArea.setPrefHeight(300);

        // Cria um BorderPane para o cabeçalho
        Label headerLabel = new Label("Código Expandido");
        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(headerLabel);
        // Se quiser algum espaçamento interno:
        // headerPane.setPadding(new Insets(5));

        // Cria o TitledPane e substitui o texto pelo nosso cabeçalho customizado
        TitledPane macroOutputTitled = new TitledPane();
        macroOutputTitled.setText(null); // remove texto padrão
        macroOutputTitled.setGraphic(headerPane); // usa nosso BorderPane como cabeçalho
        macroOutputTitled.setContent(macroOutArea);
        macroOutputTitled.setCollapsible(false);
        macroOutputTitled.setMaxHeight(Double.MAX_VALUE);
        macroOutputTitled.setMinHeight(300);

        return macroOutputTitled;
    }

    private TitledPane createMachineOutputTitledPane() {
        // Área de saída de mensagens
        outputArea = new TextArea();
        outputArea.setPromptText("Saída de mensagens...");
        outputArea.setEditable(false);
        outputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: green;");
        outputArea.setMaxHeight(Double.MAX_VALUE);
        outputArea.setPrefWidth(550);
        outputArea.setPrefHeight(300);

        // Botão de limpar a saída
        Button clearMachineOutputButton = simulationToolbar.getClearMachineOutputButton();

        // Rótulo
        Label titleLabel = new Label("Saída da Máquina");

        // Substitui o HBox por um BorderPane para organizar os elementos
        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(titleLabel);
        headerPane.setRight(clearMachineOutputButton);
        // Opcional: ajuste de espaçamento/padding no cabeçalho, se desejar
        // headerPane.setPadding(new Insets(5));

        // Cria o TitledPane e define o cabeçalho customizado com o BorderPane
        TitledPane machineOutputTitled = new TitledPane();
        machineOutputTitled.setText(null); // remove o texto padrão
        machineOutputTitled.setGraphic(headerPane);
        machineOutputTitled.setContent(outputArea);
        machineOutputTitled.setCollapsible(false);
        machineOutputTitled.setMaxHeight(Double.MAX_VALUE);
        machineOutputTitled.setMinHeight(300);

        return machineOutputTitled;
    }

    private VBox createLeftPaneVBox() {
        VBox leftPane = new VBox(5);
        leftPane.setPadding(new Insets(0));

        // Cria a área de input
        TitledPane codeInputTitled = createInputAreaTitledPane();

        // Cria o output de macros expandidos
        TitledPane macroOutputTitled = createMacroOutputTitledPane();

        // Junta as áreas de entrada e expandido lado a lado
        HBox codeInputAndMacroOutputHBox = new HBox(5, codeInputTitled, macroOutputTitled);
        codeInputAndMacroOutputHBox.setFillHeight(true);
        codeInputAndMacroOutputHBox.setMaxHeight(Double.MAX_VALUE);

        // Cria o painel de arquivos montados (TableView encapsulado em TitledPane) c
        TitledPane fileListTitled = createObjectFileTableTitledPane();

        // Cria a tabela de símbolos
        TitledPane symbolTitled = createSymbolTableTitledPane();

        // Junta as tabelas de arquivos e símbolos lado a lado
        HBox fileListPaneAndSymbolTablePane = new HBox(5, fileListTitled, symbolTitled);
        fileListPaneAndSymbolTablePane.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(fileListPaneAndSymbolTablePane, Priority.ALWAYS);
        VBox.setVgrow(codeInputAndMacroOutputHBox, Priority.ALWAYS);

        // Adiciona os componentes ao VBox principal
        leftPane.getChildren().addAll(codeInputAndMacroOutputHBox, fileListPaneAndSymbolTablePane);

        return leftPane;
    }

    private VBox createRightPaneVBox() {
        VBox rightPane = new VBox(5);
        rightPane.setPadding(new Insets(0));

        // Cria a área de saída de mensagens da máquina
        TitledPane machineOutputTitled = createMachineOutputTitledPane();

        // Cria a tabela de memória.
        TitledPane memoryTitled = createMemoryTableTitledPane();

        // Cria a tabela de registradores.
        TitledPane registerTitled = createRegisterTableTitledPane();

        // Junta as tabelas de memória e registradores lado a lado
        HBox memoryAndRegisterTablesHBox = new HBox(5, memoryTitled, registerTitled);
        memoryAndRegisterTablesHBox.setAlignment(Pos.BOTTOM_CENTER);

        // Botões de execução: Executar, Pausar, Próximo
        HBox executionControls = simulationToolbar.getMachineControlButtonsHBox();

        VBox.setVgrow(machineOutputTitled, Priority.ALWAYS);
        VBox.setVgrow(memoryAndRegisterTablesHBox, Priority.SOMETIMES);
        VBox.setVgrow(executionControls, Priority.SOMETIMES);

        // Adiciona os componentes ao VBox principal
        rightPane.getChildren().addAll(machineOutputTitled, memoryAndRegisterTablesHBox, executionControls);

        return rightPane;
    }

    private HBox createMainContentHBox() {
        HBox mainContent = new HBox(5);
        mainContent.setPadding(new Insets(10));

        // Cria os painéis esquerdo e direito
        VBox leftPane = createLeftPaneVBox();
        leftPane.setFillWidth(true);
        VBox rightPane = createRightPaneVBox();
        rightPane.setFillWidth(true);

        // Define a cor de fundo do painel principal
        mainContent.setBackground(new Background(new BackgroundFill(Color.web("#f0f9f3"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Adiciona os painéis ao HBox principal
        mainContent.getChildren().addAll(leftPane, rightPane);
        return mainContent;
    }

    private HBox createBottomBarHBox() {
        executionSpeedLabel = new Label("Atraso de ciclo: ");
        memorySizeLabel = new Label("Memória: ");
        viewFormatLabel = new Label("Formato: ");

        HBox bottomBar = new HBox(20, executionSpeedLabel, memorySizeLabel, viewFormatLabel);
        bottomBar.setPadding(new Insets(10));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle("-fx-background-color: #EEE; -fx-border-color: #CCC; -fx-padding: 5px;");
        return bottomBar;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menu "Arquivo"
        Menu fileMenu = new Menu("Arquivo");
        MenuItem openAsmFile = new MenuItem("Abrir Arquivo .ASM");
        openAsmFile.setOnAction(e -> menuBarController.handleImportASM());
        MenuItem exportExpandedCode = new MenuItem("Exportar.ASM Expandido");
        exportExpandedCode.setOnAction(e -> {
            try {
                menuBarController.handleExportASM();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem exportObjFile = new MenuItem("Exportar Arquivo .OBJ");
        exportObjFile.setOnAction(e -> menuBarController.handleExportOBJ());
        fileMenu.getItems().addAll(openAsmFile, exportExpandedCode, exportObjFile);

        // Menu "Memória"
        Menu memoryMenu = new Menu("Memória");

        MenuItem clearMemoryItem = new MenuItem("Limpar Memória");
        clearMemoryItem.setOnAction(e -> menuBarController.handleClearMemoryAction());

        MenuItem changeMemorySizeItem = new MenuItem("Tamanho da memória");
        changeMemorySizeItem.setOnAction(e -> showMemorySizeDialog());

        memoryMenu.getItems().addAll(changeMemorySizeItem, clearMemoryItem);

        // Menu "Execução"
        Menu executionMenu = new Menu("Execução");

        MenuItem executionSpeedItem = new MenuItem("Velocidade de execução");
        executionSpeedItem.setOnAction(e -> showExecutionSpeedDialog());

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

        // Menu "Sobre"
        Menu aboutMenu = new Menu("Sobre");

        MenuItem repository = new MenuItem("Repositório");
        repository.setOnAction(e -> getHostServices().showDocument("https://github.com/pinhorenan/SIC-XE-Simulator"));

        MenuItem info = new MenuItem("Informações");
        info.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sobre");
            alert.setHeaderText("Simulador SIC/XE");
            alert.setContentText("""
                    © 2025 SIC/XE
                    Time ROCK LEE VS GAARA
                    """);
            alert.showAndWait();
        });

        aboutMenu.getItems().addAll(info, repository);

        // Menu "Créditos"
        Menu creditsMenu = new Menu("Créditos");

        MenuItem renanPinho = new MenuItem("Renan Pinho");
        renanPinho.setOnAction(e -> getHostServices().showDocument("https://github.com/pinhorenan"));

        MenuItem luisRasch = new MenuItem("Luis Rasch");
        luisRasch.setOnAction(e -> getHostServices().showDocument("https://github.com/LuisEduardoRasch"));

        MenuItem gabrielMoura = new MenuItem("Gabriel Moura");
        gabrielMoura.setOnAction(e -> getHostServices().showDocument("https://github.com/gbrimoura"));

        MenuItem fabricioBartz = new MenuItem("Fabricio Bartz");
        fabricioBartz.setOnAction(e -> getHostServices().showDocument("https://github.com/FabricioBartz"));

        MenuItem arthurAlves = new MenuItem("Arthur Alves");
        arthurAlves.setOnAction(e -> getHostServices().showDocument("https://github.com/arthursa21"));

        MenuItem leonardoBraga = new MenuItem("Leonardo Braga");
        leonardoBraga.setOnAction(e -> getHostServices().showDocument("https://github.com/braga0425"));

        creditsMenu.getItems().addAll(renanPinho, luisRasch, gabrielMoura, arthurAlves, fabricioBartz, leonardoBraga);

        // Adiciona os menus à barra de menus
        menuBar.getMenus().addAll(fileMenu, executionMenu, viewMenu, helpMenu, aboutMenu, creditsMenu);
        return menuBar;
    }

    /// ===== Métodos de inicialização de componentes =====

    private void initializeUI() {
        showWelcomeMessage();
        updateAllTables();
        updateAllLabels();
        initializeFilesView();
    }

    public void initializeFilesView() {
        List<ObjectFile> files = mainController.getObjectFileListFromModel();
        if (!files.isEmpty()) {
            objectFileTableView.clearEntries();
            updateObjectFileTableView();
        }
    }

    /// ===== Métodos Getters =====

    public Stage getStage() {
        return primaryStage;
    }

    public TextArea getInputField() {
        return inputArea;
    }

    public TextArea getMacroArea() {
        return macroOutArea;
    }

    public ObjectFileTableView getObjectFileTableView() {
        return objectFileTableView;
    }

    /// ===== Métodos Setters =====

    public static void setModel(Model model) {
        injectedModel = model;
    }

    public void setViewFormat(String format) {
        viewConfig.setAddressFormat(format);
    }

    /// ===== Métodos de atualização de componentes =====

    public void updateObjectFileTableView() {
        List<ObjectFile> files = mainController.getObjectFileListFromModel();
        List<ObjectFileTableItem> items = Convert.objectFileToObjectFileTableItem(files);
        Platform.runLater(() -> objectFileTableView.getItems().addAll(items));
    }

    public void updateMacroOutputContent() {
        List<String> expanded = mainController.getMacroExpandedCode();

        // Atualiza o expandedArea com o conteúdo expandido
        getMacroArea().setText(String.join("\n", expanded));
    }

    /// ===== Métodos de atualização das TableView =====

    public void updateAllTables() {
        Platform.runLater(() -> {
            updateMemoryTableView();
            updateRegisterTableViews();
            updateSymbolTableViews();
        });
    }

    public void updateMemoryTableView() {
        memoryTable.getItems().clear();
        List<MemoryEntry> entries = mainController.getMemoryEntries();
        memoryTable.getItems().addAll(entries);
    }

    public void updateRegisterTableViews() {
        registerTable.getItems().clear();
        List<RegisterEntry> entries = mainController.getRegisterEntries();
        registerTable.getItems().addAll(entries);
    }

    public void updateSymbolTableViews() {
        symbolTable.getItems().clear();
        List<SymbolEntry> entries = mainController.getSymbolEntries();
        symbolTable.getItems().addAll(entries);
    }

    ///  ===== Métodos de atualização de Labels =====

    public void updateAllLabels() {
        updateViewFormatLabel();
        updateCycleDelayLabel();
        updateMemorySizeLabel();
    }

    public void updateViewFormatLabel() {
        Platform.runLater(() -> viewFormatLabel.setText("Formato: " + viewConfig.getAddressFormat()));
    }

    public void updateCycleDelayLabel() {
        Platform.runLater(() -> executionSpeedLabel.setText("Atraso de ciclo: " + mainController.getCycleDelay()));
    }

    public void updateMemorySizeLabel() {
        Platform.runLater(() -> memorySizeLabel.setText("Memória: " + mainController.getMemorySize() + " bytes"));
    }

    /// ===== Métodos de limpeza de componentes =====

    public void clearOutputArea() {
        outputArea.clear();
    }

    public void clearMacroOutArea() {
        macroOutArea.clear();
    }

    /// ===== Métodos de controle de janelas de diálogo =====

    public void showHelpWindow() {
        DialogUtil.showInfoDialog("Ajuda - Funcionalidades e Tutorial", "Funcionalidades, Comandos e Tutorial", "WIP");
    }

    public void showExecutionSpeedDialog() {
        List<String> options = Arrays.asList("Tempo real", "Rápido", "Médio", "Lento", "Muito lento");
        Optional<String> result = DialogUtil.showChoiceDialog(
                "Velocidade de Execução",
                "Selecione a velocidade de execução:",
                "Velocidade:",
                options);
        result.ifPresent(selected -> {
            int speedValue = switch (selected) {
                case "Rápido" -> 4;
                case "Médio" -> 3;
                case "Lento" -> 2;
                case "Muito lento" -> 1;
                default -> 0;
            };
            menuBarController.handleChangeRunningSpeedAction(speedValue);
            String delayInMs = switch (speedValue) {
                case 0 -> "Tempo real";
                case 1 -> "1000ms";
                case 2 -> "500ms";
                case 3 -> "250ms";
                case 4 -> "100ms";
                default -> "Erro na velocidade.";
            };
            executionSpeedLabel.setText("Atraso de ciclo: " + delayInMs);
        });
    }

    public void showMemorySizeDialog() {
        Optional<String> result = DialogUtil.showTextInputDialog(
                "Alterar Tamanho da Memória",
                "Defina o tamanho da memória",
                "Digite um número inteiro positivo:",
                mainController.getMemorySize() + " bytes");
        result.ifPresent(input -> {
            try {
                int newSize = Integer.parseInt(input);
                if (newSize <= 0) {
                    DialogUtil.showErrorDialog("Valor Inválido", "Tamanho da Memória", "O valor deve ser maior que zero!");
                    return;
                }
                menuBarController.handleChangeMemorySizeAction(newSize);
                memorySizeLabel.setText("Memória: " + newSize + " bytes");
                appendOutput("Tamanho da memória alterado para: " + newSize + " bytes.");
            } catch (NumberFormatException ex) {
                DialogUtil.showErrorDialog("Erro", "Valor Inválido", "Por favor, insira um número inteiro positivo.");
            }
        });
    }

    public void showWelcomeMessage() {
        String welcomeMessage = """
    ╔══════════════════════════════════════╗
    ║      Simulador SIC/XE                ║
    ║      © 2025 SIC/XE Rock Lee vs Gaara ║
    ╚══════════════════════════════════════╝

    -> Edite seu código assembly
    -> Use os controles de execução
    -> Configure via menus
    -> Monitore registradores/memória

    Dica: Comece carregando um exemplo!
    """;
        Arrays.stream(welcomeMessage.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .forEach(this::appendOutput);
    }

    public void appendOutput(String message) {
        Platform.runLater(() -> outputArea.appendText("> " + message + "\n"));
    }

     /// MAIN

    public static void main(String[] args) {
        launch(args);
    }
}
