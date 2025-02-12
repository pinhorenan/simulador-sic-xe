package sicxesimulator;

import sicxesimulator.simulation.virtualMachine.Machine;
import sicxesimulator.simulation.systems.FileHandler;
import sicxesimulator.simulation.systems.Interpreter;
import sicxesimulator.simulation.systems.Assembler;
import sicxesimulator.simulation.systems.Console;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.PrintStream;

public class MainApp extends Application {

    private Console console;
    private TextArea outputArea;
    private TextField inputField;

    @Override
    public void start(Stage primaryStage) {
        // Criando instâncias das classes necessárias
        FileHandler fileHandler = new FileHandler();
        Machine machine = new Machine(); // Ou a forma correta de instanciar Machine
        Interpreter interpreter = new Interpreter(machine);
        Assembler assembler = new Assembler();

        // Agora passamos os argumentos corretamente para Console
        console = new Console(machine, fileHandler, interpreter, assembler);

        primaryStage.setTitle("Simulador SIC/XE");

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        outputArea = new TextArea();
        outputArea.setEditable(false);

        inputField = new TextField();
        inputField.setPromptText("Digite um comando...");
        inputField.setOnAction(this::handleInputAction);

        inputField.setPrefWidth(screenWidth*0.2); // Define a largura do campo de entrada

        Button executeButton = new Button("Executar");
        executeButton.setOnAction(this::handleInputAction);

        executeButton.setPrefWidth(screenWidth*0.05); // Define a largura do botão

        HBox inputBox = new HBox(10, inputField, executeButton);
        inputBox.setPadding(new Insets(10)); // Adiciona um espaçamento interno
        inputBox.setAlignment(Pos.CENTER_LEFT); // Alinha os elementos à esquerda

        BorderPane root = new BorderPane();
        root.setCenter(outputArea);
        root.setBottom(inputBox);
        
        Scene scene = new Scene(root, screenWidth * 0.6, screenHeight * 0.6);

        // Cria a janela propriamente dita
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Redireciona a saída do console para a área de texto
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(outputArea));
        console.setOutput(printStream);
    }

    private void handleInputAction(javafx.event.ActionEvent event) {
        String command = inputField.getText();
        inputField.clear();
        processCommand(command);
    }

    private void processCommand(String command) {
        console.treatCommand(command);
        outputArea.appendText("> " + command + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}