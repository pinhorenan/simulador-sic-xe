package sicxesimulator.simulation.components.tables;

import sicxesimulator.simulation.model.data.records.SymbolEntry;

public class SymbolTable extends BaseTableView<SymbolEntry> {
    public SymbolTable() {
        super("Símbolo:symbol", "Endereço:address");
    }
}