package sicxesimulator.view;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import sicxesimulator.controller.SimulationController;
import sicxesimulator.model.SimulationModel;
import sicxesimulator.model.components.Machine;
import sicxesimulator.model.systems.Assembler;
import sicxesimulator.model.systems.Loader;
import sicxesimulator.model.systems.Runner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SimulationApp extends Application {

    // Records auxiliares para as tabelas
    public record RegisterEntry(String name, String value) {
    }
    public record MemoryEntry(String address, String value) {
    }
    public record SymbolEntry(String symbol, String address) {
    }

    private SimulationController controller;
    private TextArea outputArea;
    private TextArea inputField;
    private TableView<RegisterEntry> registerTable;
    private TableView<MemoryEntry> memoryTable;
    private TableView<SymbolEntry> symbolTable;

    @Override
    public void start(Stage primaryStage) {
        // Inicialização do modelo e controlador
        Machine virtualMachine = new Machine(40000);
        SimulationModel model = new SimulationModel(
                virtualMachine,
                new Assembler(),
                new Runner(virtualMachine),
                new Loader()
        );
        controller = new SimulationController(model, this);

        primaryStage.setTitle("SIC/XE Simulator v2.1");
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);

        BorderPane root = new BorderPane();
        // Configura a barra de menus no topo
        root.setTop(createMenuBar());

        // Coluna Esquerda: Entrada, botões de ação e Saída empilhados verticalmente
        VBox leftColumn = new VBox(10);
        leftColumn.setPadding(new Insets(10));

        TitledPane inputPane = new TitledPane("Código Assembly", createInputArea());
        inputPane.setCollapsible(false);

        // Criação dos botões de ação
        HBox actionButtons = new HBox(10);
        actionButtons.setPadding(new Insets(5));

        Button assembleButton = new Button("Montar");
        assembleButton.setOnAction(e -> {
            String inputText = inputField.getText();
            if (!inputText.trim().isEmpty()) {
                List<String> sourceLines = Arrays.asList(inputText.split("\\r?\\n"));
                controller.handleAssembleAction(sourceLines);
            }
        });

        Button runButton = new Button("Executar");
        runButton.setOnAction(e -> controller.handleRunAction());

        Button nextButton = new Button("Próximo");
        nextButton.setOnAction(e -> controller.handleNextAction());

        Button resetButton = new Button("Resetar");
        resetButton.setOnAction(e -> controller.handleResetAction());

        actionButtons.getChildren().addAll(assembleButton, runButton, nextButton, resetButton);

        TitledPane outputPane = new TitledPane("Saída", createOutputAreaPane());
        outputPane.setCollapsible(false);

        leftColumn.getChildren().addAll(inputPane, actionButtons, outputPane);
        VBox.setVgrow(inputPane, Priority.ALWAYS);
        VBox.setVgrow(outputPane, Priority.ALWAYS);

        // Coluna Direita: Tabelas empilhadas verticalmente: Memória, Registradores e Símbolos
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

        // Combina as duas colunas em um HBox
        HBox mainContent = new HBox(10, leftColumn, rightColumn);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        root.setCenter(mainContent);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        updateAllTables();
        showWelcomeMessage();
    }

    // Cria a barra de menus com os itens: Arquivo, Configurações, Ajuda, Sobre e Sair
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menu "Arquivo" com opções de importação/exportação
        Menu fileMenu = new Menu("Arquivo");
        MenuItem importAssemblyFile = new MenuItem("Importar .asm");
        importAssemblyFile.setOnAction(e -> {
            // TODO: implementar a lógica de importação de arquivo .asm
            System.out.println("Importar .asm acionado");
        });
        MenuItem importObjFile = new MenuItem("Importar .obj");
        importObjFile.setOnAction(e -> {
            // TODO: implementar a lógica de importação de arquivo .obj
            System.out.println("Importar .obj acionado");
        });
        MenuItem exportObjFile = new MenuItem("Exportar");
        exportObjFile.setOnAction(e -> {
            // TODO: implementar a lógica de exportação
            System.out.println("Exportar acionado");
        });
        fileMenu.getItems().addAll(importAssemblyFile, importObjFile, exportObjFile);

        // Menu "Configurações"
        Menu settingsMenu = new Menu("Configurações");
        MenuItem optionsItem = new MenuItem("Opções...");
        optionsItem.setOnAction(e -> {
            // TODO: implementar a lógica de configurações
            System.out.println("Opções acionadas");
        });
        settingsMenu.getItems().add(optionsItem);

        // Menu "Ajuda" com link para o repositório
        Menu helpMenu = new Menu("Ajuda");
        MenuItem repository = new MenuItem("Repositório");
        repository.setOnAction(e -> {
            getHostServices().showDocument("https://github.com/seu-repositorio");
        });
        helpMenu.getItems().add(repository);

        // Menu "Sobre" que exibe informações da aplicação
        Menu aboutMenu = new Menu("Sobre");
        MenuItem info = new MenuItem("Informações");
        info.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sobre");
            alert.setHeaderText("SIC/XE Simulator v2.1");
            alert.setContentText("© 2024 SIC/XE Team");
            alert.showAndWait();
        });
        aboutMenu.getItems().add(info);

        // Menu "Créditos" que exibe os perfis do Github de cada um dos desenvolvedores.
        Menu creditsMenu = new Menu("Créditos");

        // Perfil Renan
        MenuItem renanPinho = new MenuItem("Renan Pinho");
        renanPinho.setOnAction(e -> {
            getHostServices().showDocument("https://github.com/pinhorenan");
        });

        // Perfil Luis
        MenuItem luisRasch = new MenuItem("Luis Rasch");
        luisRasch.setOnAction(e -> {
            getHostServices().showDocument("https://github.com/LuisEduardoRasch");
        });

        // Perfil Gabriel
        MenuItem gabrielMoura = new MenuItem("Gabriel Moura");
        gabrielMoura.setOnAction(e -> {
            getHostServices().showDocument("https://github.com/gbrimoura");
        });

        // Perfil Fabricio
        MenuItem fabricioBartz = new MenuItem("Fabricio Bartz");
        fabricioBartz.setOnAction(e -> {
            getHostServices().showDocument("https://github.com/FabricioBartz");
        });

        // Perfil Arthur
        MenuItem arthurAlves = new MenuItem("Arthur Alves");
        arthurAlves.setOnAction(e -> {
            getHostServices().showDocument("https://github.com/arthursa21");
        });

        // Perfil Leonardo
        MenuItem leonardoBraga = new MenuItem("Leonardo Braga");
        leonardoBraga.setOnAction(e -> {
            getHostServices().showDocument("https://github.com/braga0425");
        });
        creditsMenu.getItems().addAll(renanPinho, luisRasch, gabrielMoura, arthurAlves, fabricioBartz, leonardoBraga);

        // Menu "Sair" com item que encerra a aplicação
        Menu exitMenu = new Menu("Sair");
        MenuItem exitItem = new MenuItem("Sair");
        exitItem.setOnAction(e -> Platform.exit());
        exitMenu.getItems().add(exitItem);

        menuBar.getMenus().addAll(fileMenu, settingsMenu, helpMenu, aboutMenu, creditsMenu, exitMenu);
        return menuBar;
    }

    // Cria a área de entrada (editor de código)
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

    // Cria a área de saída
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

    // Cria o painel para a tabela de registradores
    private ScrollPane createRegisterTablePane() {
        setupRegisterTable();
        ScrollPane registerScroll = new ScrollPane(registerTable);
        registerScroll.setFitToWidth(true);
        return registerScroll;
    }

    // Cria o painel para a tabela de memória
    private ScrollPane createMemoryTablePane() {
        setupMemoryTable();
        ScrollPane memoryScroll = new ScrollPane(memoryTable);
        memoryScroll.setFitToWidth(true);
        return memoryScroll;
    }

    // Cria o painel para a tabela de símbolos
    private ScrollPane createSymbolTablePane() {
        setupSymbolTable();
        ScrollPane symbolScroll = new ScrollPane(symbolTable);
        symbolScroll.setFitToWidth(true);
        return symbolScroll;
    }

    // Configuração da tabela de registradores utilizando lambdas
    private void setupRegisterTable() {
        registerTable = new TableView<>();
        TableColumn<RegisterEntry, String> nameCol = new TableColumn<>("Registrador");
        nameCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().name()));
        TableColumn<RegisterEntry, String> valueCol = new TableColumn<>("Valor");
        valueCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().value()));
        registerTable.getColumns().addAll(nameCol, valueCol);
        registerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // Configuração da tabela de memória utilizando lambdas
    private void setupMemoryTable() {
        memoryTable = new TableView<>();
        TableColumn<MemoryEntry, String> addressCol = new TableColumn<>("Endereço");
        addressCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().address()));
        TableColumn<MemoryEntry, String> valueCol = new TableColumn<>("Valor");
        valueCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().value()));
        memoryTable.getColumns().addAll(addressCol, valueCol);
        memoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // Configuração da tabela de símbolos utilizando lambdas
    private void setupSymbolTable() {
        symbolTable = new TableView<>();
        TableColumn<SymbolEntry, String> symbolCol = new TableColumn<>("Símbolo");
        symbolCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().symbol()));
        TableColumn<SymbolEntry, String> addressCol = new TableColumn<>("Endereço");
        addressCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().address()));
        symbolTable.getColumns().addAll(symbolCol, addressCol);
        symbolTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

    // Atualiza os dados dos registradores
    public void updateRegisterTable() {
        List<String> registerNames = Arrays.asList("A", "X", "L", "PC", "B", "S", "T", "F", "SW");
        registerTable.getItems().clear();
        for (String name : registerNames) {
            String value = controller.getSimulationModel().getVirtualMachine().getRegister(name).getValue();
            registerTable.getItems().add(new RegisterEntry(name, value));
        }
    }

    // Atualiza os dados da memória
    public void updateMemoryTable() {
        memoryTable.getItems().clear();
        for (int address = 0; address < controller.getSimulationModel().getVirtualMachine().getMemory().getSize(); address++) {
            String value = controller.getSimulationModel().getVirtualMachine().getMemory().read(address);
            memoryTable.getItems().add(new MemoryEntry(String.format("%04X", address), value));
        }
    }

    // Atualiza os dados dos símbolos
    public void updateSymbolTable() {
        symbolTable.getItems().clear();
        Map<String, Integer> symbols = controller.getSimulationModel().getAssembler().getSymbolTable();
        symbols.forEach((name, address) ->
                symbolTable.getItems().add(new SymbolEntry(name, String.format("%04X", address))));
    }

    // Atualiza todas as tabelas
    public void updateAllTables() {
        updateRegisterTable();
        updateMemoryTable();
        updateSymbolTable();
    }

    public void appendOutput(String message) {
        Platform.runLater(() -> outputArea.appendText("> " + message + "\n"));
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

    public static void main(String[] args) {
        launch(args);
    }
}
