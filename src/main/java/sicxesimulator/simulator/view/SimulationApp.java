package sicxesimulator.simulator.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.loader.Loader;
import sicxesimulator.machine.Machine;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.simulator.controller.SimulationController;
import sicxesimulator.simulator.model.SampleCodes;
import sicxesimulator.simulator.model.SimulationModel;
import sicxesimulator.simulator.view.components.MemoryTableView;
import sicxesimulator.simulator.view.components.RegisterTableView;
import sicxesimulator.simulator.view.components.SymbolTableView;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.ValueFormatter;
import sicxesimulator.utils.ViewConfig;

import java.io.IOException;
import java.util.*;

public class SimulationApp extends Application implements SimulationView {
    private SimulationController controller;
    private Stage primaryStage;
    protected SimulationToolbar simulationToolbar;

    // Quadrante esquerdo
    private TextArea inputArea;
    private TextArea outputArea;
    private TextArea expandedArea;

    // Tabelas
    private MemoryTableView memoryTable;
    private RegisterTableView registerTable;
    private SymbolTableView symbolTable;

    // Configurações e labels de status
    private ViewConfig viewConfig;
    private Label executionSpeedLabel;
    private Label memorySizeLabel;
    private Label viewFormatLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Machine machine = new Machine();
        SimulationModel model = new SimulationModel(machine, new Assembler(), new Loader(machine));
        controller = new SimulationController(model, this);
        viewConfig = model.getViewConfig();
        viewConfig.addFormatChangeListener(newFormat -> updateAllTables());
        simulationToolbar = new SimulationToolbar(controller, this);

        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(createMainContent());   // Layout principal: divisão left/right
        root.setBottom(createBottomBar());

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

    private void initializeUI() {
        disableControls();
        showWelcomeMessage();
        updateAllTables();
        updateViewFormatLabel(viewConfig.getAddressFormat());
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

        // Área de código expandido
        expandedArea = new TextArea();
        expandedArea.setPromptText("Código expandido...");
        expandedArea.setEditable(false);
        expandedArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: #006400;");
        ScrollPane expandedScroll = new ScrollPane(expandedArea);
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

        Menu fileMenu = new Menu("Arquivo");
        Menu sampleMenu = new Menu("Exemplos");
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
        fileMenu.getItems().add(sampleMenu);

        Menu optionsMenu = new Menu("Opções");
        MenuItem memorySizeItem = new MenuItem("Tamanho da memória");
        memorySizeItem.setOnAction(e -> showMemorySizeDialog());
        MenuItem executionSpeedItem = new MenuItem("Velocidade de execução");
        executionSpeedItem.setOnAction(e -> showExecutionSpeedDialog());
        optionsMenu.getItems().addAll(memorySizeItem, executionSpeedItem);

        Menu viewMenu = new Menu("Exibição");
        MenuItem hexView = new MenuItem("Hexadecimal");
        hexView.setOnAction(e -> controller.handleHexViewAction());
        MenuItem octView = new MenuItem("Octal");
        octView.setOnAction(e -> controller.handleOctalViewAction());
        MenuItem decView = new MenuItem("Decimal");
        decView.setOnAction(e -> controller.handleDecimalViewAction());
        MenuItem binView = new MenuItem("Binário");
        binView.setOnAction(e -> controller.handleBinaryViewAction());
        viewMenu.getItems().addAll(hexView, octView, decView, binView);

        Menu helpMenu = new Menu("Ajuda");
        MenuItem helpItem = new MenuItem("Ajuda e Tutorial");
        helpItem.setOnAction(e -> controller.handleHelpAction());
        helpMenu.getItems().add(helpItem);

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
                    Ícone: https://icons8.com/icon/NAL2lztANaO6/rust
                    """);
            alert.showAndWait();
        });
        aboutMenu.getItems().addAll(info, repository);

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

        menuBar.getMenus().addAll(fileMenu, optionsMenu, viewMenu, helpMenu, aboutMenu, creditsMenu);
        return menuBar;
    }

    // ----------------------------------------------------------------
    // Métodos da interface SimulationView
    // ----------------------------------------------------------------

    @Override
    public String getInputText() {
        return inputArea.getText();
    }

    @Override
    public TextArea getInputField() {
        return inputArea;
    }

    @Override
    public TextArea getOutputArea() {
        return outputArea;
    }

    public TextArea getExpandedArea() {
        return expandedArea;
    }

    @Override
    public void appendOutput(String message) {
        Platform.runLater(() -> outputArea.appendText("> " + message + "\n"));
    }

    @Override
    public void clearOutput() {
        outputArea.clear();
    }

    public void clearExpandedCode() {
        expandedArea.clear();
    }

    @Override
    public void clearInput() {
        inputArea.clear();
    }

    @Override
    public void disableControls() {
        simulationToolbar.disableExecutionButtons();
    }

    @Override
    public void enableControls() {
        simulationToolbar.enableExecutionButtons();
    }

    @Override
    public void fullReset() {
        controller.getSimulationModel().reset();
        clearInput();
        clearOutput();
        clearTables();
        updateAllTables();
        setWindowTitle("Simulador SIC/XE");
    }

    @Override
    public void updateAllTables() {
        Platform.runLater(() -> {
            updateMemoryTable();
            updateRegisterTable();
            updateSymbolTable();
        });
    }

    @Override
    public void updateMemoryTable() {
        memoryTable.getItems().clear();
        var memory = controller.getSimulationModel().getMachine().getMemory();
        for (int wordIndex = 0; wordIndex < memory.getAddressRange(); wordIndex++) {
            byte[] word = memory.readWord(wordIndex);
            int byteAddress = wordIndex * 3;
            String formattedAddress = ValueFormatter.formatAddress(byteAddress, viewConfig.getAddressFormat());
            memoryTable.getItems().add(new MemoryEntry(formattedAddress, Convert.bytesToHex(word)));
        }
    }

    @Override
    public void updateRegisterTable() {
        registerTable.getItems().clear();
        var regs = controller.getSimulationModel().getMachine().getControlUnit().getRegisterSet().getAllRegisters();
        for (Register r : regs) {
            String value = ValueFormatter.formatRegisterValue(r, viewConfig.getAddressFormat());
            registerTable.getItems().add(new RegisterEntry(r.getName(), value));
        }
    }

    @Override
    public void updateSymbolTable() {
        symbolTable.getItems().clear();
        if (controller.getSimulationModel().hasAssembledCode()) {
            var symbols = controller.getSimulationModel().getLastObjectFile().getSymbolTable().getSymbols();
            symbols.forEach((name, wordAddress) -> {
                int byteAddr = wordAddress * 3;
                String formattedAddress = ValueFormatter.formatAddress(byteAddr, viewConfig.getAddressFormat());
                symbolTable.getItems().add(new SymbolEntry(name, formattedAddress));
            });
        }
    }

    @Override
    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Simulação");
            alert.setHeaderText("Ocorreu um erro durante a execução");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    @Override
    public void showHelpWindow() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Ajuda - Funcionalidades e Tutorial");
        helpAlert.setHeaderText("Funcionalidades, Comandos e Tutorial");
        helpAlert.setContentText("// Conteúdo do help...");
        helpAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        helpAlert.showAndWait();
    }

    @Override
    public void setViewFormat(String format) {
        viewConfig.setAddressFormat(format);
        updateViewFormatLabel(format);
    }

    @Override
    public void updateViewFormatLabel(String formatName) {
        Platform.runLater(() -> viewFormatLabel.setText("Formato: " + formatName));
    }

    @Override
    public void updateCycleDelayLabel() {
        Platform.runLater(() -> executionSpeedLabel.setText("Atraso de ciclo: " + controller.getSimulationModel().getCycleDelay()));
    }

    @Override
    public void updateMemorySizeLabel() {
        Platform.runLater(() -> memorySizeLabel.setText("Memória: " + controller.getSimulationModel().getMachine().getMemorySize() + " bytes"));
    }

    @Override
    public Stage getStage() {
        return primaryStage;
    }

    @Override
    public void setWindowTitle(String title) {
        Platform.runLater(() -> primaryStage.setTitle(title));
    }

    @Override
    public void clearTables() {
        memoryTable.getItems().clear();
        registerTable.getItems().clear();
        symbolTable.getItems().clear();
    }

    @Override
    public TableView<RegisterEntry> getRegisterTable() {
        return registerTable;
    }

    @Override
    public TableView<MemoryEntry> getMemoryTable() {
        return memoryTable;
    }

    @Override
    public TableView<SymbolEntry> getSymbolTable() {
        return symbolTable;
    }

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

    public void showMemorySizeDialog() {
        TextInputDialog dialog = new TextInputDialog("1024");
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


    // -------------------------------------------------------------
    // Estruturas auxiliares para as tabelas
    // -------------------------------------------------------------
    public record RegisterEntry(String name, String value) {}
    public record MemoryEntry(String address, String value) {}
    public record SymbolEntry(String symbol, String address) {}

    public static void main(String[] args) {
        launch(args);
    }
}
