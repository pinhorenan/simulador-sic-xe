package sicxesimulator.simulation.components.tables;

import sicxesimulator.simulation.model.data.records.MemoryEntry;

public class MemoryTable extends BaseTableView<MemoryEntry> {
    public MemoryTable() {
        super(MemoryEntry.class, "Endereço:address", "Valor:value");
    }
}