package sicxesimulator.simulator.view.components.tables;

import javafx.scene.control.TableColumn;
import sicxesimulator.simulator.model.records.SymbolEntry;

public class SymbolTableView extends BaseTableView<SymbolEntry> {

    public SymbolTableView() {
        super("Símbolo", "Endereço");
    }

    @Override
    protected void createColumns(String[] columnTitles) {
        TableColumn<SymbolEntry, String> symbolCol = createColumn(columnTitles[0], "symbol");
        TableColumn<SymbolEntry, String> addressCol = createColumn(columnTitles[1], "address");

        //noinspection unchecked
        this.getColumns().addAll(symbolCol, addressCol);
    }
}