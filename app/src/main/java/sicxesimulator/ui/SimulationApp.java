package sicxesimulator.ui;

// Importações do JavaFX
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;

// Importações próprias
import sicxesimulator.systems.Assembler;
import sicxesimulator.systems.Console;
import sicxesimulator.utils.FileHandler;
import sicxesimulator.systems.Interpreter;
import sicxesimulator.components.Machine;
import sicxesimulator.components.operations.Instruction;

// Importações Java nativas.
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimulationApp extends Application {
    // Componentes do simulador
    private Machine virtualMachine;
    private FileHandler fileHandler;
    private Interpreter interpreter;
    private Assembler assembler;
    private Console console;

    // Componentes da interface
    private TextArea outputArea;
    private TextArea inputField;
    private TableView<RegisterEntry> registerTable;
    private TableView<MemoryEntry> memoryTable;
    private MediaPlayer mediaPlayer;
    private String previousMusicFile = "";


    @Override
    public void start(Stage primaryStage) {
        // Inicializa os componentes centrais
        virtualMachine = new Machine();
        fileHandler = new FileHandler();
        interpreter = new Interpreter(virtualMachine);
        assembler = new Assembler();

        // Console
        console = new Console(virtualMachine, fileHandler, interpreter, assembler);

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

        double buttonWidth = screenWidth * 0.05;
        assembleButton.setPrefWidth(buttonWidth);
        runButton.setPrefWidth(buttonWidth);
        nextButton.setPrefWidth(buttonWidth);
        clearButton.setPrefWidth(buttonWidth);

        VBox buttonColumn = new VBox(30, assembleButton, runButton, nextButton, clearButton);
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

        BorderPane contentPane = new BorderPane();
        contentPane.setTop(topBox);
        contentPane.setBottom(outputBox);

        updateRegisterTable();
        updateMemoryTable();

        Scene scene = new Scene(contentPane);
        primaryStage.sizeToScene();
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        // Redireciona a saída do console para a área de texto
        PrintStream ps = new PrintStream(new TextAreaOutputStream(outputArea));
        // TODO método para definir a saída no console ou whatever.

        // Carregar a imagem de plano de fundo
        Image backgroundImage = new Image(getClass().getResource("/background.png").toExternalForm());

        // Criar o BackgroundImage
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, true)
        );

        contentPane.setBackground(new Background(background));

        // Músicas de fundo
        List<String> musicFiles = List.of(
                getClass().getResource("/music/Crawling.mp3").toExternalForm(),
                getClass().getResource("/music/Numb.mp3").toExternalForm(),
                getClass().getResource("/music/In The End.mp3").toExternalForm()
        );

        Random random = new Random();
        try {
            playRandomMusic(musicFiles, random);
        } catch (Exception e) {
            System.out.println("Não foi possível tocar nenhuma música");
        }

        // Redireciona a saída do console para a area de texto
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(outputArea));
        console.setOutput(printStream);

        PauseTransition pause1 = new PauseTransition(Duration.seconds(1));
        pause1.setOnFinished(event -> outputArea.appendText("Bem vindo ao Simulador SIC/XE!\n\n"));

        PauseTransition pause2 = new PauseTransition(Duration.seconds(2));
        pause2.setOnFinished(event -> {
            outputArea.appendText("Para comecar digite um codigo e clique no botao \"Montar\"." +
                    "\nApos isso, use os botoes \"Executar\" para executar o programa de uma so vez, " +
                    "ou \"Proximo\" para executar o programa passo a passo. Use tambom o o botao \"Parar\" " +
                    "ou \"Proximo\" para executar o programa passo a passo.\nUse tambem o o botao \"Parar\"" +
                    "para parar a execucao ou o botao \"Sair\" para finalizar o programa.\n\n");
        });


        pause1.play();
        pause2.play();
    }




    private void handleAssembleAction() {
        outputArea.clear();
        outputArea.appendText("> montar[teste.asm]\n");
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
        outputArea.clear();
        outputArea.appendText("> exec\n");
        while (!interpreter.isFinished()) {
            interpreter.runNextInstruction();
        }

        outputArea.appendText("Execução concluída.\n");
        updateRegisterTable();
        updateMemoryTable();
    }

    private void handleNextAction() {
        outputArea.clear();
        outputArea.appendText("> prox\n");
        interpreter.runNextInstruction();
        updateRegisterTable();
        updateMemoryTable();
    }

    private void handleClearAction() {
        outputArea.clear();
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

    private void playRandomMusic(List<String> musicFiles, Random random) {
        // Criar uma cópia da lista de músicas para manipulação
        List<String> availableMusic = new ArrayList<>(musicFiles);

        // Se houver uma música anterior, remova-a da lista para não tocá-la novamente
        if (!previousMusicFile.isEmpty()) {
            availableMusic.remove(previousMusicFile);
        }

        // Escolher aleatoriamente uma música da lista de músicas restantes
        String musicFile = availableMusic.get(random.nextInt(availableMusic.size()));

        // Atualiza a música anterior
        previousMusicFile = musicFile;

        // Criar o Media e MediaPlayer
        Media media = new Media(musicFile);
        mediaPlayer = new MediaPlayer(media);

        // Definir o volume, se necessário
        mediaPlayer.setVolume(0.15);

        // Adicionar ouvinte para quando a música terminar
        mediaPlayer.setOnEndOfMedia(() -> {
            // Quando a música acabar, toca uma nova música aleatória
            playRandomMusic(musicFiles, random);
        });

        // Iniciar a reprodução da música
        mediaPlayer.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
