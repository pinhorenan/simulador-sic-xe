package sicxesimulator.application.components.tables;

import sicxesimulator.application.model.records.RegisterEntry;

public class RegisterTableView extends BaseTableView<RegisterEntry> {
    public RegisterTableView() {
        super(RegisterEntry.class, "Registrador:registerName", "Valor:value");
    }
}