package sicxesimulator.application.view.components.tables;

import javafx.scene.control.TableColumn;
import sicxesimulator.application.model.records.MemoryEntry;

public class MemoryTableView extends BaseTableView<MemoryEntry> {

    public MemoryTableView() {
        super("Endereço", "Valor");
    }

    @Override
    protected void createColumns(String[] columnTitles) {
        TableColumn<MemoryEntry, String> addressCol = createColumn(columnTitles[0], "address");
        TableColumn<MemoryEntry, String> valueCol = createColumn(columnTitles[1], "value");

        //noinspection unchecked
        this.getColumns().addAll(addressCol, valueCol);
    }
}