package sicxesimulator.ui.components.tables;

import sicxesimulator.ui.data.memory.SymbolEntry;

public class SymbolTable extends BaseTableView<SymbolEntry> {
    public SymbolTable() {
        super("Símbolo:symbol", "Endereço:address");
    }
}