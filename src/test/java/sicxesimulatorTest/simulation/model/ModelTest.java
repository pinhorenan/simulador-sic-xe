package sicxesimulatorTest.simulation.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import sicxesimulator.simulation.model.Model;
import sicxesimulator.simulation.data.records.MemoryEntry;
import sicxesimulator.simulation.data.records.RegisterEntry;

public class ModelTest {

    @Test
    void testInitialState() {
        Model model = new Model();
        assertNotNull(model.getMachine(), "Machine should be initialized");
        assertTrue(model.getMemorySize() > 0, "Memory size should be positive");
        // By default, no code loaded
        assertFalse(model.codeLoadedProperty().get(), "Code should not be loaded initially");
    }

    @Test
    void testMemoryEntries() {
        Model model = new Model();
        // Escreve uma palavra na memória da máquina
        model.getMachine().getMemory().writeWord(0, new byte[]{0x0A, 0x0B, 0x0C});
        List<MemoryEntry> entries = model.getMemoryEntries();
        assertFalse(entries.isEmpty(), "Memory entries should not be empty");
        // Verifica que a primeira entrada contém a representação hexadecimal do valor escrito
        String hexValue = entries.getFirst().value().toUpperCase();
        assertTrue(hexValue.contains("0A0B0C"), "Expected memory value 0A0B0C, got " + hexValue);
    }

    @Test
    void testRegisterEntries() {
        Model model = new Model();
        // Seta um valor no registrador A
        model.getMachine().getControlUnit().getRegisterSet().getRegister("A").setValue(1234);
        List<RegisterEntry> regEntries = model.getRegisterEntries();
        // Converte 1234 para hexadecimal (ex: "4D2")
        String expected = Integer.toHexString(1234).toUpperCase();
        boolean found = regEntries.stream().anyMatch(
                re -> re.registerName().equals("A") && re.value().toUpperCase().contains(expected)
        );
        assertTrue(found, "Register entry for A should reflect the set value");
    }

    @Test
    void testSymbolEntriesEmptyWhenNoCode() {
        Model model = new Model();
        List<?> symbols = model.getSymbolEntries();
        assertTrue(symbols.isEmpty(), "Symbol entries should be empty if no code loaded");
    }

    @Test
    void testProperties() {
        Model model = new Model();
        model.setCodeLoaded(true);
        model.setSimulationFinished(true);
        assertTrue(model.codeLoadedProperty().get());
        assertTrue(model.simulationFinishedProperty().get());
    }
}
