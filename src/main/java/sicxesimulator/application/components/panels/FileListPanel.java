package sicxesimulator.application.components.panels;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import sicxesimulator.application.components.buttons.FileListButtons;
import sicxesimulator.application.components.tables.FileTable;
import sicxesimulator.application.model.data.ObjectFileTableItem;
import sicxesimulator.data.ObjectFile;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.util.DialogUtil;

import java.io.IOException;
import java.util.List;

public class FileListPanel {
    private final TitledPane objectFilePane;
    private final FileTable objectFileTable;
    private final HBox fileControlButtons;
    private final Controller controller;

    public FileListPanel(Controller controller) {
        this.controller = controller;
        this.objectFileTable = new FileTable();
        objectFileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        objectFileTable.setMaxWidth(Double.MAX_VALUE);

        // Defino uma label para o título do painel
        Label titleLabel = new Label("Arquivos");

        // Defino um espaçador para empurrar os botões para a direita
        Region spacer = new Region();
        spacer.setPrefWidth(50);
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Defino um HBox para os botões de controle dos arquivos
        fileControlButtons = new HBox(5);

        // Defino um HBox para o cabeçalho
        HBox headerHBox = new HBox();
        headerHBox.getChildren().addAll(titleLabel, spacer, fileControlButtons);
        headerHBox.setAlignment(Pos.CENTER);

        // Defino um BorderPane para o cabeçalho
        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(headerHBox);

        this.objectFilePane = new TitledPane();
        objectFilePane.setText(null);
        objectFilePane.setGraphic(headerPane);
        objectFilePane.setContent(objectFileTable);
        objectFilePane.setCollapsible(false);
        objectFilePane.setMaxHeight(Double.MAX_VALUE);

        // 🔹 Adiciona evento de clique duplo para carregar o arquivo na interface
        objectFileTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<ObjectFileTableItem>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ObjectFileTableItem item = row.getItem();
                    loadFileToView(item.getObjectFile());
                }
            });
            return row;
        });
    }

    public void setButtons(FileListButtons buttons) {
        fileControlButtons.getChildren().clear();
        fileControlButtons.getChildren().addAll(buttons.getLinkButton(), buttons.getDeleteButton());
    }

    /**
     * Atualiza os painéis da interface com os dados do arquivo selecionado.
     */
    private void loadFileToView(ObjectFile objectFile) {
        if (objectFile == null) return;

        // 🔹 Obtém o código-fonte original
        List<String> rawSourceCode = objectFile.getRawSourceCode();
        if (rawSourceCode == null || rawSourceCode.isEmpty()) {
            DialogUtil.showError("O arquivo não contém código-fonte.");
            return;
        }

        // 🔹 Define o código fonte original no `InputPanel`
        String sourceCode = String.join("\n", rawSourceCode);
        controller.getMainLayout().getInputPanel().setInputText(sourceCode);

        // 🔹 Processa os macros para gerar o código expandido e exibir no TextArea inferior
        try {
            List<String> expandedCode = controller.getModel().processCodeMacros(rawSourceCode);
            controller.getMainLayout().getInputPanel().setExpandedCodeText(String.join("\n", expandedCode));
        } catch (IOException e) {
            DialogUtil.showError("Erro ao expandir macros: " + e.getMessage());
        }

        // 🔹 Atualiza a tabela de símbolos com os símbolos do arquivo
        controller.getUpdater().updateSymbolTableView(objectFile);
    }

    public TitledPane getPane() {
        return objectFilePane;
    }

    public FileTable getObjectFileTable() {
        return objectFileTable;
    }
}
