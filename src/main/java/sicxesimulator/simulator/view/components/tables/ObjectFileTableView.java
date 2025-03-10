package sicxesimulator.simulator.view.components.tables;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

public class ObjectFileTableView extends TableView<ObjectFileTableItem> {

    private final ObservableList<ObjectFileTableItem> entries = FXCollections.observableArrayList();

    public ObjectFileTableView() {
        // Habilita a edição na tabela
        setEditable(true);

        // Coluna de seleção com checkbox
        TableColumn<ObjectFileTableItem, Boolean> selectedCol = new TableColumn<>("Selecionado");
        selectedCol.setPrefWidth(90);
        selectedCol.setEditable(true);
        selectedCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectedCol));

        // Outras colunas (Programa, Tamanho, Data de Montagem)
        TableColumn<ObjectFileTableItem, String> nameCol = new TableColumn<>("Programa");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("programName"));
        nameCol.setPrefWidth(150);

        TableColumn<ObjectFileTableItem, String> sizeCol = new TableColumn<>("Tamanho");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(100);

        TableColumn<ObjectFileTableItem, String> dateCol = new TableColumn<>("Data de Montagem");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("mountDate"));
        dateCol.setPrefWidth(150);

        // Adiciona as colunas à tabela
        this.getColumns().addAll(nameCol, sizeCol, dateCol, selectedCol);
        this.setItems(entries);
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
