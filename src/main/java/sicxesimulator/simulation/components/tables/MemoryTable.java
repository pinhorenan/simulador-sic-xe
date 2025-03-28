package sicxesimulator.simulation.components.tables;

import sicxesimulator.simulation.data.records.MemoryEntry;

public class MemoryTable extends BaseTableView<MemoryEntry> {
    public MemoryTable() {
        super("Endereço:address", "Valor:value");
    }
}