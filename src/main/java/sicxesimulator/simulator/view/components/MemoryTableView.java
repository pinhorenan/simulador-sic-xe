package sicxesimulator.simulator.view.components;

import javafx.scene.control.TableColumn;
import sicxesimulator.simulator.view.SimulationApp.MemoryEntry;

public class MemoryTableView extends BaseTableView<MemoryEntry> {

    public MemoryTableView() {
        super("Endereço", "Valor");
    }

    @Override
    protected void createColumns(String[] columnTitles) {
        TableColumn<MemoryEntry, String> addressCol = createColumn(columnTitles[0], "address");
        TableColumn<MemoryEntry, String> valueCol = createColumn(columnTitles[1], "value");

        this.getColumns().addAll(addressCol, valueCol);
    }
}