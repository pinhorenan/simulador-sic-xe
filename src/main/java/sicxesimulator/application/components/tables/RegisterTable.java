package sicxesimulator.application.components.tables;

import sicxesimulator.application.model.records.RegisterEntry;

public class RegisterTable extends BaseTableView<RegisterEntry> {
    public RegisterTable() {
        super(RegisterEntry.class, "Registrador:registerName", "Valor:value");
    }
}