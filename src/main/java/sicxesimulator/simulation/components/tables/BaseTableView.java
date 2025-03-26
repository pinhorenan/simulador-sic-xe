package sicxesimulator.simulation.components.tables;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyStringWrapper;

public abstract class BaseTableView<T> extends TableView<T> {

    public BaseTableView(String... columnMappings) {
        super();
        this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        createColumns(columnMappings);
        this.getStyleClass().add("base-table");
    }

    private void createColumns(String... columnMappings) {
        for (String mapping : columnMappings) {
            String[] split = mapping.split(":");
            if (split.length == 2) {
                this.getColumns().add(createColumn(split[0], split[1]));
            }
        }
    }

    protected TableColumn<T, String> createColumn(String title, String propertyKey) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            try {
                Object value = cellData.getValue().getClass().getDeclaredMethod(propertyKey).invoke(cellData.getValue());
                return new ReadOnlyStringWrapper(value != null ? value.toString() : "");
            } catch (ReflectiveOperationException e) {
                System.err.println("Erro ao acessar propriedade: " + propertyKey + " - " + e.getMessage());
                return new ReadOnlyStringWrapper("");
            }
        });
        return column;
    }

}