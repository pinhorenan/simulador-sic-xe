package sicxesimulator.simulation.components.tables;

import sicxesimulator.simulation.data.records.RegisterEntry;

public class RegisterTable extends BaseTableView<RegisterEntry> {
    public RegisterTable() {
        super("Registrador:registerName", "Valor:value");
    }
}