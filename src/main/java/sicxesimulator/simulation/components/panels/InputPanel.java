package sicxesimulator.simulation.components.panels;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import sicxesimulator.simulation.components.buttons.AssemblerButtons;

public class InputPanel {
    private final TitledPane inputPane;
    private final TextArea inputArea;
    private final TextArea expandedCodeArea;
    private Button assembleButton;

    public InputPanel() {
        // Área de entrada de código
        inputArea = new TextArea();
        inputArea.setPromptText("Insira seu código assembly aqui...");
        inputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");

        // Área para exibir o código expandido
        expandedCodeArea = new TextArea();
        expandedCodeArea.setPromptText("Código Expandido...");
        expandedCodeArea.setEditable(false);
        expandedCodeArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: #006400;");

        // Definindo que ambos os TextArea devem crescer igualmente
        VBox.setVgrow(inputArea, Priority.ALWAYS);
        VBox.setVgrow(expandedCodeArea, Priority.ALWAYS);

        // Defino um rótulo para o título
        Label titleLabel = new Label("Código");

        // Defino um espaçador para empurrar os botões para a direita
        Region spacer = new Region();
        spacer.setPrefWidth(200);
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Defino um HBox para os botões de controle dos arquivos
        HBox buttonBox = new HBox(); // O botão Montar será adicionado depois

        // Defino um HBox para o cabeçalho
        HBox headerHBox = new HBox();
        headerHBox.getChildren().addAll(titleLabel, spacer, buttonBox);
        headerHBox.setAlignment(Pos.CENTER);

        // Defino um BorderPane para o cabeçalho
        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(headerHBox);

        // Criando um VBox para empilhar os TextArea com um rótulo para o código expandido
        Label expandedLabel = new Label("Código Expandido");
        VBox contentBox = new VBox(5, inputArea, expandedLabel, expandedCodeArea);
        VBox.setVgrow(inputArea, Priority.ALWAYS);
        VBox.setVgrow(expandedCodeArea, Priority.ALWAYS);

        // Criando o TitledPane e ajustando o cabeçalho
        inputPane = new TitledPane();
        inputPane.setText(null); // Usamos o header customizado
        inputPane.setGraphic(headerPane);
        inputPane.setContent(contentBox);
        inputPane.setCollapsible(false);
        inputPane.setMaxHeight(Double.MAX_VALUE);
    }

    /**
     * Define os botões reais após a criação do "MainButtons",
     * garantindo que o botão de montagem mantenha os bindings.
     */
    public void setButtons(AssemblerButtons buttons) {
        if (assembleButton != null) {
            return;
        }

        this.assembleButton = buttons.getAssembleButton();

        // Atualiza o cabeçalho para incluir o botão Montar corretamente
        BorderPane headerPane = (BorderPane) inputPane.getGraphic();
        HBox headerHBox = (HBox) headerPane.getLeft();
        HBox buttonHBox = (HBox) headerHBox.getChildren().getLast();
        buttonHBox.getChildren().add(assembleButton);
    }

    public TitledPane getPane() {
        return inputPane;
    }

    public String getInputText() {
        return inputArea.getText();
    }

    public void setInputText(String text) {
        inputArea.setText(text);
    }

    public void setExpandedCodeText(String text) {
        expandedCodeArea.setText(text);
    }

    public TextArea getExpandedCodeArea() {
        return expandedCodeArea;
    }

    public TextArea getInputArea() {
        return inputArea;
    }
}
