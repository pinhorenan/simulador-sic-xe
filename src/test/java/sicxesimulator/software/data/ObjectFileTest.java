package sicxesimulator.software.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ObjectFileTest {

    @Test
    void testValidConstructionAndGetters() {
        int startAddress = 0x1000;
        byte[] machineCode = new byte[]{0x01, 0x02, 0x03, 0x04};
        SymbolTable symbolTable = new SymbolTable();
        symbolTable.addSymbol("A", 0x1000, true);
        List<String> rawSourceCode = Arrays.asList("line1", "line2");
        Set<String> importedSymbols = new HashSet<>(Arrays.asList("EXT1", "EXT2"));
        List<RelocationRecord> relocationRecords = new ArrayList<>();

        ObjectFile objFile = new ObjectFile(startAddress, machineCode, symbolTable, "TestProgram", rawSourceCode, importedSymbols, relocationRecords);

        assertEquals(startAddress, objFile.getStartAddress());
        assertEquals(machineCode.length, objFile.getProgramLength());
        assertArrayEquals(machineCode, objFile.getObjectCode());
        assertEquals("TestProgram", objFile.getProgramName());
        assertEquals(rawSourceCode, objFile.getRawSourceCode());
        assertEquals(importedSymbols, objFile.getImportedSymbols());
        assertEquals(relocationRecords, objFile.getRelocationRecords());
        assertFalse(objFile.isFullyRelocated());
        assertEquals(symbolTable, objFile.getSymbolTable());
    }

    @Test
    void testNullParameters() {
        SymbolTable symbolTable = new SymbolTable();
        List<String> rawSource = new ArrayList<>();
        Set<String> imported = new HashSet<>();
        List<RelocationRecord> relocationRecords = new ArrayList<>();
        byte[] validCode = new byte[]{0x01, 0x02, 0x03};

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                new ObjectFile(0, null, symbolTable, "Prog", rawSource, imported, relocationRecords));
        assertTrue(ex.getMessage().contains("Nenhum parâmetro pode ser nulo"));

        ex = assertThrows(IllegalArgumentException.class, () ->
                new ObjectFile(0, validCode, null, "Prog", rawSource, imported, relocationRecords));
        assertTrue(ex.getMessage().contains("Nenhum parâmetro pode ser nulo"));

        ex = assertThrows(IllegalArgumentException.class, () ->
                new ObjectFile(0, validCode, symbolTable, null, rawSource, imported, relocationRecords));
        assertTrue(ex.getMessage().contains("Nenhum parâmetro pode ser nulo"));
    }

    @Test
    void testSettersAndToString() {
        byte[] machineCode = new byte[]{0x0A, 0x0B, 0x0C};
        SymbolTable symbolTable = new SymbolTable();
        List<String> rawSource = List.of("line1");
        Set<String> imported = new HashSet<>();
        List<RelocationRecord> relocationRecords = new ArrayList<>();
        ObjectFile objFile = new ObjectFile(0x2000, machineCode, symbolTable, "MyProgram", rawSource, imported, relocationRecords);

        objFile.setFullyRelocated(true);
        assertTrue(objFile.isFullyRelocated());

        objFile.setOrigin(ObjectFile.ObjectFileOrigin.LINKED_MODULES);
        assertEquals(ObjectFile.ObjectFileOrigin.LINKED_MODULES, objFile.getOrigin());

        String str = objFile.toString();
        assertTrue(str.contains("MyProgram"));
        assertTrue(str.contains("2000"));
        assertTrue(str.contains("bytes"));
    }

    @Test
    void testSerialization() throws IOException {
        int startAddress = 0x3000;
        byte[] machineCode = new byte[]{0x05, 0x06, 0x07};
        SymbolTable symbolTable = new SymbolTable();
        symbolTable.addSymbol("B", 0x3000, false);
        List<String> rawSource = Arrays.asList("source1", "source2");
        Set<String> imported = new HashSet<>(List.of("EXT1"));
        List<RelocationRecord> relocationRecords = new ArrayList<>();

        ObjectFile objFile = new ObjectFile(startAddress, machineCode, symbolTable, "SerialTest", rawSource, imported, relocationRecords);
        objFile.setFullyRelocated(true);
        objFile.setOrigin(ObjectFile.ObjectFileOrigin.SINGLE_MODULE);

        File tempFile = File.createTempFile("objectfile_test", ".obj");
        objFile.saveToFile(tempFile);

        ObjectFile loaded = ObjectFile.loadFromFile(tempFile);

        assertEquals(objFile.getStartAddress(), loaded.getStartAddress());
        assertArrayEquals(objFile.getObjectCode(), loaded.getObjectCode());
        assertEquals(objFile.getProgramName(), loaded.getProgramName());
        assertEquals(objFile.getRawSourceCode(), loaded.getRawSourceCode());
        assertEquals(objFile.getImportedSymbols(), loaded.getImportedSymbols());
        assertEquals(objFile.isFullyRelocated(), loaded.isFullyRelocated());
        assertEquals(objFile.getOrigin(), loaded.getOrigin());

        //noinspection ResultOfMethodCallIgnored
        tempFile.delete();
    }

    @Test
    void testGetObjectCodeAsString_FileNotFound() {
        byte[] machineCode = new byte[]{0x01, 0x02, 0x03};
        SymbolTable symbolTable = new SymbolTable();
        ObjectFile objFile = new ObjectFile(0, machineCode, symbolTable, "NonExistent", null, null, null);
        String result = objFile.getObjectCodeAsString();
        assertEquals("Arquivo .obj não encontrado.", result);
    }
}
