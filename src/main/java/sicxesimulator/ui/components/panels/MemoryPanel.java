package sicxesimulator.ui.components.panels;

import javafx.scene.control.TitledPane;
import sicxesimulator.ui.components.tables.MemoryTable;

public class MemoryPanel {
    private final TitledPane memoryPane;
    private final MemoryTable memoryTable;

    public MemoryPanel() {
        this.memoryTable = new MemoryTable();
        this.memoryPane = new TitledPane("Memória", memoryTable);
        memoryPane.setCollapsible(false);
        memoryPane.setPrefHeight(150);
    }

    public TitledPane getPane() {
        return memoryPane;
    }

    public MemoryTable getMemoryTable() {
        return memoryTable;
    }
}
