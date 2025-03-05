package sicxesimulator.machine.memory;

import java.util.Arrays;

public class Word {
    private final byte[] value = new byte[3];
    private final int address; // Endereço da palavra (ex: 0, 1, 2...)

    public Word(int address) {
        this.address = address;
    }

    public byte[] getValue() {
        return Arrays.copyOf(value, value.length);
    }

    public void setValue(byte[] newValue) {
        if (newValue.length != 3) {
            throw new IllegalArgumentException("A palavra deve ter 3 bytes.");
        }
        System.arraycopy(newValue, 0, value, 0, 3);
    }
}