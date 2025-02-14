package sicxesimulator.ui;

// Importações do JavaFX
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

// Importações próprias
import sicxesimulator.systems.Assembler;
import sicxesimulator.utils.FileHandler;
import sicxesimulator.systems.Interpreter;
import sicxesimulator.components.Machine;
import sicxesimulator.components.operations.Instruction;

// Importações Java nativas.
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class SimulationApp extends Application {
    // Componentes do simulador
    private Machine virtualMachine;
    private FileHandler fileHandler;
    private Interpreter interpreter;
    private Assembler assembler;

    // Componentes da interface
    private TextArea outputArea;
    private TextArea inputField;
    private TableView<RegisterEntry> registerTable;
    private TableView<MemoryEntry> memoryTable;


    @Override
    public void start(Stage primaryStage) {
        // Inicializa os componentes centrais
        virtualMachine = new Machine();
        fileHandler = new FileHandler();
        interpreter = new Interpreter(virtualMachine);
        assembler = new Assembler();

        // Configura os elementos da interface gráfica.
        primaryStage.setTitle("Simulador SIC/XE");

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();


        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefWidth(screenWidth * 0.3);
        outputArea.setPrefHeight(screenHeight * 0.4);

        inputField = new TextArea();
        inputField.setPromptText("Digite comandos ou código assembly...");
        inputField.setWrapText(true);
        inputField.setPrefWidth(screenWidth * 0.3);
        inputField.setPrefHeight(screenHeight * 0.2);
        inputField.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                processCommand(inputField.getText());
                inputField.clear();
            }
        });

        // Botões de ação
        Button assembleButton = new Button("Montar");
        assembleButton.setOnAction(event -> handleAssembleAction());
        Button runButton = new Button("Executar");
        runButton.setOnAction(event -> handleRunAction());
        Button nextButton = new Button("Próximo");
        nextButton.setOnAction(event -> handleNextAction());
        Button clearButton = new Button("Limpar");
        clearButton.setOnAction(event -> handleClearAction());
        Button exitButton = new Button("Sair");
        exitButton.setOnAction(event -> System.exit(0));

        double buttonWidth = screenWidth * 0.05;
        assembleButton.setPrefWidth(buttonWidth);
        runButton.setPrefWidth(buttonWidth);
        nextButton.setPrefWidth(buttonWidth);
        clearButton.setPrefWidth(buttonWidth);
        exitButton.setPrefWidth(buttonWidth);

        VBox buttonColumn = new VBox(30, assembleButton, runButton, nextButton, clearButton, exitButton);
        buttonColumn.setAlignment(Pos.CENTER_LEFT);

        // Tabela de Registradores
        registerTable = new TableView<>();

        TableColumn<RegisterEntry, String> registerNameCol = new TableColumn<>("Registrador");
        registerNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<RegisterEntry, String> registerValueCol = new TableColumn<>("Valor");
        registerValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        registerTable.getColumns().addAll(registerNameCol, registerValueCol);
        registerTable.setPrefWidth(screenWidth * 0.12);
        registerTable.setPrefHeight(screenHeight * 0.5);
        registerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Tabela de Memória
        memoryTable = new TableView<>();

        TableColumn<MemoryEntry, String> memoryAddressCol = new TableColumn<>("Endereço");
        memoryAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<MemoryEntry, String> memoryValueCol = new TableColumn<>("Valor");
        memoryValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        memoryTable.getColumns().addAll(memoryAddressCol, memoryValueCol);
        memoryTable.setPrefWidth(screenWidth * 0.12);
        memoryTable.setPrefHeight(screenHeight * 0.5);
        memoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox registerBox = new VBox(registerTable);
        registerBox.setPadding(new Insets(10));
        registerBox.setAlignment(Pos.TOP_RIGHT);

        VBox memoryBox = new VBox(memoryTable);
        memoryBox.setPadding(new Insets(10));
        memoryBox.setAlignment(Pos.TOP_RIGHT);

        HBox topBox = new HBox(10, inputField, buttonColumn, registerBox);
        topBox.setPadding(new Insets(15));
        topBox.setAlignment(Pos.TOP_CENTER);
        topBox.setPrefHeight(screenHeight * 0.27);

        HBox outputBox = new HBox(10, outputArea, memoryBox);
        outputBox.setPadding(new Insets(5, 15, 5, 15));
        outputBox.setAlignment(Pos.BOTTOM_CENTER);
        outputBox.setPrefHeight(screenHeight * 0.3);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setBottom(outputBox);

        updateRegisterTable();
        updateMemoryTable();

        Scene scene = new Scene(root);
        primaryStage.sizeToScene();
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Redireciona a saída do console para a área de texto
        PrintStream ps = new PrintStream(new TextAreaOutputStream(outputArea));
        // TODO método para definir a saída no console ou whatever.

        PauseTransition pause1 = new PauseTransition(Duration.seconds(1));
        pause1.setOnFinished(e -> outputArea.appendText("Bem vindo ao Simulador SIC/XE!\n"));
        pause1.play();

        PauseTransition pause2 = new PauseTransition(Duration.seconds(2));
        pause2.setOnFinished(e -> outputArea.appendText("Para começar, digite comandos ou digite um código assembly e clique em 'Montar'.\n"));
        pause2.play();
    }


    private void handleAssembleAction() {
        outputArea.appendText("> montar[arquivo.asm]\n");
        // TODO
        // Por enquanto, vamos assumir um teste em resources.
        List<String> sourceLines = fileHandler.readFileLines("teste.asm");
        if (sourceLines == null) {
            outputArea.appendText("Erro ao ler arquivo.\n");
            return;
        }
        List<Instruction> assembledInstructions = assembler.assemble(sourceLines);
        if (assembledInstructions.isEmpty()) {
            outputArea.appendText("Falha ao montar o programa. \n");
            return;
        }
        interpreter.setInstructions(assembledInstructions);
        outputArea.appendText("Montagem concluída e programa carregado.\n");
    }

    private void handleRunAction() {
        outputArea.appendText("> exec\n");
        while (!interpreter.isFinished()) {
            interpreter.runNextInstruction();
        }

        outputArea.appendText("Execução concluída.\n");
        updateRegisterTable();
        updateMemoryTable();
    }

    private void handleNextAction() {
        outputArea.appendText("> prox\n");
        interpreter.runNextInstruction();
        updateRegisterTable();
        updateMemoryTable();
    }

    private void handleClearAction() {
        // TODO
    }

    private void processCommand(String command) {
        outputArea.appendText("> " + command + "\n");
        // TODO; Aqui, você pode processar comandos de texto digitados pelo usuário (se necessário)
    }

    private void updateRegisterTable() {
        List<String> registerNames = Arrays.asList("A", "X", "L", "PC", "B", "S", "T", "F", "SW");
        registerTable.getItems().clear();
        for (String name : registerNames) {
            String value = virtualMachine.getRegister(name).getValue();
            registerTable.getItems().add(new RegisterEntry(name, value));
        }
    }

    private void updateMemoryTable() {
        memoryTable.getItems().clear();
        for (int address = 0; address < virtualMachine.getMemory().getSize(); address++) {
            String value = virtualMachine.getMemory().read(address);
            memoryTable.getItems().add(new MemoryEntry(String.format("%04X", address), value));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
