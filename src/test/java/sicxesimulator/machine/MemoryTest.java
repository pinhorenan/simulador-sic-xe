package sicxesimulator.machine;

import org.junit.jupiter.api.Test;
import sicxesimulator.hardware.Memory;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTest {

    @Test
    public void testMemorySizeBelowMinimum() {
        // Como a nova implementação não lança exceção para tamanhos abaixo de um mínimo,
        // apenas criamos a memória e verificamos o tamanho.
        Memory mem = new Memory(512);
        assertEquals(512, mem.getSize());
    }

    @Test
    public void testGetSizeInBytes() {
        Memory mem = new Memory(1024);
        assertEquals(1024, mem.getSize());
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
        // Usando endereço em bytes diretamente. Por exemplo, escrevemos no byte de índice 5.
        int byteAddr = 5;
        int value = 0xAB;
        mem.writeByte(byteAddr, value);
        int readValue = mem.readByte(byteAddr);
        assertEquals(value, readValue);
    }

    @Test
    public void testMemoryMapOnlyNonZero() {
        Memory mem = new Memory(1024);
        // Escreve uma palavra com valor não zero em uma posição.
        // Neste exemplo, escrevemos {0x00, 0x0F, 0x00} na palavra de índice 2.
        byte[] data = {0x00, 0x0F, 0x00};
        int wordAddress = 2;
        mem.writeWord(wordAddress, data);

        byte[] map = mem.getMemoryMap();

        // Calcula o endereço em bytes do segundo byte da palavra 2
        int targetAddress = wordAddress * 3 + 1;

        // Verifica que o byte no targetAddress é 0x0F e todos os outros bytes são 0.
        for (int i = 0; i < map.length; i++) {
            int val = map[i] & 0xFF;
            if (i == targetAddress) {
                assertEquals(0x0F, val, "Valor no endereço " + i + " deve ser 0x0F");
            } else {
                assertEquals(0, val, "Valor no endereço " + i + " deve ser 0");
            }
        }
    }
}
