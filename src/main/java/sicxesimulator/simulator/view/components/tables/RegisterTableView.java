package sicxesimulator.simulator.view.components.tables;

import javafx.scene.control.TableColumn;
import sicxesimulator.simulator.view.records.RegisterEntry;

public class RegisterTableView extends BaseTableView<RegisterEntry> {

    public RegisterTableView() {
        super("Registrador", "Valor");
    }

    @Override
    protected void createColumns(String[] columnTitles) {
        TableColumn<RegisterEntry, String> nameCol = createColumn(columnTitles[0], "name");
        TableColumn<RegisterEntry, String> valueCol = createColumn(columnTitles[1], "value");

        //noinspection unchecked
        this.getColumns().addAll(nameCol, valueCol);
    }
}