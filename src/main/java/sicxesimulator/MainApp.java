package sicxesimulator;

import sicxesimulator.simulation.virtualMachine.Machine;
import sicxesimulator.simulation.systems.FileHandler;
import sicxesimulator.simulation.systems.Interpreter;
import sicxesimulator.simulation.systems.Assembler;
import sicxesimulator.simulation.systems.Console;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class MainApp extends Application {

    private Console console;
    private TextArea outputArea;
    private TextField inputField;
    private TableView<RegisterEntry> registerTable;

    @Override
    public void start(Stage primaryStage) {

        // Criando instâncias das classes necessárias
        FileHandler fileHandler = new FileHandler();
        Machine machine = new Machine(); // Ou a forma correta de instanciar Machine
        Interpreter interpreter = new Interpreter(machine);
        Assembler assembler = new Assembler();

        // Agora passamos os argumentos corretamente para Console
        console = new Console(machine, fileHandler, interpreter, assembler);

        // Título da janela
        primaryStage.setTitle("Simulador SIC/XE");

        // Obtém as dimensões da tela
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        outputArea = new TextArea();
        outputArea.setEditable(false);

        inputField = new TextField();
        inputField.setPromptText("Digite um comando...");
        inputField.setOnAction(this::handleInputAction);

        inputField.setPrefWidth(screenWidth * 0.2); // Define a largura do campo de entrada

        Button executeButton = new Button("Executar");
        executeButton.setOnAction(this::handleInputAction);

        executeButton.setPrefWidth(screenWidth * 0.05); // Define a largura do botão

        HBox inputBox = new HBox(10, inputField, executeButton);
        inputBox.setPadding(new Insets(10)); // Adiciona um espaçamento interno
        inputBox.setAlignment(Pos.CENTER_LEFT); // Alinha os elementos à esquerda

        // Cria a tabela de registradores
        registerTable = new TableView<>();
        TableColumn<RegisterEntry, String> nameColumn = new TableColumn<>("Registrador");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<RegisterEntry, String> valueColumn = new TableColumn<>("Valor");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        registerTable.getColumns().addAll(nameColumn, valueColumn);
        registerTable.setPrefWidth(screenWidth * 0.12);
        registerTable.setPrefHeight(screenHeight * 0.5);
        registerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Impede o redimensionamento das colunas pelo usuário

        // Alinha a tabela à direita
        BorderPane.setAlignment(registerTable, Pos.CENTER_RIGHT);

        // Atualiza a tabela de registradores
        updateRegisterTable(machine);

        // Cria o conteúdo principal
        BorderPane contentPane = new BorderPane();
        contentPane.setCenter(outputArea);
        contentPane.setBottom(inputBox);
        contentPane.setRight(registerTable);

        // Criar a cena
        Scene scene = new Scene(contentPane, screenWidth * 0.6, screenHeight * 0.6);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());

        // Configura a janela
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Redireciona a saída do console para a área de texto
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(outputArea));
        console.setOutput(printStream);

        PauseTransition pause1 = new PauseTransition(Duration.seconds(1));
        pause1.setOnFinished(event -> {
            outputArea.appendText("Bem-vindo ao Simulador SIC/XE!\n");
        });

        PauseTransition pause2 = new PauseTransition(Duration.seconds(2));
        pause2.setOnFinished(event -> {
            outputArea.appendText("Digite \"comandos\" para ver a lista de comandos disponíveis.\n\n");
        });

        // Primeiro exibe a mensagem de boas-vindas, depois a segunda mensagem
        pause1.play();
        pause2.play();
    }

    private void handleInputAction(javafx.event.ActionEvent event) {
        String command = inputField.getText();
        inputField.clear();
        processCommand(command);
    }

    private void processCommand(String command) {
        outputArea.appendText("> " + command + "\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand(command);
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void updateRegisterTable(Machine machine) {
        List<String> registerNames = Arrays.asList("A", "X", "L", "PC", "B", "S", "T", "F", "SW");
        registerTable.getItems().clear();
        for (String name : registerNames) {
            String value = machine.getRegister(name).getValue();
            registerTable.getItems().add(new RegisterEntry(name, value));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
