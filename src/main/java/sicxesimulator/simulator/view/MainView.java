package sicxesimulator.simulator.view;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.simulator.controller.Controller;
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
    private static Model injectedModel;
    private Controller controller;

    // Configurações de exibição
    private ViewConfig viewConfig;

    // Componentes da interface
    private Stage primaryStage;
    private SimulationToolbar simulationToolbar;

    // Áreas de texto (lado esquerdo)
    private TextArea inputArea;
    private TextArea macroOutArea;
    private TextArea outputArea;

    // Componente para exibição dos ObjectFiles montados (TableView customizado)
    private ObjectFileTableView objectFileTableView;

    // Tabelas de Memória, Registradores e Símbolos
    private MemoryTableView memoryTable;
    private RegisterTableView registerTable;
    private SymbolTableView symbolTable;

    // Labels de status
    private Label executionSpeedLabel;
    private Label memorySizeLabel;
    private Label viewFormatLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        if (injectedModel == null) {
            throw new IllegalStateException("O model não foi injetado! Utilize MainApp.setModel(model) antes de chamar launch().");
        }
        Model model = injectedModel;
        controller = new Controller(model, this);
        simulationToolbar = new SimulationToolbar(controller, this);

        // Configurações de exibição
        viewConfig = model.getViewConfig();
        viewConfig.addFormatChangeListener(newFormat -> updateAllTables());

        // Cria o layout principal
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(createMainContent());
        root.setBottom(createBottomBar());

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulador SIC/XE");
        primaryStage.show();

        simulationToolbar.setupBindings();
        controller.handleInitializeObjectFiles();
        configureStageProperties();


        initializeUI();

        // Para debug TODO: RETIRAR DEPOIS
        getObjectFileTableView().getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Object> change) -> {
            System.out.println("Itens selecionados: " +
                    getObjectFileTableView().getSelectionModel().getSelectedItems().size());
        });
    }

    /// MÉTODOS DE MONTAGEM DA INTERFACE

    private HBox createMemoryRegisterSymbolPane() {
        memoryTable = new MemoryTableView();
        registerTable = new RegisterTableView();
        symbolTable = new SymbolTableView();

        ScrollPane memoryScroll = new ScrollPane(memoryTable);
        memoryScroll.setFitToWidth(true);
        memoryScroll.setFitToHeight(true);

        ScrollPane registerScroll = new ScrollPane(registerTable);
        registerScroll.setFitToWidth(true);
        registerScroll.setFitToHeight(true);

        ScrollPane symbolScroll = new ScrollPane(symbolTable);
        symbolScroll.setFitToWidth(true);
        symbolScroll.setFitToHeight(true);

        TitledPane memoryTitled = new TitledPane("Memória", memoryScroll);
        memoryTitled.setCollapsible(false);
        TitledPane registerTitled = new TitledPane("Registradores", registerScroll);
        registerTitled.setCollapsible(false);
        TitledPane symbolTitled = new TitledPane("Símbolos", symbolScroll);
        symbolTitled.setCollapsible(false);

        memoryTitled.setPrefHeight(150);
        registerTitled.setPrefHeight(150);
        symbolTitled.setPrefHeight(150);

        HBox tablesBox = new HBox(10, memoryTitled, registerTitled, symbolTitled);
        tablesBox.setAlignment(Pos.BOTTOM_CENTER);
        return tablesBox;
    }

    private TitledPane createObjectFileTablePane() {
        // Instancia a ObjectFileTableView; as colunas já são configuradas no seu construtor.
        objectFileTableView = new ObjectFileTableView();
        // Configura a seleção múltipla padrão
        objectFileTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Cria os botões "Linkar" para o header
        Button linkButton = simulationToolbar.getLinkButton();

        // Botão "Deletar" para o header
        Button deletarButton = simulationToolbar.getDeleteButton();

        // Cria o header customizado com um HBox: Label à esquerda, botões à direita
        Label headerLabel = new Label("Arquivos Montados");
        Region spacer = new Region();
        spacer.setPrefWidth(100);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox headerBox = new HBox(10, headerLabel, spacer, linkButton, deletarButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Cria o TitledPane e define o header customizado e o conteúdo (a tabela)
        TitledPane fileListTitled = new TitledPane();
        fileListTitled.setText(null); // Remove o texto padrão
        fileListTitled.setGraphic(headerBox); // Define o cabeçalho customizado
        fileListTitled.setContent(objectFileTableView);
        fileListTitled.setCollapsible(false);
        fileListTitled.setPrefHeight(150);
        fileListTitled.setMaxWidth(Double.MAX_VALUE);
        fileListTitled.setStyle("-fx-border-color: #CCC; -fx-border-width: 1;");

        return fileListTitled;
    }

    private VBox createLeftPane() {
        VBox leftPane = new VBox(5);
        leftPane.setPadding(new Insets(5));

        // Área de entrada de código
        inputArea = new TextArea();
        inputArea.setPromptText("Insira seu código assembly aqui...");
        inputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");
        ScrollPane inputScroll = new ScrollPane(inputArea);
        inputScroll.setFitToWidth(true);
        inputScroll.setFitToHeight(true);



        // Cria um cabeçalho customizado com rótulo e botão "Montar"
        Label titleLabel = new Label("Código de Entrada");
        Button montarButton = simulationToolbar.getAssembleButton();
        // Um Region para empurrar o botão para a direita
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setPrefWidth(100);
        HBox headerBox = new HBox(10, titleLabel, spacer, montarButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Cria o TitledPane e define o cabeçalho customizado
        TitledPane inputTitled = new TitledPane();
        // Remove o texto padrão
        inputTitled.setText(null);
        // Define o cabeçalho customizado
        inputTitled.setGraphic(headerBox);
        inputTitled.setContent(inputScroll);
        inputTitled.setCollapsible(false);
        inputTitled.setCollapsible(false);

        // Área de código expandido (Macros)
        macroOutArea = new TextArea();
        macroOutArea.setPromptText("Macros expandidos");
        macroOutArea.setEditable(false);
        macroOutArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: #006400;");
        ScrollPane expandedScroll = new ScrollPane(macroOutArea);
        expandedScroll.setFitToWidth(true);
        expandedScroll.setFitToHeight(true);
        TitledPane expandedTitled = new TitledPane("Código Expandido", expandedScroll);
        expandedTitled.setCollapsible(false);

        // Junta as áreas de entrada e expandido lado a lado
        HBox inputExpandedRow = new HBox(5, inputTitled, expandedTitled);
        HBox.setHgrow(inputTitled, Priority.ALWAYS);
        HBox.setHgrow(expandedTitled, Priority.ALWAYS);

        // Cria o painel de arquivos montados (TableView encapsulado em TitledPane)
        TitledPane fileListTitled = createObjectFileTablePane();

        HBox fileListAndControls = new HBox(5, fileListTitled);
        HBox.setHgrow(fileListTitled, Priority.ALWAYS);


        // Cria as tabelas de memória, registradores e símbolos
        HBox tablesBox = createMemoryRegisterSymbolPane();

        leftPane.getChildren().addAll(inputExpandedRow, fileListAndControls, tablesBox);
        VBox.setVgrow(inputExpandedRow, Priority.ALWAYS);

        return leftPane;
    }

    private VBox createRightPane() {
        VBox rightPane = new VBox(5);
        rightPane.setPadding(new Insets(5));

        // Área de saída de mensagens
        outputArea = new TextArea();
        outputArea.setPromptText("Saída de mensagens...");
        outputArea.setEditable(false);
        outputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: green;");
        outputArea.setPrefHeight(500);
        outputArea.setPrefWidth(550);
        TitledPane outputTitled = new TitledPane("Saída de Mensagens", outputArea);
        outputTitled.setCollapsible(false);

        // Botões de execução: Executar, Pausar, Próximo
        HBox executionControls = simulationToolbar.getExecutionControls();
        // Botão RESET
        HBox resetBox = simulationToolbar.getResetControl();

        rightPane.getChildren().addAll(outputTitled, executionControls, resetBox);
        HBox.setHgrow(outputTitled, Priority.ALWAYS);

        return rightPane;
    }

    private HBox createMainContent() {
        HBox mainContent = new HBox(10);
        mainContent.setPadding(new Insets(10));

        VBox leftPane = createLeftPane();
        VBox rightPane = createRightPane();

        HBox.setHgrow(leftPane, Priority.ALWAYS);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        mainContent.getChildren().addAll(leftPane, rightPane);
        return mainContent;
    }

    private HBox createBottomBar() {
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
        openAsmFile.setOnAction(e -> controller.handleImportASM());
        MenuItem exportExpandedCode = new MenuItem("Exportar.ASM Expandido");
        exportExpandedCode.setOnAction(e -> {
            try {
                controller.handleExportASM();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem exportObjFile = new MenuItem("Exportar Arquivo .OBJ");
        exportObjFile.setOnAction(e -> controller.handleExportOBJ());
        fileMenu.getItems().addAll(openAsmFile, exportExpandedCode, exportObjFile);

        // Menu "Montador"
        Menu assemblerMenu = new Menu("Montador");
        MenuItem assembleCodeItem = new MenuItem("Montar Código");
        assembleCodeItem.setOnAction(e -> controller.handleAssembleAction());

        // Menu "Memória"
        Menu memoryMenu = new Menu("Memória");
        MenuItem clearMemoryItem = new MenuItem("Limpar Memória");
        clearMemoryItem.setOnAction(e -> controller.handleClearMemoryAction());
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
        hexView.setOnAction(e -> controller.handleSetHexViewAction());
        MenuItem octView = new MenuItem("Octal");
        octView.setOnAction(e -> controller.handleSetOctalViewAction());
        MenuItem decView = new MenuItem("Decimal");
        decView.setOnAction(e -> controller.handleSetDecimalViewAction());
        MenuItem binView = new MenuItem("Binário");
        binView.setOnAction(e -> controller.handleSetBinaryViewAction());
        viewMenu.getItems().addAll(hexView, octView, decView, binView);

        // Menu "Ajuda"
        Menu helpMenu = new Menu("Ajuda");
        MenuItem helpItem = new MenuItem("Ajuda e Tutorial");
        helpItem.setOnAction(e -> controller.handleHelpAction());
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

        menuBar.getMenus().addAll(fileMenu, assemblerMenu, executionMenu, viewMenu, helpMenu, aboutMenu, creditsMenu);
        return menuBar;
    }

    /// MÉTODOS DE CONFIGURAÇÃO DA INTERFACE

    private void configureStageProperties() {
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
    }

    public void initializeView() {
        List<ObjectFile> files = controller.getLinkableObjectFiles();
        if (files.isEmpty()) {
        } else {
            List<ObjectFileTableItem> items = Convert.objectFileToObjectFileTableItem(files);
            updateObjectFileTableView(items);
        }
    }



    private void initializeUI() {
        showWelcomeMessage();
        updateAllTables();
        updateViewFormatLabel();
        updateCycleDelayLabel();
        updateMemorySizeLabel();
    }

    /// GETTERS

    public Stage getStage() {
        return primaryStage;
    }

    public TextArea getInputField() {
        return inputArea;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public TextArea getMacroArea() {
        return macroOutArea;
    }

    public ObjectFileTableView getObjectFileTableView() {
        return objectFileTableView;
    }

    /// SETTERS

    public static void setModel(Model model) {
        injectedModel = model;
    }

    public void setWindowTitle(String title) {
        Platform.runLater(() -> primaryStage.setTitle(title));
    }

    public void setViewFormat(String format) {
        viewConfig.setAddressFormat(format);
    }

    /// MÉTODOS DE ATUALIZAÇÃO DE COMPONENTES

    public void updateObjectFileTableView(List<ObjectFileTableItem> items) {
        Platform.runLater(() -> {
            objectFileTableView.getItems().clear();
            objectFileTableView.getItems().addAll(items);
        });
    }


    // TABELAS

    public void updateAllTables() {
        Platform.runLater(() -> {
            updateMemoryTable();
            updateRegisterTable();
            updateSymbolTable();
        });
    }

    public void updateMemoryTable() {
        memoryTable.getItems().clear();
        List<MemoryEntry> entries = controller.getMemoryEntries();
        memoryTable.getItems().addAll(entries);
    }

    public void updateRegisterTable() {
        registerTable.getItems().clear();
        List<RegisterEntry> entries = controller.getRegisterEntries();
        registerTable.getItems().addAll(entries);
    }

    public void updateSymbolTable() {
        symbolTable.getItems().clear();
        List<SymbolEntry> entries = controller.getSymbolEntries();
        symbolTable.getItems().addAll(entries);
    }

    // LABELS

    public void updateAllLabels() {
        updateViewFormatLabel();
        updateCycleDelayLabel();
        updateMemorySizeLabel();
    }

    public void updateViewFormatLabel() {
        Platform.runLater(() -> viewFormatLabel.setText("Formato: " + viewConfig.getAddressFormat()));
    }

    public void updateCycleDelayLabel() {
        Platform.runLater(() -> executionSpeedLabel.setText("Atraso de ciclo: " + controller.getCycleDelay()));
    }

    public void updateMemorySizeLabel() {
        Platform.runLater(() -> memorySizeLabel.setText("Memória: " + controller.getMemorySize() + " bytes"));
    }


    public void clearOutputArea() {
        outputArea.clear();
    }

    public void clearMacroOutArea() {
        macroOutArea.clear();
    }

    public void clearTables() {
        memoryTable.getItems().clear();
        registerTable.getItems().clear();
        symbolTable.getItems().clear();
    }

    /// MÉTODOS SHOW (utilizando DialogUtil)

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
            controller.handleChangeRunningSpeedAction(speedValue);
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
                controller.getMemorySize() + " bytes");
        result.ifPresent(input -> {
            try {
                int newSize = Integer.parseInt(input);
                if (newSize <= 0) {
                    DialogUtil.showErrorDialog("Valor Inválido", "Tamanho da Memória", "O valor deve ser maior que zero!");
                    return;
                }
                controller.handleChangeMemorySizeAction(newSize);
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

    public void showNoFilesMessage() {
        DialogUtil.showErrorDialog("Aviso", "Nenhum arquivo montado", "Por favor, monte um programa primeiro.");
    }


    /// MÉTODOS DE CONTROLE DE COMPONENTES

    public void appendOutput(String message) {
        Platform.runLater(() -> outputArea.appendText("> " + message + "\n"));
    }


    /// MAIN

    public static void main(String[] args) {
        launch(args);
    }
}
