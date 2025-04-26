package sicxesimulator.ui.view;

import javafx.application.Platform;
import sicxesimulator.ui.controller.Controller;
import sicxesimulator.ui.data.records.SymbolEntry;
import sicxesimulator.ui.components.tables.MemoryTable;
import sicxesimulator.ui.components.tables.RegisterTable;
import sicxesimulator.ui.components.tables.SymbolTable;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.common.utils.Convert;

/**
 * Classe responsável por atualizar dinamicamente a interface gráfica (JavaFX).
 *
 * <p>Sincroniza os dados da memória, registradores e símbolos com os componentes visuais,
 * garantindo que os dados apresentados ao usuário reflitam o estado atual do simulador.</p>
 *
 * <p>Utiliza {@link Platform#runLater} para garantir que as atualizações sejam executadas na thread de UI.</p>
 *
 * <p>Também atualiza rótulos de status como tamanho da memória e modo de ligação (linker).</p>
 */
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
            symbolTable.getItems().add(new SymbolEntry(name, Convert.intToHexString24((byteAddress))));
        });
    }

    public void updateAllLabels() {
        updateMemorySizeLabel();
        updateLinkerModeLabel();
    }

    public void updateMemorySizeLabel() {
        mainLayout.getLabelsPanel().updateMemorySizeLabel();
    }

    public void updateLinkerModeLabel() {
        mainLayout.getLabelsPanel().updateLinkerModeLabel();
    }

}
