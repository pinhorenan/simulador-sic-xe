package sicxesimulator.ui.components.panels;

import javafx.scene.control.TitledPane;
import sicxesimulator.ui.components.tables.RegisterTable;

public class RegisterPanel {
    private final TitledPane registerPane;
    private final RegisterTable registerTable;

    public RegisterPanel() {
        this.registerTable = new RegisterTable();
        this.registerPane = new TitledPane("Registradores", registerTable);
        registerPane.setCollapsible(false);
        registerPane.setPrefHeight(150);
    }

    public TitledPane getPane() {
        return registerPane;
    }

    public RegisterTable getRegisterTable() {
        return registerTable;
    }
}
