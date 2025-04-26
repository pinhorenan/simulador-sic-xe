package sicxesimulator.ui.components.tables;

import sicxesimulator.ui.data.records.RegisterEntry;

public class RegisterTable extends BaseTableView<RegisterEntry> {
    public RegisterTable() {
        super("Registrador:registerName", "Valor:value");
    }
}