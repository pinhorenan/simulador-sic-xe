package sicxesimulator.application.components.panels;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import sicxesimulator.application.components.buttons.MainButtons;

public class InputPanel {
    private final TitledPane inputPane;
    private final TextArea inputArea;
    private final TextArea expandedCodeArea;
    private Button assembleButton;

    public InputPanel() {
        // Área de entrada de código (sem altura fixa)
        inputArea = new TextArea();
        inputArea.setPromptText("Insira seu código assembly aqui...");
        inputArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14;");

        // Área para exibir o código expandido (sem altura fixa)
        expandedCodeArea = new TextArea();
        expandedCodeArea.setPromptText("Código Expandido...");
        expandedCodeArea.setEditable(false);
        expandedCodeArea.setStyle("-fx-font-family: Consolas; -fx-font-size: 14; -fx-text-fill: #006400;");

        // Definindo que ambos os TextArea devem crescer igualmente
        VBox.setVgrow(inputArea, Priority.ALWAYS);
        VBox.setVgrow(expandedCodeArea, Priority.ALWAYS);

        // Criando o cabeçalho do painel com título e botões alinhados
        Label titleLabel = new Label("Código");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Espaçador que empurra os botões para a direita

        HBox buttonBox = new HBox(5, spacer); // O botão Montar será adicionado depois
        spacer.setMaxWidth(Double.MAX_VALUE);

        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(titleLabel);
        headerPane.setRight(buttonBox);

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
    public void setMainButtons(MainButtons mainButtons) {
        if (assembleButton != null) {
            return; // Evita reatribuição caso já tenha sido definido
        }

        this.assembleButton = mainButtons.getAssembleButton();

        // Atualiza o cabeçalho para incluir o botão Montar corretamente
        BorderPane headerPane = (BorderPane) inputPane.getGraphic();
        HBox buttonBox = (HBox) headerPane.getRight();
        buttonBox.getChildren().add(assembleButton);
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

    public TextArea getInputArea() {
        return inputArea;
    }
}
