package sicxesimulator.simulator.view;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import sicxesimulator.simulator.controller.SimulationController;
import sicxesimulator.simulator.model.SimulationModel;
import sicxesimulator.machine.Machine;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.loader.Loader;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.ValueFormatter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class SimulationApp extends Application {
    private SimulationController controller;

    private Stage primaryStage;
    private TextArea outputArea;
    private TextArea inputField;
    private TableView<RegisterEntry> registerTable;
    private TableView<MemoryEntry> memoryTable;
    private TableView<SymbolEntry> symbolTable;
    private String viewFormat = "HEX";
    private boolean darkModeEnabled = false;

    // Records auxiliares para as tabelas
    public record RegisterEntry(String name, String value) { }
    public record MemoryEntry(String address, String value) { }
    public record SymbolEntry(String symbol, String address) { }

    // Para poder exibir valores atuais
    private Label executionSpeedLabel;
    private Label memorySizeLabel;
    private Label viewFormatLabel;

    /// CRIAÇÃO DE COMPONENTES DA INTERFACE

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menu "Arquivo"
        Menu fileMenu = new Menu("Arquivo");

        MenuItem loadExampleASM = new MenuItem("Carregar código exemplo");
        loadExampleASM.setOnAction(e -> controller.handleLoadSampleCodeAction());

        fileMenu.getItems().add(loadExampleASM);

        // Menu "Configurações"
        Menu optionsMenu = new Menu("Opções");

        MenuItem memorySizeItem = new MenuItem("Tamanho da memória");
        memorySizeItem.setOnAction(e -> showMemorySizeDialog());

        MenuItem executionSpeedItem = new MenuItem("Velocidade de execução");
        executionSpeedItem.setOnAction(e -> showExecutionSpeedDialog());

        optionsMenu.getItems().addAll(memorySizeItem, executionSpeedItem);

        // Menu "Exibição"
        Menu viewMenu = new Menu("Exibição");

        MenuItem hexadecimalView = new MenuItem("Hexadecimal");
        hexadecimalView.setOnAction(e -> controller.handleHexViewAction());

        MenuItem octalView = new MenuItem("Octal");
        octalView.setOnAction(e -> controller.handleOctalViewAction());

        MenuItem decimalView = new MenuItem("Decimal");
        decimalView.setOnAction(e -> controller.handleDecimalViewAction());

        MenuItem darkModeItem = new MenuItem("Dark Mode");
        darkModeItem.setOnAction(e -> toggleDarkMode());

        viewMenu.getItems().addAll(hexadecimalView, octalView, decimalView, darkModeItem);

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
            alert.setHeaderText("SIC/XE Simulator v2.1");
            alert.setContentText("""
                    © 2025 SIC/XEd
                    Time ROCK LEE VS GAARA
                    Ícone: https://icons8.com/icon/NAL2lztANaO6/rust""");
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

        menuBar.getMenus().addAll(fileMenu, optionsMenu, viewMenu, helpMenu, aboutMenu, creditsMenu);
        return menuBar;
    }

    private ScrollPane createInputArea() {
        inputField = new TextArea();
        inputField.setPromptText("Insira seu código assembly aqui...");
        inputField.setWrapText(true);
        inputField.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");
        ScrollPane inputScroll = new ScrollPane(inputField);
        inputScroll.setFitToWidth(true);
        inputScroll.setFitToHeight(true);
        return inputScroll;
    }

    private ScrollPane createOutputAreaPane() {
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setStyle("-fx-font-family: monospace; -fx-text-fill: #006400; -fx-font-size: 14;");
        ScrollPane outputScroll = new ScrollPane(outputArea);
        outputScroll.setFitToWidth(true);
        outputScroll.setFitToHeight(true);
        return outputScroll;
    }

    private ScrollPane createRegisterTablePane() {
        setupRegisterTable();
        ScrollPane registerScroll = new ScrollPane(registerTable);
        registerScroll.setFitToWidth(true);
        return registerScroll;
    }

    private ScrollPane createMemoryTablePane() {
        setupMemoryTable();
        ScrollPane memoryScroll = new ScrollPane(memoryTable);
        memoryScroll.setFitToWidth(true);
        return memoryScroll;
    }

    private ScrollPane createSymbolTablePane() {
        setupSymbolTable();
        ScrollPane symbolScroll = new ScrollPane(symbolTable);
        symbolScroll.setFitToWidth(true);
        return symbolScroll;
    }

    private void createBottomBar(BorderPane root) {
        executionSpeedLabel = new Label("Atraso de ciclo: Tempo Real");
        memorySizeLabel = new Label("Memória: 1024 bytes");
        viewFormatLabel = new Label("Formato: Hexadecimal");

        HBox bottomBar = new HBox(20, executionSpeedLabel, memorySizeLabel, viewFormatLabel);
        bottomBar.setPadding(new Insets(10));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle("-fx-background-color: #EEE; -fx-border-color: #CCC; -fx-padding: 5px;");

        root.setBottom(bottomBar);
    }

    /// SETUP E ATUALIZAÇÃO DAS TABELAS

    private void setupRegisterTable() {
        registerTable = new TableView<>();
        TableColumn<RegisterEntry, String> nameCol = new TableColumn<>("Registrador");
        nameCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().name()));
        TableColumn<RegisterEntry, String> valueCol = new TableColumn<>("Valor");
        valueCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().value()));
        registerTable.getColumns().addAll(nameCol, valueCol);
        registerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupMemoryTable() {
        memoryTable = new TableView<>();
        TableColumn<MemoryEntry, String> addressCol = new TableColumn<>("Endereço");
        addressCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().address()));
        TableColumn<MemoryEntry, String> valueCol = new TableColumn<>("Valor");
        valueCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().value()));
        memoryTable.getColumns().addAll(addressCol, valueCol);
        memoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupSymbolTable() {
        symbolTable = new TableView<>();
        TableColumn<SymbolEntry, String> symbolCol = new TableColumn<>("Símbolo");
        symbolCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().symbol()));
        TableColumn<SymbolEntry, String> addressCol = new TableColumn<>("Endereço");
        addressCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().address()));
        symbolTable.getColumns().addAll(symbolCol, addressCol);
        symbolTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /// ATUALIZAÇÃO DAS TABELAS

    public void updateRegisterTable() {
        registerTable.getItems().clear();

        // Obtém a lista de registradores da unidade de controle
        Collection<Register> registers = controller.getSimulationModel()
                .getMachine()
                .getControlUnit()
                .getRegisterSet()
                .getAllRegisters();

        for (Register reg : registers) {
            String value;
            String regName = reg.getName().toUpperCase();

            // Formatação especial para registradores específicos
            if ("PC".equals(regName)) {
                // PC é um endereço de byte - 24 bits (6 dígitos hex)
                value = String.format("%06X", reg.getIntValue());
            }
            else if ("F".equals(regName)) {
                // Registrador de ponto flutuante - 48 bits (12 dígitos hex)
                value = String.format("%012X", reg.getLongValue());
            }
            else {
                // Demais registradores - 24 bits (6 dígitos hex)
                value = String.format("%06X", reg.getIntValue());
            }

            registerTable.getItems().add(new RegisterEntry(reg.getName(), value));
        }
    }

    public void updateMemoryTable() {
        memoryTable.getItems().clear();
        var memory = controller.getSimulationModel().getMachine().getMemory();

        for (int wordIndex = 0; wordIndex < memory.getAddressRange(); wordIndex++) {
            byte[] word = memory.readWord(wordIndex);
            int byteAddress = wordIndex * 3; // Converte para bytes
            String formattedAddress = ValueFormatter.formatAddress(byteAddress, viewFormat);
            String value = Convert.bytesToHex(word); // Método auxiliar para converter 3 bytes para hex
            memoryTable.getItems().add(new MemoryEntry(formattedAddress, value));
        }
    }

    public void updateSymbolTable() {
        // Verifica se o symbolTable é nulo
        if (symbolTable == null) {
            return; // Sai do método se symbolTable for nulo
        }

        // Verifica se o controller e o simulationModel não são nulos
        if (controller != null && controller.getSimulationModel() != null && controller.getSimulationModel().hasAssembledCode()) {
            // Limpa a tabela de símbolos
            symbolTable.getItems().clear();

            // Verifica se o último arquivo objeto e sua tabela de símbolos não são nulos
            if (controller.getSimulationModel().getLastObjectFile() != null &&
                    controller.getSimulationModel().getLastObjectFile().getSymbolTable() != null) {

                // Obtém os símbolos
                Map<String, Integer> symbols = controller.getSimulationModel().getLastObjectFile().getSymbolTable().getSymbols();

                // Itera sobre os símbolos e adiciona à tabela
                symbols.forEach((name, wordAddress) -> {
                    int byteAddress = wordAddress * 3; // Converte para bytes
                    String formattedAddress = ValueFormatter.formatAddress(byteAddress, viewFormat);
                    symbolTable.getItems().add(new SymbolEntry(name, formattedAddress));
                });
            }
        }
    }

    public void updateAllTables() {
        updateRegisterTable();
        updateMemoryTable();
        updateSymbolTable();
    }

    /**
     * Envia texto para a caixa de saída.
     */
    public void appendOutput(String message) {
        // Usar Pattern e Matcher explicitamente para compatibilidade
        Pattern pattern = Pattern.compile("0x([0-9A-Fa-f]{1,8})"); // Captura apenas o número hex após 0x
        Matcher matcher = pattern.matcher(message);
        StringBuffer convertedMessage = new StringBuffer();

        while (matcher.find()) {
            // Converte o endereço de palavra para bytes
            int wordAddress = Integer.parseInt(matcher.group(1), 16);
            int byteAddress = wordAddress * 3;
            matcher.appendReplacement(
                    convertedMessage,
                    "0x" + Integer.toHexString(byteAddress).toUpperCase()
            );
        }
        matcher.appendTail(convertedMessage);

        String finalMessage = "> " + convertedMessage.toString() + "\n";
        Platform.runLater(() -> outputArea.appendText(finalMessage));
    }

    private void showWelcomeMessage() {
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            outputArea.appendText("╔══════════════════════════════════════╗\n");
            outputArea.appendText("║      SIC/XE Simulator v2.1           ║\n");
            outputArea.appendText("║      © 2025 SIC/XE Rock Lee vs Gaara ║\n");
            outputArea.appendText("╚══════════════════════════════════════╝\n\n");
            outputArea.appendText("Instruções:\n");
            outputArea.appendText("1. Digite o código assembly na área de edição\n");
            outputArea.appendText("2. Utilize os botões abaixo para Montar, Executar, Próximo ou Reiniciar\n");
            outputArea.appendText("3. Utilize os menus para importar/exportar, configurar, obter ajuda ou visualizar informações\n");
            outputArea.appendText("4. Visualize registradores, memória e símbolos em tempo real\n\n");
        });
        pause.play();
    }

    public void showError(String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Simulação");
            alert.setHeaderText("Ocorreu um erro durante a execução");
            alert.setContentText(errorMessage);
            alert.showAndWait();
        });
    }

    public void showExecutionSpeedDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Tempo real", "Rápido", "Médio", "Lento", "Muito lento");
        dialog.setTitle("Tempo real");
        dialog.setHeaderText("Selecione a velocidade de execução:");
        dialog.setContentText("Velocidade:");
        dialog.showAndWait().ifPresent(selected -> {
            int speedValue = switch (selected) {
                case "Tempo real" -> //noinspection DuplicateBranchesInSwitch
                        0;
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

    public void showHelpWindow() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Ajuda - Funcionalidades e Tutorial");
        helpAlert.setHeaderText("Funcionalidades, Comandos e Tutorial");
        String helpText = """
            Funcionalidades Suportadas:
              - Montar: Compila o código assembly e gera o código objeto.
              - Executar: Executa o código objeto carregado na memória.
              - Próximo: Executa o próximo ciclo de instrução.
              - Resetar: Reinicia a simulação, limpando registradores, memória e tabelas.
              - Importar .asm / .obj: Permite carregar arquivos de código assembly ou objeto.
            
            Comandos e Diretivas:
              - START: Define o endereço inicial (ex.: START 1000).
              - WORD: Define uma palavra de 3 bytes.
              - RESW: Reserva um número de palavras na memória.
              - BYTE: Define um valor literal (ex.: C'ABC' ou X'1F').
              - Instruções: LDA, ADD, STA, RSUB, etc.
            
            Tutorial:
              1. Digite seu código assembly na área de edição.
              2. Utilize o botão Montar para compilar e carregar o programa.
              3. Utilize os botões Executar/Próximo para executar os ciclos de instrução.
              4. Utilize os menus para ajustar visualização, tamanho da memória e velocidade de execução.
            """;
        helpAlert.setContentText(helpText);
        helpAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        helpAlert.showAndWait();
    }

    public void toggleDarkMode() {
        Scene scene = primaryStage.getScene();
        String darkModeStylesheet = Objects.requireNonNull(getClass().getResource("/darkmode.css")).toExternalForm();
        if (!darkModeEnabled) {
            scene.getStylesheets().add(darkModeStylesheet);
            darkModeEnabled = true;
            appendOutput("Dark Mode ativado. Essa funcionalidade ainda não foi totalmente implementada!");
        } else {
            scene.getStylesheets().remove(darkModeStylesheet);
            darkModeEnabled = false;
            appendOutput("Dark Mode desativado.");
        }
    }

    /// GETTERS

    public Stage getStage() {
        return primaryStage;
    }

    public TextArea getOutputArea() { return outputArea; }

    public TextArea getInputField() { return inputField; }

    public TableView<RegisterEntry> getRegisterTable() { return registerTable; }

    public TableView<MemoryEntry> getMemoryTable() { return memoryTable; }

    public TableView<SymbolEntry> getSymbolTable() { return symbolTable; }

    /// SETTERS

    public void setViewFormatToHex() {
        viewFormat = "HEX";
        viewFormatLabel.setText("Formato: Hexadecimal");
    }

    public void setViewFormatToDecimal() {
        viewFormat = "DEC";
        viewFormatLabel.setText("Formato: Decimal");
    }

    public void setViewFormatToOctal() {
        viewFormat = "OCT";
        viewFormatLabel.setText("Formato: Octal");
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Inicialização do modelo e controlador
        Machine machine = new Machine();
        SimulationModel model = new SimulationModel(
                machine,
                new Assembler(),
                new Loader(machine)
        );
        controller = new SimulationController(model, this);

        primaryStage.setTitle("SIC/XE Simulator v2.1");
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.getIcons().add(new Image("https://img.icons8.com/?size=100&id=rd2k11wyt7We&format=png&color=000000"));

        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());
        root.setPadding(new Insets(0, 0, 10, 0));

        /// Coluna Esquerda: entrada, botões de ação e saída
        VBox leftColumn = new VBox(10);
        leftColumn.setPadding(new Insets(10));

        TitledPane inputPane = new TitledPane("Código Assembly", createInputArea());
        inputPane.setCollapsible(false);
        VBox.setVgrow(inputPane, Priority.NEVER);

        HBox actionButtons = new HBox(10);
        actionButtons.setPadding(new Insets(5));
        VBox.setVgrow(actionButtons, Priority.NEVER);

        Button assembleButton = new Button("Montar");
        assembleButton.setOnAction(e -> {
            String inputText = inputField.getText();
            if (!inputText.trim().isEmpty()) {
                List<String> sourceLines = Arrays.asList(inputText.split("\\r?\\n"));
                controller.handleAssembleAction(sourceLines);
            }
        });

        Button showObjectCodeButton = new Button("Mostrar Código Objeto");
        showObjectCodeButton.setOnAction(e -> controller.handleShowObjectCodeAction());

        Button runButton = new Button("Executar");
        runButton.setOnAction(e -> controller.handleRunAction());

        Button pauseButton = new Button("Pausar");
        pauseButton.setOnAction(e -> controller.handlePauseAction());

        Button nextButton = new Button("Próximo");
        nextButton.setOnAction(e -> controller.handleNextAction());

        Button resetButton = new Button("Resetar");
        resetButton.setOnAction(e -> controller.handleResetAction());

        actionButtons.getChildren().addAll(assembleButton, showObjectCodeButton, runButton, pauseButton, nextButton, resetButton);

        ScrollPane outputScroll = createOutputAreaPane();
        outputScroll.setFitToHeight(true);
        outputScroll.setFitToWidth(true);
        VBox.setVgrow(outputScroll, Priority.ALWAYS);

        TitledPane outputPane = new TitledPane("Saída", outputScroll);
        outputPane.setCollapsible(false);
        outputPane.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(outputPane, Priority.ALWAYS);

        leftColumn.getChildren().addAll(inputPane, actionButtons, outputPane);
        root.setLeft(leftColumn);

        /// Coluna direita: Tabelas de memória, registradores e símbolos
        VBox rightColumn = new VBox(10);
        rightColumn.setPadding(new Insets(10));
        TitledPane memoryPane = new TitledPane("Memória", createMemoryTablePane());
        memoryPane.setCollapsible(false);
        TitledPane registersPane = new TitledPane("Registradores", createRegisterTablePane());
        registersPane.setCollapsible(false);
        TitledPane symbolsPane = new TitledPane("Símbolos", createSymbolTablePane());
        symbolsPane.setCollapsible(false);
        rightColumn.getChildren().addAll(memoryPane, registersPane, symbolsPane);
        VBox.setVgrow(memoryPane, Priority.ALWAYS);
        VBox.setVgrow(registersPane, Priority.ALWAYS);
        VBox.setVgrow(symbolsPane, Priority.ALWAYS);

        HBox mainContent = new HBox(10, leftColumn, rightColumn);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        root.setCenter(mainContent);

        createBottomBar(root);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        updateAllTables();
        showWelcomeMessage();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
