package sicxesimulator.application.components.tables;

import sicxesimulator.application.model.records.SymbolEntry;

public class SymbolTableView extends BaseTableView<SymbolEntry> {
    public SymbolTableView() {
        super(SymbolEntry.class, "Símbolo:symbol", "Endereço:address");
    }
}