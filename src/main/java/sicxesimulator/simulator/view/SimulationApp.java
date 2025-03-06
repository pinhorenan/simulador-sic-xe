package sicxesimulator.simulator.view;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sicxesimulator.simulator.controller.SimulationController;
import sicxesimulator.simulator.model.SimulationModel;
import sicxesimulator.machine.Machine;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.loader.Loader;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.ValueFormatter;
import sicxesimulator.simulator.view.components.*;
import sicxesimulator.utils.ViewConfig;
import java.util.*;

public class SimulationApp extends Application implements SimulationView {
    private SimulationController controller;
    private Stage primaryStage;

    // Componentes da UI
    private InputOutputPane inputOutputPane;
    private SimulationToolbar toolbar;
    private RegisterTableView registerTable;
    private MemoryTableView memoryTable;
    private SymbolTableView symbolTable;

    // Configurações
    private ViewConfig viewConfig;

    // Labels da barra inferior
    private Label executionSpeedLabel;
    private Label memorySizeLabel;
    private Label viewFormatLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Inicialização com injeção de dependência
        Machine machine = new Machine();
        SimulationModel model = new SimulationModel(machine, new Assembler(), new Loader(machine));
        controller = new SimulationController(model, this);
        this.viewConfig = model.getViewConfig();
        viewConfig.addFormatChangeListener(newFormat -> {
            updateMemoryTable();
            updateRegisterTable();
            updateSymbolTable();
        });


        // Configurar UI
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(createMainContent());
        root.setBottom(createBottomBar());

        // Configuração da cena
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulador SIC/XE");
        primaryStage.show();

        configureStageProperties();
        initializeUI();
    }

    private void configureStageProperties() {
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        // As propriedades mínimas são definidas separadamente para largura e altura
    }

    private void initializeUI() {
        showWelcomeMessage();
        updateAllTables();
    }

    // Implementação da Interface SimulationView
    @Override
    public void updateAllTables() {
        Platform.runLater(() -> {
            updateRegisterTable();
            updateMemoryTable();
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
            String formattedAddress = ValueFormatter.formatAddress(
                    byteAddress,
                    viewConfig.getAddressFormat()
            );
            memoryTable.getItems().add(new MemoryEntry(formattedAddress, Convert.bytesToHex(word)));
        }
    }

    @Override
    public void updateRegisterTable() {
        registerTable.getItems().clear();
        Collection<Register> registers = controller.getSimulationModel()
                .getMachine()
                .getControlUnit()
                .getRegisterSet()
                .getAllRegisters();

        for (Register reg : registers) {
            String value = ValueFormatter.formatRegisterValue(reg);
            registerTable.getItems().add(new RegisterEntry(reg.getName(), value));
        }
    }

    @Override
    public void updateSymbolTable() {
        symbolTable.getItems().clear();
        if (controller.getSimulationModel().hasAssembledCode()) {
            Map<String, Integer> symbols = controller.getSimulationModel()
                    .getLastObjectFile()
                    .getSymbolTable()
                    .getSymbols();

            symbols.forEach((name, wordAddress) -> {
                int byteAddress = wordAddress * 3;
                String formattedAddress = ValueFormatter.formatAddress(
                        byteAddress,
                        viewConfig.getAddressFormat()
                );
                symbolTable.getItems().add(new SymbolEntry(name, formattedAddress));
            });
        }
    }

    @Override
    public void appendOutput(String message) {
        String processedMessage = ValueFormatter.processAddresses(message);
        Platform.runLater(() ->
                inputOutputPane.getOutputArea().appendText("> " + processedMessage + "\n")
        );
    }

    @Override
    public void clearOutput() {
        inputOutputPane.getOutputArea().clear();
    }

    @Override
    public String getInputText() {
        // Retorna o texto do campo de entrada do InputOutputPane
        return inputOutputPane.getInputField().getText();
    }

    @Override
    public void clearInput() {
        inputOutputPane.getInputField().clear();
    }

    @Override
    public void clearTables() {
        registerTable.getItems().clear();
        memoryTable.getItems().clear();
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
        String helpText = """
            // Conteúdo do help...
            """;
        helpAlert.setContentText(helpText);
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
        Platform.runLater(() ->
                viewFormatLabel.setText("Formato: " + formatName)
        );
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
    public void disableControls(boolean disable) {
        Platform.runLater(() -> toolbar.disableControls(disable));
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

    // Métodos auxiliares

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

        // Append imediato, sem PauseTransition
        Arrays.stream(welcomeMessage.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .forEach(this::appendOutput);
    }

    public void showExecutionSpeedDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Tempo real", "Rápido", "Médio", "Lento", "Muito lento");
        dialog.setTitle("Tempo real");
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

    public void showMemorySizeDialog() {
        TextInputDialog dialog = new TextInputDialog("1024");
        dialog.setTitle("Alterar Tamanho da Memória");
        dialog.setHeaderText("Defina o tamanho da memória");
        dialog.setContentText("Digite um número inteiro positivo:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int newSize = Integer.parseInt(input);
                if (newSize <= 0) throw new NumberFormatException("Valor deve ser maior que zero.");
                controller.handleChangeMemorySizeAction(newSize);
                memorySizeLabel.setText("Memória: " + newSize + " bytes");
                appendOutput("Tamanho da memória alterado para: " + newSize + " bytes.");
            } catch (NumberFormatException ex) {
                showError("Valor inválido! Por favor, insira um número inteiro positivo.");
            }
        });
    }

    public TextArea getOutputArea() {
        return inputOutputPane.getOutputArea();
    }

    public TextArea getInputField() {
        return inputOutputPane.getInputField();
    }

    // Criação de componentes
    private HBox createMainContent() {
        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();
        HBox mainContent = new HBox(10, leftPanel, rightPanel);
        mainContent.setPadding(new Insets(0));
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        return mainContent;
    }

    private VBox createLeftPanel() {
        inputOutputPane = new InputOutputPane();
        VBox.setVgrow(inputOutputPane, Priority.ALWAYS);
        toolbar = new SimulationToolbar(controller);

        VBox leftPanel = new VBox(10, inputOutputPane, toolbar);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setAlignment(Pos.CENTER);
        return leftPanel;
    }

    private VBox createRightPanel() {
        registerTable = new RegisterTableView();
        memoryTable = new MemoryTableView();
        symbolTable = new SymbolTableView();

        ScrollPane memoryScrollPane = wrapInScrollPane(memoryTable);
        ScrollPane registerScrollPane = wrapInScrollPane(registerTable);
        ScrollPane symbolScrollPane = wrapInScrollPane(symbolTable);

        VBox.setVgrow(memoryScrollPane, Priority.ALWAYS);
        VBox.setVgrow(registerScrollPane, Priority.ALWAYS);
        VBox.setVgrow(symbolScrollPane, Priority.ALWAYS);

        TitledPane memoryTitled = new TitledPane("Memória", memoryScrollPane);
        TitledPane registerTitled = new TitledPane("Registradores", registerScrollPane);
        TitledPane symbolTitled = new TitledPane("Símbolos", symbolScrollPane);

        memoryTitled.setCollapsible(false);
        registerTitled.setCollapsible(false);
        symbolTitled.setCollapsible(false);

        VBox rightPanel = new VBox(10, memoryTitled, registerTitled, symbolTitled);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setAlignment(Pos.CENTER);
        VBox.setVgrow(rightPanel, Priority.ALWAYS);
        return rightPanel;
    }

    private ScrollPane wrapInScrollPane(TableView<?> table) {
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-padding: 0;");
        return scrollPane;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("Arquivo");
        MenuItem loadExampleASM = new MenuItem("Carregar código exemplo");
        loadExampleASM.setOnAction(e -> controller.handleLoadSampleCodeAction());
        fileMenu.getItems().add(loadExampleASM);

        Menu optionsMenu = new Menu("Opções");
        MenuItem memorySizeItem = new MenuItem("Tamanho da memória");
        memorySizeItem.setOnAction(e -> showMemorySizeDialog());
        MenuItem executionSpeedItem = new MenuItem("Velocidade de execução");
        executionSpeedItem.setOnAction(e -> showExecutionSpeedDialog());
        optionsMenu.getItems().addAll(memorySizeItem, executionSpeedItem);

        Menu viewMenu = new Menu("Exibição");
        MenuItem hexadecimalView = new MenuItem("Hexadecimal");
        hexadecimalView.setOnAction(e -> controller.handleHexViewAction());
        MenuItem octalView = new MenuItem("Octal");
        octalView.setOnAction(e -> controller.handleOctalViewAction());
        MenuItem decimalView = new MenuItem("Decimal");
        decimalView.setOnAction(e -> controller.handleDecimalViewAction());
        viewMenu.getItems().addAll(hexadecimalView, octalView, decimalView);

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
                    Ícone: https://icons8.com/icon/NAL2lztANaO6/rust""");
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

    public static void main(String[] args) {
        launch(args);
    }

    // Records para as tabelas
    public record RegisterEntry(String name, String value) {}
    public record MemoryEntry(String address, String value) {}
    public record SymbolEntry(String symbol, String address) {}

    // LOGGING

    public void generateStateLog() {
        String filename = "simulator_log.txt";
        SimulationModel model = controller.getSimulationModel();

        try (FileWriter writer = new FileWriter(filename, true)) {
            // Cabeçalho com timestamp
            writer.write("\n=== Log em: " +
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " ===\n");

            // Seção de Memória
            // --- Memória (dados reais) ---
            writer.write("\n--- Memória (valores não zero) ---\n");
            model.getMachine().getMemory().getMemoryMap().forEach((address, value) -> {
                if (value != 0) { // Filtra zeros
                    String formattedAddr = ValueFormatter.formatAddress(
                            address,
                            viewConfig.getAddressFormat()
                    );
                    String formattedValue = ValueFormatter.formatByte(
                            value,
                            viewConfig.getAddressFormat()
                    );
                    try {
                        writer.write(String.format("%-8s | %s\n", formattedAddr, formattedValue));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            // Seção de Registradores
            writer.write("\n--- Registradores ---\n");
            model.getMachine().getControlUnit().getRegisterSet().getAllRegisters().forEach(reg -> {
                String formattedValue = ValueFormatter.formatRegisterValue(reg);
                try {
                    writer.write(String.format("%-10s | %s\n", reg.getName(), formattedValue));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            // Seção de Símbolos
            if (model.hasAssembledCode()) {
                writer.write("\n--- Tabela de Símbolos ---\n");
                model.getLastObjectFile().getSymbolTable().getSymbols().forEach((name, address) -> {
                    String formattedAddr = ValueFormatter.formatAddress(
                            address * 3, // Converte endereço de palavra para byte
                            viewConfig.getAddressFormat()
                    );
                    try {
                        writer.write(String.format("%-15s | %s\n", name, formattedAddr));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            writer.write("\n".repeat(2));  // Espaçamento final
        } catch (IOException e) {
            showError("Erro ao gerar log: " + e.getMessage());
        }
    }
}
