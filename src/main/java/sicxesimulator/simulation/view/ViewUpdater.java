package sicxesimulator.simulation.view;

import javafx.application.Platform;
import sicxesimulator.simulation.controller.Controller;
import sicxesimulator.simulation.model.data.records.SymbolEntry;
import sicxesimulator.simulation.components.tables.MemoryTable;
import sicxesimulator.simulation.components.tables.RegisterTable;
import sicxesimulator.simulation.components.tables.SymbolTable;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.simulation.util.ValueFormatter;

public class ViewUpdater {
    private final Controller controller;
    private final Layout mainLayout;

    public ViewUpdater(Controller controller, Layout mainLayout) {
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
        MemoryTable memoryTable = mainLayout.getMemoryPanel().getMemoryTable();
        memoryTable.getItems().clear();
        memoryTable.getItems().addAll(controller.getMemoryEntries());
    }

    public void updateRegisterTableView() {
        RegisterTable registerTable = mainLayout.getRegisterPanel().getRegisterTable();
        registerTable.getItems().clear();
        registerTable.getItems().addAll(controller.getRegisterEntries());
    }

    public void updateSymbolTableView() {
        SymbolTable symbolTable = mainLayout.getSymbolPanel().getSymbolTable();
        symbolTable.getItems().clear();
        symbolTable.getItems().addAll(controller.getSymbolEntries());
    }

    public void updateSymbolTableView(ObjectFile objectFile) {
        if (objectFile == null) return;

        SymbolTable symbolTable = mainLayout.getSymbolPanel().getSymbolTable();
        symbolTable.getItems().clear();

        var symbolsMap = objectFile.getSymbolTable().getAllSymbols();
        symbolsMap.forEach((name, info) -> {
            int byteAddress = info.address;
            String formattedAddress = ValueFormatter.formatAddress(byteAddress, controller.getModel().getViewConfig().getAddressFormat());
            symbolTable.getItems().add(new SymbolEntry(name, formattedAddress));
        });
    }

    public void updateAllLabels() {
        updateMemorySizeLabel();
        updateAddressFormatLabel();
        updateCycleDelayLabel();
        updateLinkerModeLabel();
    }

    public void updateMemorySizeLabel() {
        mainLayout.getLabelsPanel().updateMemoryLabel();
    }

    public void updateAddressFormatLabel() {
        mainLayout.getLabelsPanel().updateFormatLabel();
    }

    public void updateCycleDelayLabel() {
        mainLayout.getLabelsPanel().updateSpeedLabel();
    }

    public void updateLinkerModeLabel() {
        mainLayout.getLabelsPanel().updateLinkerModeLabel();
    }

}
