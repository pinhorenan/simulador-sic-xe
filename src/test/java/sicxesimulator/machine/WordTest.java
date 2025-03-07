package sicxesimulator.machine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sicxesimulator.machine.memory.Word;

public class WordTest {

    @Test
    public void testSetAndGetValue() {
        Word word = new Word(0);
        byte[] value = {0x10, 0x20, 0x30};
        word.setValue(value);
        byte[] retrieved = word.getValue();
        assertArrayEquals(value, retrieved);
    }

    @Test
    public void testSetInvalidValueLength() {
        Word word = new Word(0);
        byte[] invalid = {0x01, 0x02}; // menos de 3 bytes

        // Substituímos @Test(expected = IllegalArgumentException.class)
        // por assertThrows (JUnit 5)
        assertThrows(IllegalArgumentException.class, () -> word.setValue(invalid));
    }
}
