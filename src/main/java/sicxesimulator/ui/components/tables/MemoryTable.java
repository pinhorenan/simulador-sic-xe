package sicxesimulator.ui.components.tables;

import sicxesimulator.ui.data.memory.MemoryEntry;

public class MemoryTable extends BaseTableView<MemoryEntry> {
    public MemoryTable() {
        super("Endereço:address", "Valor:value");
    }
}