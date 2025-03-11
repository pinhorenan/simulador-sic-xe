package sicxesimulator.application.components.tables;

import sicxesimulator.application.model.records.MemoryEntry;

public class MemoryTableView extends BaseTableView<MemoryEntry> {
    public MemoryTableView() {
        super(MemoryEntry.class, "Endereço:address", "Valor:value");
    }
}