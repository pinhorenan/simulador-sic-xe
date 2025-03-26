package sicxesimulatorTest.simulation.view;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import sicxesimulator.simulation.view.ViewConfig;

public class ViewConfigTest {

    @Test
    void testDefaultAddressFormat() {
        ViewConfig config = new ViewConfig();
        assertEquals("HEX", config.getAddressFormat(), "Default address format should be HEX");
    }

    @Test
    void testSetAddressFormatAndListener() throws Exception {
        ViewConfig config = new ViewConfig();
        // Use reflexão para acessar a lista de listeners
        Field listenersField = ViewConfig.class.getDeclaredField("listeners");
        listenersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ViewConfig.FormatChangeListener> listeners = (List<ViewConfig.FormatChangeListener>) listenersField.get(config);

        final String[] notifiedFormat = { null };
        listeners.add(newFormat -> notifiedFormat[0] = newFormat);
        config.setAddressFormat("DEC");
        assertEquals("DEC", notifiedFormat[0], "Listener should be notified with new format DEC");
    }
}
