package sicxesimulator.application.components.panels;

import javafx.scene.control.TitledPane;
import sicxesimulator.application.components.tables.MemoryTableView;

public class MemoryPanel {
    private final TitledPane memoryPane;
    private final MemoryTableView memoryTable;

    public MemoryPanel() {
        this.memoryTable = new MemoryTableView();
        this.memoryPane = new TitledPane("Memória", memoryTable);
        memoryPane.setCollapsible(false);
        memoryPane.setPrefHeight(150); // 🔹 Reduzimos a altura do painel
    }

    public TitledPane getPane() {
        return memoryPane;
    }

    public MemoryTableView getMemoryTable() {
        return memoryTable;
    }
}
