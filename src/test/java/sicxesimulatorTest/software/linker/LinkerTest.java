package sicxesimulatorTest.software.linker;

import org.junit.jupiter.api.Test;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.software.linker.Linker;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class LinkerTest {

    @Test
    void linkModulesWithEmptyModuleList() {
        Linker linker = new Linker();
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                linker.linkModules(Collections.emptyList(), true, 0, "output")
        );
        assertEquals("Nenhum módulo para linkar.", exception.getMessage());
    }

    @Test
    void linkModulesSingleModuleFinalRelocation() {
        ObjectFile module = createDummyModule("MOD1", new byte[]{0x00, 0x01, 0x02});
        Linker linker = new Linker();

        ObjectFile result = linker.linkModules(Collections.singletonList(module), true, 0, "output");

        assertNotNull(result);
        assertArrayEquals(new byte[]{0x00, 0x01, 0x02}, result.getObjectCode());
        assertTrue(result.isFullyRelocated());
    }

    @Test
    void linkModulesMultipleModulesPartialRelocation() {
        ObjectFile module1 = createDummyModule("MOD1", new byte[]{0x01, 0x02});
        ObjectFile module2 = createDummyModule("MOD2", new byte[]{0x03, 0x04});

        Linker linker = new Linker();

        ObjectFile result = linker.linkModules(Arrays.asList(module1, module2), false, 0, "output");

        assertNotNull(result);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, result.getObjectCode());
        assertFalse(result.isFullyRelocated());
        assertEquals(4, result.getObjectCode().length);
    }

    private ObjectFile createDummyModule(String name, byte[] code) {
        SymbolTable symbolTable = new SymbolTable();
        // Se for o segundo módulo, use outro nome, por exemplo "TEST2"
        String symbolName = "MOD2".equals(name) ? "TEST2" : "TEST";
        // Se não quiser duplicar de forma alguma, pode suprimir "isPublic = true"
        symbolTable.addSymbol(symbolName, 0, true);

        return new ObjectFile(
                0,
                code,
                symbolTable,
                name,
                Collections.emptyList(),
                Collections.emptySet(),
                Collections.emptyList()
        );
    }

}
