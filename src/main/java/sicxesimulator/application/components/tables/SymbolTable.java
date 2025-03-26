package sicxesimulator.application.components.tables;

import sicxesimulator.application.model.data.records.SymbolEntry;

public class SymbolTable extends BaseTableView<SymbolEntry> {
    public SymbolTable() {
        super(SymbolEntry.class, "Símbolo:symbol", "Endereço:address");
    }
}