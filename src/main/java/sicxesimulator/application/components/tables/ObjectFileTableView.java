package sicxesimulator.application.components.tables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import sicxesimulator.application.model.ObjectFileTableItem;
import sicxesimulator.models.ObjectFile;

import java.util.List;
import java.util.stream.Collectors;

public class ObjectFileTableView extends TableView<ObjectFileTableItem> {

    private final ObservableList<ObjectFileTableItem> entries = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public ObjectFileTableView() {
        // Configura a seleção múltipla
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Configura o comportamento de duplo clique nas linhas
        this.setRowFactory(tv -> {
            TableRow<ObjectFileTableItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    ObjectFileTableItem clickedItem = row.getItem();
                    System.out.println("Duplo clique em " + clickedItem.getProgramName());
                    // TODO: Não é aqui, mas preciso lidar com a exibição do conteúdo de arquivos gerados pela ligação de vários módulos.
                    // TODO: Atualmente, quando dou duplo clique em um arquivo linkado, ele exibe dialog informando que o arquivo não possui código fonte.
                }
            });
            return row;
        });

        // Criação das colunas (removemos a coluna "Selecionado")
        TableColumn<ObjectFileTableItem, String> nameCol = new TableColumn<>("Programa");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("programName"));
        nameCol.setPrefWidth(80);
        nameCol.setMaxWidth(Double.MAX_VALUE);

        TableColumn<ObjectFileTableItem, String> sizeCol = new TableColumn<>("Tamanho");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(80);

        TableColumn<ObjectFileTableItem, String> dateCol = new TableColumn<>("Data de Montagem");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("mountDate"));
        dateCol.setPrefWidth(120);

        this.getColumns().addAll(nameCol, sizeCol, dateCol);
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
        if (!entries.contains(entry)) {  // Evita duplicação
            entries.add(entry);
        }
    }
}