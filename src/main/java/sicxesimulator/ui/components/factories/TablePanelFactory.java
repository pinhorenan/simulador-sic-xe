package sicxesimulator.ui.components.factories;

import javafx.scene.control.TitledPane;
import javafx.scene.control.TableView;

public final class TablePanelFactory {
    private TablePanelFactory() {}

    /**
     * Empacota qualquer TableView dentro de um TitledPane pronto.
     *
     * @param title      título do painel
     * @param table      instância de TableView já configurada
     * @param prefHeight altura preferida (0 para ignorar)
     */
    public static <T extends TableView<?>> TitledPane build(
            String title, T table, double prefHeight) {

        TitledPane pane = new TitledPane(title, table);
        pane.setCollapsible(false);
        if (prefHeight > 0) pane.setPrefHeight(prefHeight);
        return pane;
    }
}
