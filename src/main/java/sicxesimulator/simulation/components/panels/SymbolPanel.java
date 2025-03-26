package sicxesimulator.simulation.components.panels;

import javafx.scene.control.TitledPane;
import sicxesimulator.simulation.components.tables.SymbolTable;

public class SymbolPanel {
    private final TitledPane symbolPane;
    private final SymbolTable symbolTable;

    public SymbolPanel() {
        this.symbolTable = new SymbolTable();
        this.symbolPane = new TitledPane("Tabela de Símbolos", symbolTable);
        symbolPane.setCollapsible(false);
    }

    public TitledPane getPane() {
        return symbolPane;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
