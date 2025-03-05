package sicxesimulator.simulator.view.components;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyStringWrapper;

public abstract class BaseTableView<T> extends TableView<T> {

    public BaseTableView(String... columnTitles) {
        super();
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createColumns(columnTitles);
        this.getStyleClass().add("base-table");
    }

    protected abstract void createColumns(String[] columnTitles);

    protected TableColumn<T, String> createColumn(String title, String propertyKey) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            try {
                return new ReadOnlyStringWrapper(
                        cellData.getValue().getClass()
                                .getMethod(propertyKey)
                                .invoke(cellData.getValue())
                                .toString()
                );
            } catch (Exception e) {
                return new ReadOnlyStringWrapper("");
            }
        });
        return column;
    }
}