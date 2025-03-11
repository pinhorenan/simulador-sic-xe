package sicxesimulator.application.components.panels;

import javafx.scene.control.TitledPane;
import sicxesimulator.application.components.tables.SymbolTableView;

public class SymbolPanel {
    private final TitledPane symbolPane;
    private final SymbolTableView symbolTable;

    public SymbolPanel() {
        this.symbolTable = new SymbolTableView();
        this.symbolPane = new TitledPane("Símbolos", symbolTable);
        symbolPane.setCollapsible(false);
    }

    public TitledPane getPane() {
        return symbolPane;
    }

    public SymbolTableView getSymbolTable() {
        return symbolTable;
    }
}
