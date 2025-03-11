package sicxesimulator.application.components.panels;

import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import sicxesimulator.application.components.tables.ObjectFileTableView;
import sicxesimulator.application.components.buttons.MainButtons;
import sicxesimulator.application.model.ObjectFileTableItem;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.utils.DialogUtil;

import java.io.IOException;
import java.util.List;

public class ObjectFilePanel {
    private final TitledPane objectFilePane;
    private final ObjectFileTableView objectFileTable;
    private final HBox fileControlButtons;
    private final Controller controller;

    public ObjectFilePanel(Controller controller) {
        this.controller = controller;
        this.objectFileTable = new ObjectFileTableView();
        objectFileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        objectFileTable.setMaxWidth(Double.MAX_VALUE);

        fileControlButtons = new HBox();

        BorderPane headerPane = new BorderPane();
        headerPane.setLeft(new Label("Arquivos Montados"));
        headerPane.setRight(fileControlButtons);

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

    public void setMainButtons(MainButtons buttons) {
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

    public ObjectFileTableView getObjectFileTable() {
        return objectFileTable;
    }
}
