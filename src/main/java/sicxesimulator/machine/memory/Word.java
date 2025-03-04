package sicxesimulator.machine.memory;

public class Word {
    private byte[] value = new byte[3];
    private final int address;

    public Word(int address) {
        this.address = address;
    }

    ///  GETTERS

    public byte[] getValue() {
        return value;
    }

    public int getAddress() {
        return address;
    }

    /// SETTERS

    public void setValue(byte[] value) {
        this.value = value;
    }
}
