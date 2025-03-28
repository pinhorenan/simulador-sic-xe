package sicxesimulatorTest.simulation.view;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sicxesimulator.simulation.controller.Controller;
import sicxesimulator.simulation.model.Model;
import sicxesimulator.simulation.view.Layout;
import sicxesimulator.simulation.view.ViewUpdater;
import sicxesimulator.simulation.data.records.MemoryEntry;
import sicxesimulator.simulation.data.records.RegisterEntry;
import sicxesimulator.simulation.data.records.SymbolEntry;

public class ViewUpdaterTest {

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
    }

    // Dummy Controller que retorna listas fixas para os testes
    private static class DummyController extends Controller {
        public DummyController(Model model, Layout layout) {
            super(model, layout);
        }
        @Override
        public List<MemoryEntry> getMemoryEntries() {
            return List.of(new MemoryEntry("000000", "ABCDEF"));
        }
        @Override
        public List<RegisterEntry> getRegisterEntries() {
            return List.of(new RegisterEntry("A", "000123"));
        }
        @Override
        public List<SymbolEntry> getSymbolEntries() {
            return List.of(new SymbolEntry("SYM", "001000"));
        }
    }

    @Test
    void testUpdateAllTables() throws InterruptedException {
        Model model = new Model();
        Layout layout = new Layout();
        DummyController controller = new DummyController(model, layout);
        ViewUpdater updater = new ViewUpdater(controller, layout);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            updater.updateAllTables();
            latch.countDown();
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout waiting for Platform.runLater");

        ObservableList<?> memItems = layout.getMemoryPanel().getMemoryTable().getItems();
        ObservableList<?> regItems = layout.getRegisterPanel().getRegisterTable().getItems();
        ObservableList<?> symItems = layout.getSymbolPanel().getSymbolTable().getItems();

        assertFalse(memItems.isEmpty(), "Memory table should not be empty");
        assertFalse(regItems.isEmpty(), "Register table should not be empty");
        assertFalse(symItems.isEmpty(), "Symbol table should not be empty");
    }

    @Test
    void testUpdateAllLabels() {
        Model model = new Model();
        Layout layout = new Layout();
        // Crie um DummyController com model e layout e injete-o no layout
        DummyController controller = new DummyController(model, layout);
        layout.setController(controller);  // importante: setController() para que mainController não seja nulo
        ViewUpdater updater = new ViewUpdater(controller, layout);
        // Inicializa o LabelsPanel
        layout.updateLabelsPanel();
        // Agora atualize os labels
        updater.updateAllLabels();
    }
}
