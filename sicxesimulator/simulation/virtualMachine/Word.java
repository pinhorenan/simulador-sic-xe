package sicxesimulator.simulation.virtualMachine;

public class Word {

    private String value;
    // TODO
    // Deixar de trabalhar com inteiros e achar uma representação melhor para os bits na palavra.

    public Word() {
        this.value = "";
    }

    public Word(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
