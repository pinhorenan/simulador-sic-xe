package sicxesimulator.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sicxesimulator.components.Memory;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    private Memory memory;

    @BeforeEach
    void setUp() {
        memory = new Memory();
    }

    @Test
    void testInitialMemoryIsZero() {
        // Verifica se todas as posições estão zeradas (ou "000000", dependendo do default)
        for (int i = 0; i < memory.getSize(); i++) {
            assertEquals("000000", memory.read(i), "A memória deve iniciar zerada (ou com valor default).");
        }
    }

    @Test
    void testWriteAndRead() {
        memory.write(10, "123456");
        assertEquals("123456", memory.read(10), "Deve retornar o valor escrito.");
    }

    @Test
    void testSetByte() {
        // Supondo que memory.setByte(10, "FF") substitua apenas o último byte
        memory.write(10, "123456");
        memory.setByte(10, "AB");
        assertEquals("1234AB", memory.read(10), "Apenas o último byte deve ser substituído.");
    }

    @Test
    void testOutOfBounds() {
        // Se a classe Memory não fizer checagem, você pode querer garantir que
        // não ocorra IndexOutOfBoundsException ou retorne null.
        assertThrows(IndexOutOfBoundsException.class, () -> memory.read(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> memory.read(memory.getSize()));
    }
}
