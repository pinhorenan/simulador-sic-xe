package sicxesimulatorTest.software.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.software.data.Symbol;

public class SymbolTableTest {

    @Test
    void testAddAndGetSymbol() {
        SymbolTable table = new SymbolTable();
        table.addSymbol("SYM1", 0x1000, true);
        table.addSymbol("SYM2", 0x2000, false);

        Symbol sym1 = table.getSymbolInfo("SYM1");
        Symbol sym2 = table.getSymbolInfo("SYM2");

        assertNotNull(sym1);
        assertNotNull(sym2);

        assertEquals("SYM1", sym1.name);
        assertEquals(0x1000, sym1.address);
        assertTrue(sym1.isPublic);

        assertEquals("SYM2", sym2.name);
        assertEquals(0x2000, sym2.address);
        assertFalse(sym2.isPublic);
    }

    @Test
    void testGetSymbolAddress() {
        SymbolTable table = new SymbolTable();
        table.addSymbol("A", 0x1234, true);
        assertEquals(0x1234, table.getSymbolAddress("A"));
        assertNull(table.getSymbolAddress("NON_EXISTENT"));
    }

    @Test
    void testContainsAndToString() {
        SymbolTable table = new SymbolTable();
        table.addSymbol("X", 0xAAAA, false);
        assertTrue(table.contains("X"));
        String str = table.toString();
        // Verifica se a string contém o nome do símbolo e o endereço em formato hexadecimal.
        assertTrue(str.contains("X"));
        assertTrue(str.toUpperCase().contains("AAAA"));
    }

    @Test
    void testGetAllSymbols() {
        SymbolTable table = new SymbolTable();
        table.addSymbol("S1", 0x1111, true);
        table.addSymbol("S2", 0x2222, false);
        assertEquals(2, table.getAllSymbols().size());
    }
}
