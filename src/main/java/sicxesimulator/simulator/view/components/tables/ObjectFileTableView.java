package sicxesimulator.simulator.view.components.tables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;

public class ObjectFileTableView extends TableView<ObjectFileTableItem> {

    private final ObservableList<ObjectFileTableItem> entries = FXCollections.observableArrayList();

    public ObjectFileTableView() {
        // Configura a seleção múltipla
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Criação das colunas (removemos a coluna "Selecionado")
        TableColumn<ObjectFileTableItem, String> nameCol = new TableColumn<>("Programa");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("programName"));
        nameCol.setPrefWidth(150);

        TableColumn<ObjectFileTableItem, String> sizeCol = new TableColumn<>("Tamanho");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(100);

        TableColumn<ObjectFileTableItem, String> dateCol = new TableColumn<>("Data de Montagem");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("mountDate"));
        dateCol.setPrefWidth(150);

        this.getColumns().addAll(nameCol, sizeCol, dateCol);
        this.setItems(entries);

        // Adiciona um Event Filter na TableView para interceptar o mouse antes do comportamento padrão
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // Procura o TableRow que foi clicado
            Node target = event.getPickResult().getIntersectedNode();
            while (target != null && !(target instanceof TableRow)) {
                target = target.getParent();
            }
            if (target instanceof TableRow) {
                @SuppressWarnings("unchecked")
                TableRow<ObjectFileTableItem> row = (TableRow<ObjectFileTableItem>) target;
                int index = row.getIndex();
                if (index >= 0) {
                    // Faz o toggle da seleção: se a linha já está selecionada, deseleciona; caso contrário, seleciona
                    if (this.getSelectionModel().isSelected(index)) {
                        this.getSelectionModel().clearSelection(index);
                    } else {
                        this.getSelectionModel().select(index);
                    }
                    // Consome o evento para evitar que o comportamento padrão (que limparia outras seleções) seja executado
                    event.consume();
                }
            }
        });
    }

    public void addEntry(ObjectFileTableItem entry) {
        entries.add(entry);
    }

    public void clearEntries() {
        entries.clear();
    }

    public ObservableList<ObjectFileTableItem> getEntries() {
        return entries;
    }
}
