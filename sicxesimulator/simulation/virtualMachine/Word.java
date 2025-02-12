package sicxesimulator.simulation.virtualMachine;

public class Word {
    private String value;

    public Word() {
        this.value = "000000"; // Valor padrão para 24 bits.
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
