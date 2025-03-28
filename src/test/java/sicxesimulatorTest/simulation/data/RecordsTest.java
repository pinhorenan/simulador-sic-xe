package sicxesimulatorTest.simulation.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import sicxesimulator.simulation.data.records.MemoryEntry;
import sicxesimulator.simulation.data.records.RegisterEntry;
import sicxesimulator.simulation.data.records.SymbolEntry;
import sicxesimulator.simulation.data.ObjectFileTableItem;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.SymbolTable;

import java.util.Collections;

public class RecordsTest {

    @Test
    void testMemoryEntry() {
        MemoryEntry entry = new MemoryEntry("0000", "FFEECC");
        assertEquals("0000", entry.address());
        assertEquals("FFEECC", entry.value());
    }

    @Test
    void testRegisterEntry() {
        RegisterEntry entry = new RegisterEntry("A", "0004D2");
        assertEquals("A", entry.registerName());
        assertEquals("0004D2", entry.value());
    }

    @Test
    void testSymbolEntry() {
        SymbolEntry entry = new SymbolEntry("LABEL", "001000");
        assertEquals("LABEL", entry.symbol());
        assertEquals("001000", entry.address());
    }

    @Test
    void testObjectFileTableItem_SimpleModule() {
        // Cria um SymbolTable dummy com um símbolo
        SymbolTable symTable = new SymbolTable();
        symTable.addSymbol("SYM", 0x1000, true);
        // Cria um ObjectFile dummy
        ObjectFile objFile = new ObjectFile(0x1000, new byte[]{0x01, 0x02, 0x03}, symTable, "TestProg", Collections.singletonList("line1"), Collections.emptySet(), Collections.emptyList());
        // Define explicitamente a origem como SINGLE_MODULE
        objFile.setOrigin(ObjectFile.ObjectFileOrigin.SINGLE_MODULE);

        ObjectFileTableItem item = new ObjectFileTableItem(objFile);
        assertEquals("TestProg", item.getProgramName());
        assertTrue(item.getSize().contains("bytes"));
        assertEquals("Módulo Simples", item.getOrigin());
    }


    @Test
    void testObjectFileTableItem_CompositeModule() {
        // Cria um SymbolTable dummy
        SymbolTable symTable = new SymbolTable();
        symTable.addSymbol("SYM", 0x2000, false);
        // Cria um ObjectFile dummy com origem definida como LINKED_MODULES
        ObjectFile objFile = new ObjectFile(0x2000, new byte[]{0x04,0x05,0x06}, symTable, "CompositeProg", Collections.singletonList("lineA"), Collections.emptySet(), Collections.emptyList());
        objFile.setOrigin(ObjectFile.ObjectFileOrigin.LINKED_MODULES);
        ObjectFileTableItem item = new ObjectFileTableItem(objFile);
        assertEquals("CompositeProg", item.getProgramName());
        assertTrue(item.getSize().contains("bytes"));
        assertEquals("Módulo Composto", item.getOrigin());
    }
}
