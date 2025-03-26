package sicxesimulatorTest.utils;

import org.junit.jupiter.api.Test;
import sicxesimulator.utils.Converter;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void testIntTo3BytesAndBack() {
        int original = 0x123456;
        byte[] bytes = Converter.intTo3Bytes(original);
        assertEquals(original, Converter.bytesToInt(bytes));
    }

    @Test
    void testIntToBinaryString24() {
        int value = 5;
        String binary = Converter.intToBinaryString24(value);
        assertEquals(24, binary.length());
        assertTrue(binary.matches("[01]+"));
    }

    @Test
    void testLongToBinaryString48() {
        long value = 0x123456789AL;
        String binary = Converter.longToBinaryString48(value);
        assertEquals(48, binary.length());
    }

    @Test
    void testHexStringToByteArrayValid() {
        String hex = "1A 2B 3C";
        byte[] result = Converter.hexStringToByteArray(hex);
        assertEquals(3, result.length);
        assertEquals("1A2B3C", Converter.bytesToHex(result));
    }

    @Test
    void testHexStringToByteArrayOddLength() {
        String hex = "ABC"; // deve ser interpretado como "0ABC"
        byte[] result = Converter.hexStringToByteArray(hex);
        assertEquals("0ABC", Converter.bytesToHex(result));
    }

    @Test
    void testHexStringToByteArrayInvalid() {
        String hex = "ZZ";
        assertThrows(IllegalArgumentException.class, () -> Converter.hexStringToByteArray(hex));
    }

    @Test
    void testBytesToBinaryString() {
        byte[] bytes = { (byte)0xAA, (byte)0x55 };
        String binary = Converter.bytesToBinaryString(bytes);
        assertEquals("1010101001010101", binary);
    }

    @Test
    void testBytesToIntInvalidArray() {
        byte[] invalid = { 0x00, 0x01 };
        assertThrows(IllegalArgumentException.class, () -> Converter.bytesToInt(invalid));
    }
}
