package sicxesimulator.application.components.panels;

import javafx.scene.control.TitledPane;
import sicxesimulator.application.components.tables.RegisterTableView;

public class RegisterPanel {
    private final TitledPane registerPane;
    private final RegisterTableView registerTable;

    public RegisterPanel() {
        this.registerTable = new RegisterTableView();
        this.registerPane = new TitledPane("Registradores", registerTable);
        registerPane.setCollapsible(false);
        registerPane.setPrefHeight(150); // 🔹 Reduzimos a altura do painel
    }

    public TitledPane getPane() {
        return registerPane;
    }

    public RegisterTableView getRegisterTable() {
        return registerTable;
    }
}
