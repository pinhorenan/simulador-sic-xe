package sicxesimulator.simulator.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.simulator.controller.MainController;
import sicxesimulator.simulator.model.SampleCodes;
import sicxesimulator.simulator.model.MainModel;
import sicxesimulator.simulator.view.components.*;
import sicxesimulator.utils.ViewConfig;

import java.io.IOException;
import java.util.*;

public class MainApp extends Application {
    private MainController controller;

    // Configurações de exibição
    private ViewConfig viewConfig;

    // Componentes da interface
    private Stage primaryStage;
    private SimulationToolbar simulationToolbar;

    // ListView para manter os arquivos de objeto montados
    private ListView<String> objectFileListView;

    // TextAreas, todas ficam no lado esquerdo
    private TextArea inputArea;
    private TextArea macroOutArea;
    private TextArea outputArea;

    // Tabelas
    private MemoryTableView memoryTable;
    private RegisterTableView registerTable;
    private SymbolTableView symbolTable;

    // Configurações e labels de status
    private Label executionSpeedLabel;
    private Label memorySizeLabel;
    private Label viewFormatLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Inicializa o
        MainModel model = new MainModel();
        controller = new MainController(model, this);
        objectFileListView = new ListView<>();

        // Configurações de exibição
        viewConfig = model.getViewConfig();
        viewConfig.addFormatChangeListener(newFormat -> updateAllTables());
        simulationToolbar = new SimulationToolbar(controller, this);

        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(createMainContent());   // Layout principal: divisão left/right
        root.setBottom(createBottomBar());

        // Adiciona a list view??
        VBox layout = new VBox(10);
        layout.getChildren().add(objectFileListView);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulador SIC/XE");
        primaryStage.show();

        configureStageProperties();
        initializeUI();
    }

    private void configureStageProperties() {
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
    }

    public void initializeView() {
        List<ObjectFile> objFiles = controller.getObjectFilesList();

        if (objFiles.isEmpty()) {
            showNoFilesMessage();  // Se não houver arquivos, exibe a mensagem
        } else {
            controller.loadObjFilesToListView();  // Caso haja arquivos, carrega a ListView
        }
    }

    public void showNoFilesMessage() {
        // Exibe uma mensagem de alerta se não houver arquivos montados
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText("Nenhum arquivo montado");
        alert.setContentText("Por favor, monte um programa primeiro.");
        alert.showAndWait();  // Exibe o alerta e aguarda o fechamento
    }

    private void initializeUI() {
        disableControls();
        showWelcomeMessage();
        updateAllTables();
        updateViewFormatLabel();
        updateCycleDelayLabel();
        updateMemorySizeLabel();
    }

    /**
     * Cria o conteúdo principal dividido em duas colunas (HBox):
     * - Lado Esquerdo: uma VBox com:
     *      [Área de entrada] e [SimulationToolbar] (botões de controle)
     * - Lado Direito: uma VBox com:
     *      [Código expandido]
     *      [Botão Reload]
     */
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

    /**
     * Cria o painel esquerdo (VBox) com:
     * - Área de entrada no topo.
     * - SimulationToolbar (botões de controle) logo abaixo.
     */
    private VBox createLeftPane() {
        VBox leftPane = new VBox(5);
        leftPane.setPadding(new Insets(5));

        // Área de entrada
        inputArea = new TextArea();
        inputArea.setPromptText("Insira seu código assembly aqui...");
        inputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");
        ScrollPane inputScroll = new ScrollPane(inputArea);
        inputScroll.setFitToWidth(true);
        inputScroll.setFitToHeight(true);
        TitledPane inputTitled = new TitledPane("Código de Entrada", inputScroll);
        inputTitled.setCollapsible(false);

        // Área onde o código processado pelo MacroProcessor é exibido.
        macroOutArea = new TextArea();
        macroOutArea.setPromptText("Macros expandidos");
        macroOutArea.setEditable(false);
        macroOutArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: #006400;");
        ScrollPane expandedScroll = new ScrollPane(macroOutArea);
        expandedScroll.setFitToWidth(true);
        expandedScroll.setFitToHeight(true);
        TitledPane expandedTitled = new TitledPane("Código Expandido", expandedScroll);
        expandedTitled.setCollapsible(false);

        // Junta as duas áreas lado a lado
        HBox inputExpandedRow = new HBox(5, inputTitled, expandedTitled);
        HBox.setHgrow(inputTitled, Priority.ALWAYS);
        HBox.setHgrow(expandedTitled, Priority.ALWAYS);

        // Área de saída de mensagens
        outputArea = new TextArea();
        outputArea.setPromptText("Saída de mensagens...");
        outputArea.setEditable(false);
        outputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: green;");
        outputArea.setPrefHeight(250);
        TitledPane outputTitled = new TitledPane("Saída de Mensagens", outputArea);
        outputTitled.setCollapsible(false);

        // Primeira linha de botões: Montar, Carregar, Ver código expandido
        HBox simulationControls = simulationToolbar.getFileControls();

        // Segunda linha de botões: Executar, Pausar, Próximo, Resetar
        HBox executionControls = simulationToolbar.getExecutionControls();

        leftPane.getChildren().addAll(inputExpandedRow, simulationControls, outputTitled, executionControls);
        VBox.setVgrow(inputExpandedRow, Priority.ALWAYS);

        return leftPane;
    }

    /**
     * Cria o painel direito (VBox) com:
     * - Parte superior: área de código expandido e botão Reload.
     * - Parte inferior: tabelas empilhadas verticalmente (Memória, Registradores e Símbolos).
     */
    private VBox createRightPane() {
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));

        // Parte inferior: tabelas empilhadas verticalmente
        VBox tablesBox = createMemoryRegisterSymbolPane();

        // Adiciona as duas seções ao painel direito
        rightPane.getChildren().addAll(tablesBox);
        VBox.setVgrow(tablesBox, Priority.ALWAYS);

        return rightPane;
    }

    /**
     * Cria as tabelas de Memória, Registradores e Símbolos empilhadas verticalmente.
     */
    private VBox createMemoryRegisterSymbolPane() {
        memoryTable = new MemoryTableView();
        registerTable = new RegisterTableView();
        symbolTable = new SymbolTableView();

        ScrollPane memoryScroll = new ScrollPane(memoryTable);
        memoryScroll.setFitToWidth(true);
        memoryScroll.setFitToHeight(true);

        ScrollPane registerScroll = new ScrollPane(registerTable);
        registerScroll.setFitToWidth(true);
        registerScroll.setFitToHeight(true);

        //noinspection ExtractMethodRecommender
        ScrollPane symbolScroll = new ScrollPane(symbolTable);
        symbolScroll.setFitToWidth(true);
        symbolScroll.setFitToHeight(true);

        TitledPane memoryTitled = new TitledPane("Memória", memoryScroll);
        memoryTitled.setCollapsible(false);
        TitledPane registerTitled = new TitledPane("Registradores", registerScroll);
        registerTitled.setCollapsible(false);
        TitledPane symbolTitled = new TitledPane("Símbolos", symbolScroll);
        symbolTitled.setCollapsible(false);

        // Define alturas preferenciais para que cada painel fique menor
        memoryTitled.setPrefHeight(150);
        registerTitled.setPrefHeight(150);
        symbolTitled.setPrefHeight(150);

        VBox tablesBox = new VBox(10, memoryTitled, registerTitled, symbolTitled);
        tablesBox.setAlignment(Pos.TOP_LEFT);
        return tablesBox;
    }

    /**
     * Cria a barra inferior de status.
     */
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

    /**
     * Cria o menu superior.
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        /*
          Menu "Arquivo" com opções de importar/exportar arquivos .ASM e .OBJ.
         */
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

        /*
            Menu "Códigos Exemplo" com opções de carregar códigos de exemplo.
         */
        Menu sampleMenu = new Menu("Códigos Exemplo");
        MenuItem sample1 = new MenuItem("Carregar código exemplo 1");
        sample1.setOnAction(e -> {
            try {
                controller.handleLoadSampleCodeAction(SampleCodes.SAMPLE_CODE_1, "Simulador SIC/XE - Exemplo 1");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem sample2 = new MenuItem("Carregar código exemplo 2");
        sample2.setOnAction(e -> {
            try {
                controller.handleLoadSampleCodeAction(SampleCodes.SAMPLE_CODE_2, "Simulador SIC/XE - Exemplo 2");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem sample3 = new MenuItem("Carregar código exemplo 3");
        sample3.setOnAction(e -> {
            try {
                controller.handleLoadSampleCodeAction(SampleCodes.SAMPLE_CODE_3, "Simulador SIC/XE - Exemplo 3");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem sample4 = new MenuItem("Carregar código exemplo 4");
        sample4.setOnAction(e -> {
            try {
                controller.handleLoadSampleCodeAction(SampleCodes.SAMPLE_CODE_4, "Simulador SIC/XE - Exemplo 4");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem sample5 = new MenuItem("Carregar código exemplo 5");
        sample5.setOnAction(e -> {
            try {
                controller.handleLoadSampleCodeAction(SampleCodes.SAMPLE_CODE_5, "Simulador SIC/XE - Exemplo 5");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        MenuItem sample6 = new MenuItem("Carregar código exemplo 6");
        sample6.setOnAction(e -> {
            try {
                controller.handleLoadSampleCodeAction(SampleCodes.SAMPLE_CODE_6, "Simulador SIC/XE - Exemplo 6");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        sampleMenu.getItems().addAll(sample1, sample2, sample3, sample4, sample5, sample6);

        /*
            Menu "Memória" com opções de limpar a memória e alterar o tamanho da memória.
         */
        Menu memoryMenu = new Menu("Memória");
        MenuItem clearMemoryItem = new MenuItem("Limpar Memória");
        clearMemoryItem.setOnAction(e -> controller.handleClearMemoryAction());
        MenuItem changeMemorySizeItem = new MenuItem("Tamanho da memória");
        changeMemorySizeItem.setOnAction(e -> showMemorySizeDialog());
        memoryMenu.getItems().addAll(changeMemorySizeItem, clearMemoryItem);

        /*
            Menu "Execução" com opções de alterar a velocidade de execução.
         */
        Menu executionMenu = new Menu("Execução");
        MenuItem executionSpeedItem = new MenuItem("Velocidade de execução");
        executionSpeedItem.setOnAction(e -> showExecutionSpeedDialog());
        executionMenu.getItems().add(executionSpeedItem);

        /*
            Menu "Exibição" com opções de alterar o formato de exibição dos endereços.
         */
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

        /*
            Menu "Ajuda" com opções de ajuda e tutorial.
         */
        Menu helpMenu = new Menu("Ajuda");
        MenuItem helpItem = new MenuItem("Ajuda e Tutorial");
        helpItem.setOnAction(e -> controller.handleHelpAction());
        helpMenu.getItems().add(helpItem);

        /*
            Menu "Sobre" com opções de informações sobre o projeto e repositório.
         */
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

        /*
            Menu "Créditos" com informações sobre os desenvolvedores.
         */
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

        /*
            Adiciona todos os menus à barra de menus.
         */
        menuBar.getMenus().addAll(fileMenu, sampleMenu, memoryMenu, executionMenu, viewMenu, helpMenu, aboutMenu, creditsMenu);

        return menuBar;
    }

    /**
     * Cria uma mensagem de boas-vindas para o usuário.
     */
    private void showWelcomeMessage() {
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
        // Exibe cada linha na área de código expandido (ou na saída de status)
        Arrays.stream(welcomeMessage.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .forEach(this::appendOutput);
    }

    /**
     * Exibe um diálogo para alterar o tamanho da memória.
     */
    public void showMemorySizeDialog() {
        TextInputDialog dialog = new TextInputDialog(controller.getMemorySize()+ "bytes");
        dialog.setTitle("Alterar Tamanho da Memória");
        dialog.setHeaderText("Defina o tamanho da memória");
        dialog.setContentText("Digite um número inteiro positivo:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int newSize = Integer.parseInt(input);
                if (newSize <= 0) {
                    throw new NumberFormatException("Valor deve ser maior que zero.");
                }
                controller.handleChangeMemorySizeAction(newSize);
                memorySizeLabel.setText("Memória: " + newSize + " bytes");
                appendOutput("Tamanho da memória alterado para: " + newSize + " bytes.");
            } catch (NumberFormatException ex) {
                showError("Valor inválido! Por favor, insira um número inteiro positivo.");
            }
        });
    }

    /**
     * @return A ListView de arquivos de objeto
     */
    public ListView<String> getObjectFileListView() {
        return objectFileListView;
    }


    ///  Getters de TextAreas

    public TextArea getInputField() {
        return inputArea;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public TextArea getExpandedArea() {
        return macroOutArea;
    }

    public void appendOutput(String message) {
        Platform.runLater(() -> outputArea.appendText("> " + message + "\n"));
    }

    public void clearOutputArea() {
        outputArea.clear();
    }

    public void clearMacroOutArea() {
        macroOutArea.clear();
    }

    public void disableControls() {
        simulationToolbar.disableExecutionButtons();
    }

    public void enableControls() {
        simulationToolbar.enableExecutionButtons();
    }

    /**
     * Atualiza todas as tabelas de memória, registradores e símbolos.
     */
    public void updateAllTables() {
        Platform.runLater(() -> {
            updateMemoryTable();
            updateRegisterTable();
            updateSymbolTable();
        });
    }

    /**
     * Atualiza a tabela de memória com os valores atuais.
     */
    public void updateMemoryTable() {
        memoryTable.getItems().clear();
        List<MemoryEntry> entries = controller.getMemoryEntries();
        memoryTable.getItems().addAll(entries);
    }


    /**
     * Atualiza a tabela de registradores com os valores atuais.
     */
    public void updateRegisterTable() {
        registerTable.getItems().clear();
        List<RegisterEntry> entries = controller.getRegisterEntries();
        registerTable.getItems().addAll(entries);
    }

    /**
     * Atualiza a tabela de símbolos com os valores atuais.
     */
    public void updateSymbolTable() {
        symbolTable.getItems().clear();
        List<SymbolEntry> entries = controller.getSymbolEntries();
        symbolTable.getItems().addAll(entries);
    }


    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Simulação");
            alert.setHeaderText("Ocorreu um erro durante a execução");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void showHelpWindow() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Ajuda - Funcionalidades e Tutorial");
        helpAlert.setHeaderText("Funcionalidades, Comandos e Tutorial");
        helpAlert.setContentText("WIP");
        helpAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        helpAlert.showAndWait();
    }

    public void setViewFormat(String format) {
        viewConfig.setAddressFormat(format);
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

    public void updateAllLabels() {
        updateViewFormatLabel();
        updateCycleDelayLabel();
        updateMemorySizeLabel();
    }

    public Stage getStage() {
        return primaryStage;
    }

    public void setWindowTitle(String title) {
        Platform.runLater(() -> primaryStage.setTitle(title));
    }

    public void clearTables() {
        memoryTable.getItems().clear();
        registerTable.getItems().clear();
        symbolTable.getItems().clear();
    }

    public RegisterTableView getRegisterTable() {
        return registerTable;
    }

    public MemoryTableView getMemoryTable() {
        return memoryTable;
    }

    public SymbolTableView getSymbolTable() {
        return symbolTable;
    }

    public void showExecutionSpeedDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Tempo real", "Rápido", "Médio", "Lento", "Muito lento");
        dialog.setTitle("Velocidade de Execução");
        dialog.setHeaderText("Selecione a velocidade de execução:");
        dialog.setContentText("Velocidade:");
        dialog.showAndWait().ifPresent(selected -> {
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

    public static void main(String[] args) {
        launch(args);
    }
}
