package sicxesimulator.ui.components.tables;

import sicxesimulator.ui.data.records.MemoryEntry;

public class MemoryTable extends BaseTableView<MemoryEntry> {
    public MemoryTable() {
        super("Endereço:address", "Valor:value");
    }
}