package sicxesimulator.machine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sicxesimulator.machine.memory.Memory;
import java.util.Map;

public class MemoryTest {

    @Test
    public void testMemorySizeBelowMinimum() {
        // Em JUnit 5, ao invés de @Test(expected = ...)
        // use assertThrows para verificar exceções.
        assertThrows(IllegalArgumentException.class, () -> new Memory(512));
    }

    @Test
    public void testGetSizeInBytes() {
        Memory mem = new Memory(1024); // 1024 bytes ⇾ 341 palavras (3 bytes por palavra)
        int expectedSize = 341 * 3;
        assertEquals(expectedSize, mem.getSizeInBytes());
    }

    @Test
    public void testWriteAndReadWord() {
        Memory mem = new Memory(1024);
        byte[] data = {0x01, 0x02, 0x03};
        int wordAddress = 10;
        mem.writeWord(wordAddress, data);
        byte[] readData = mem.readWord(wordAddress);
        assertArrayEquals(data, readData);
    }

    @Test
    public void testWriteAndReadByte() {
        Memory mem = new Memory(1024);
        int wordAddress = 5;
        int offset = 1;
        int value = 0xAB;

        mem.writeByte(wordAddress, offset, value);
        int readValue = mem.readByte(wordAddress, offset);
        assertEquals(value, readValue);
    }

    @Test
    public void testMemoryMapOnlyNonZero() {
        Memory mem = new Memory(1024);
        // Escreve um valor não zero em uma palavra
        byte[] data = {0x00, 0x0F, 0x00};
        int wordAddress = 2;
        mem.writeWord(wordAddress, data);

        Map<Integer, Integer> map = mem.getMemoryMap();

        // Somente o byte com valor 0x0F deve estar mapeado
        int byteAddress = wordAddress * 3 + 1;
        assertTrue(map.containsKey(byteAddress));
        assertEquals(0x0F, map.get(byteAddress).intValue());
    }
}
