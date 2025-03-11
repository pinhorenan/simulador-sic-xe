package sicxesimulator.application.view;

import javafx.application.Platform;
import sicxesimulator.application.controller.Controller;
import sicxesimulator.application.model.records.MemoryEntry;
import sicxesimulator.application.model.records.RegisterEntry;
import sicxesimulator.application.model.records.SymbolEntry;
import sicxesimulator.application.components.tables.MemoryTableView;
import sicxesimulator.application.components.tables.RegisterTableView;
import sicxesimulator.application.components.tables.SymbolTableView;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.utils.ValueFormatter;

import java.util.List;

public class MainViewUpdater {
    private final Controller controller;
    private final MainLayout mainLayout;

    public MainViewUpdater(Controller controller, MainLayout mainLayout) {
        this.controller = controller;
        this.mainLayout = mainLayout;
    }

    public void updateAllTables() {
        Platform.runLater(() -> {
            updateMemoryTableView();
            updateRegisterTableView();
            updateSymbolTableView();
        });
    }

    public void updateMemoryTableView() {
        MemoryTableView memoryTable = mainLayout.getMemoryPanel().getMemoryTable();
        memoryTable.getItems().clear();
        memoryTable.getItems().addAll(controller.getMemoryEntries());
    }

    public void updateRegisterTableView() {
        RegisterTableView registerTable = mainLayout.getRegisterPanel().getRegisterTable();
        registerTable.getItems().clear();
        registerTable.getItems().addAll(controller.getRegisterEntries());
    }

    public void updateSymbolTableView() {
        SymbolTableView symbolTable = mainLayout.getSymbolPanel().getSymbolTable();
        symbolTable.getItems().clear();
        symbolTable.getItems().addAll(controller.getSymbolEntries());
    }

    public void updateSymbolTableView(ObjectFile objectFile) {
        if (objectFile == null) return;

        SymbolTableView symbolTable = mainLayout.getSymbolPanel().getSymbolTable();
        symbolTable.getItems().clear();

        objectFile.getSymbolTable().getSymbols().forEach((name, address) -> {
            int byteAddress = address * 3;
            String formattedAddress = ValueFormatter.formatAddress(byteAddress, controller.getModel().getViewConfig().getAddressFormat());
            symbolTable.getItems().add(new SymbolEntry(name, formattedAddress));
        });
    }

    public void updateAllLabels() {
        updateMemorySizeLabel();
        updateAddressFormatLabel();
        updateCycleDelayLabel();
    }

    public void updateMemorySizeLabel() {
        mainLayout.getBottomBarPanel().updateMemoryLabel();
    }

    public void updateAddressFormatLabel() {
        mainLayout.getBottomBarPanel().updateFormatLabel();
    }

    public void updateCycleDelayLabel() {
        mainLayout.getBottomBarPanel().updateSpeedLabel();
    }

}
