package sicxesimulatorTest.hardware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sicxesimulator.hardware.Memory;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    private Memory memory;

    @BeforeEach
    void setUp() {
        // Cria uma memória com 30 bytes para os testes
        memory = new Memory(30);
    }

    @Test
    void testGetSizeAndAddressRange() {
        // 30 bytes => 10 palavras (30/3)
        assertEquals(30, memory.getSize());
        assertEquals(10, memory.getAddressRange());
    }

    @Test
    void testWriteAndReadByte() {
        // Escreve um valor (por exemplo, 300 mod 256 = 44) no endereço 5
        memory.writeByte(5, 300);
        assertEquals(44, memory.readByte(5));
    }

    @Test
    void testWriteByteOutOfBounds() {
        // Endereço 30 está fora dos limites (0 a 29)
        assertThrows(IndexOutOfBoundsException.class, () -> memory.writeByte(30, 100));
    }

    @Test
    void testReadByteOutOfBounds() {
        // Tentar ler no endereço 30 deve lançar exceção
        assertThrows(IndexOutOfBoundsException.class, () -> memory.readByte(30));
    }

    @Test
    void testWriteAndReadWord() {
        // Cria uma palavra com 3 bytes e escreve na palavra de índice 2 (offset 6)
        byte[] word = new byte[]{(byte)0xAA, (byte)0xBB, (byte)0xCC};
        memory.writeWord(2, word);
        byte[] readWord = memory.readWord(2);
        assertArrayEquals(word, readWord);
    }

    @Test
    void testWriteWordInvalidLength() {
        // Tenta escrever uma "palavra" com tamanho diferente de 3
        byte[] invalidWord = new byte[]{0x01, 0x02};
        assertThrows(IllegalArgumentException.class, () -> memory.writeWord(0, invalidWord));
    }

    @Test
    void testWriteWordOutOfBounds() {
        // Com 30 bytes, índice máximo válido é 9 (9*3+3 = 30). Índice 10 deve lançar exceção.
        byte[] word = new byte[]{0x01, 0x02, 0x03};
        assertThrows(IndexOutOfBoundsException.class, () -> memory.writeWord(10, word));
    }

    @Test
    void testClearMemory() {
        // Preenche a memória com 0xFF e depois limpa
        for (int i = 0; i < memory.getSize(); i++) {
            memory.writeByte(i, 0xFF);
        }
        memory.clearMemory();
        byte[] map = memory.getMemoryMap();
        for (int value : map) {
            assertEquals(0, value);
        }
    }

    @Test
    void testGetMemoryMapReturnsCopy() {
        // Escreve um valor e obtém o mapa da memória
        memory.writeByte(0, 0x12);
        byte[] map1 = memory.getMemoryMap();
        // Modifica o array retornado
        map1[0] = 0x34;
        byte[] map2 = memory.getMemoryMap();
        // O valor original deve permanecer inalterado
        assertEquals(0x12, map2[0] & 0xFF);
    }

    @Test
    void testToString() {
        // Preenche a memória com valores crescentes
        for (int i = 0; i < memory.getSize(); i++) {
            memory.writeByte(i, i);
        }
        String memString = memory.toString();
        // Verifica se a string contém a representação hexadecimal de alguns valores
        assertTrue(memString.contains("00"));
        assertTrue(memString.contains("0A")); // 10 em hexadecimal
    }
}
