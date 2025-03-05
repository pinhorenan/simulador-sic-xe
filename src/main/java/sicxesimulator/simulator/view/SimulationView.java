package sicxesimulator.simulator.view;

import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import sicxesimulator.simulator.view.SimulationApp.RegisterEntry;
import sicxesimulator.simulator.view.SimulationApp.MemoryEntry;
import sicxesimulator.simulator.view.SimulationApp.SymbolEntry;

public interface SimulationView {
    // Atualizações de UI
    void updateAllTables();
    void updateMemoryTable();
    void updateRegisterTable();
    void updateSymbolTable();

    // Gerenciamento de saída
    void appendOutput(String message);
    void clearOutput();

    // Gerenciamento de entrada
    String getInputText();
    void clearInput();

    // Controle de tabelas
    void clearTables();
    TableView<RegisterEntry> getRegisterTable();
    TableView<MemoryEntry> getMemoryTable();
    TableView<SymbolEntry> getSymbolTable();

    // Diálogos e alertas
    void showError(String message);
    void showAlert(Alert.AlertType type, String title, String header, String content);
    void showHelpWindow();

    // Configuração de visualização
    void setViewFormat(String format);
    void updateViewFormatLabel(String formatName);

    // Controle da janela
    Stage getStage();
    void setWindowTitle(String title);

    // Controle de execução
    void disableControls(boolean disable);

    // Reset completo
    void fullReset();
}