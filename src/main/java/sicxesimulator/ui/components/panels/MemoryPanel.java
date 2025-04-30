package sicxesimulator.ui.components.panels;

import javafx.scene.control.TitledPane;
import sicxesimulator.ui.components.factories.TablePanelFactory;
import sicxesimulator.ui.components.tables.MemoryTable;

public class MemoryPanel {
    private final TitledPane pane;
    private final MemoryTable table = new MemoryTable();
    public MemoryPanel() {
        pane = TablePanelFactory.build("Memória", table, 150);
    }
    public TitledPane getPane()   { return pane; }
    public MemoryTable getTable() { return table; }
}

