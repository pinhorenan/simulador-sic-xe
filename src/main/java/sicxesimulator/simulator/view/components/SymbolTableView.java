package sicxesimulator.simulator.view.components;

import javafx.scene.control.TableColumn;
import sicxesimulator.simulator.view.SimulationApp.SymbolEntry;

public class SymbolTableView extends BaseTableView<SymbolEntry> {

    public SymbolTableView() {
        super("Símbolo", "Endereço");
    }

    @Override
    protected void createColumns(String[] columnTitles) {
        TableColumn<SymbolEntry, String> symbolCol = createColumn(columnTitles[0], "symbol");
        TableColumn<SymbolEntry, String> addressCol = createColumn(columnTitles[1], "address");

        this.getColumns().addAll(symbolCol, addressCol);
    }
}