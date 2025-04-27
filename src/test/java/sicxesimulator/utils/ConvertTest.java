package sicxesimulator.utils;

import org.junit.jupiter.api.Test;
import sicxesimulator.common.utils.Convert;

import static org.junit.jupiter.api.Assertions.*;

class ConvertTest {

    @Test
    void testIntTo3BytesAndBack() {
        int original = 0x123456;
        byte[] bytes = Convert.intTo3Bytes(original);
        assertEquals(original, Convert.bytesToInt(bytes));
    }

    @Test
    void testIntToBinaryString24() {
        int value = 5;
        String binary = Convert.intToBinaryString24(value);
        assertEquals(24, binary.length());
        assertTrue(binary.matches("[01]+"));
    }

    @Test
    void testLongToBinaryString48() {
        long value = 0x123456789AL;
        String binary = Convert.longToBinaryString48(value);
        assertEquals(48, binary.length());
    }

    @Test
    void testHexStringToByteArrayValid() {
        String hex = "1A 2B 3C";
        byte[] result = Convert.hexStringToByteArray(hex);
        assertEquals(3, result.length);
        assertEquals("1A2B3C", Convert.bytesToHex(result));
    }

    @Test
    void testHexStringToByteArrayOddLength() {
        String hex = "ABC"; // deve ser interpretado como "0ABC"
        byte[] result = Convert.hexStringToByteArray(hex);
        assertEquals("0ABC", Convert.bytesToHex(result));
    }

    @Test
    void testHexStringToByteArrayInvalid() {
        String hex = "ZZ";
        assertThrows(IllegalArgumentException.class, () -> Convert.hexStringToByteArray(hex));
    }

    @Test
    void testBytesToBinaryString() {
        byte[] bytes = { (byte)0xAA, (byte)0x55 };
        String binary = Convert.bytesToBinaryString(bytes);
        assertEquals("1010101001010101", binary);
    }

    @Test
    void testBytesToIntInvalidArray() {
        byte[] invalid = { 0x00, 0x01 };
        assertThrows(IllegalArgumentException.class, () -> Convert.bytesToInt(invalid));
    }
}
