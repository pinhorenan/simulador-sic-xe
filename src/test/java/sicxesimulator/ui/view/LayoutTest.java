package sicxesimulator.ui.view;

import static org.junit.jupiter.api.Assertions.*;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javafx.scene.layout.BorderPane;
import sicxesimulator.ui.model.Model;
import sicxesimulator.ui.controller.Controller;

public class LayoutTest {

    @BeforeAll
    public static void initJavaFX() {
        // Cria um JFXPanel, que inicializa o toolkit do JavaFX
        new JFXPanel();
    }

    @Test
    void testLayoutComponentsNotNull() {
        Layout layout = new Layout();
        BorderPane root = layout.getRoot();
        assertNotNull(root, "Root should not be null");
        assertNotNull(layout.getMemoryPanel(), "MemoryPanel should not be null");
        assertNotNull(layout.getRegisterPanel(), "RegisterPanel should not be null");
        assertNotNull(layout.getSymbolPanel(), "SymbolPanel should not be null");
        assertNotNull(layout.getInputPanel(), "InputPanel should not be null");
        // ObjectFilePanel may ser nulo até setController ser chamado.
    }

    @Test
    void testSetControllerSetsObjectFilePanel() {
        Layout layout = new Layout();
        Model model = new Model();
        Controller dummyController = new DummyController(model, layout);
        layout.setController(dummyController);
        assertNotNull(layout.getObjectFilePanel(), "ObjectFilePanel should be set after setController");
    }


    // DummyController para teste
    private static class DummyController extends Controller {
        public DummyController(Model model, Layout layout) {
            super(model, layout);
        }
    }

}
