package sicxesimulator.simulation.components.tables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import sicxesimulator.simulation.data.ObjectFileTableItem;
import sicxesimulator.software.data.ObjectFile;

import java.util.List;
import java.util.stream.Collectors;

public class FileTable extends TableView<ObjectFileTableItem> {

    private final ObservableList<ObjectFileTableItem> entries = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public FileTable() {
        // Configura a seleção múltipla
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Configura o comportamento de duplo clique nas linhas
        this.setRowFactory(tv -> {
            TableRow<ObjectFileTableItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    ObjectFileTableItem clickedItem = row.getItem();
                    System.out.println("Duplo clique em " + clickedItem.getProgramName());
                }
            });
            return row;
        });

        TableColumn<ObjectFileTableItem, String> nameCol = new TableColumn<>("Programa");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("programName"));
        nameCol.setPrefWidth(80);
        nameCol.setMaxWidth(Double.MAX_VALUE);

        TableColumn<ObjectFileTableItem, String> sizeCol = new TableColumn<>("Tamanho");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(80);

        TableColumn<ObjectFileTableItem, String> originCol = new TableColumn<>("Origem");
        originCol.setCellValueFactory(new PropertyValueFactory<>("origin"));
        originCol.setPrefWidth(120);

        this.getColumns().addAll(nameCol, sizeCol, originCol);
        this.setItems(entries);

        // Adiciona um Event Filter na TableView para interceptar o mouse antes do comportamento padrão
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // Procura o TableRow que foi clicado
            Node target = event.getPickResult().getIntersectedNode();
            while (target != null && !(target instanceof TableRow)) {
                target = target.getParent();
            }
            if (target != null) {
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

    public List<ObjectFile> getSelectedFiles() {
       return getSelectionModel().getSelectedItems()
                .stream()
                .map(ObjectFileTableItem::getObjectFile)
                .collect(Collectors.toList());
    }

    public void addEntry(ObjectFileTableItem entry) {
        if (!entries.contains(entry)) {
            entries.add(entry);
        }
    }
}