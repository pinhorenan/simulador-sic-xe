package sicxesimulator.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;  // Adicione um serialVersionUID
    private final Map<String, Integer> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public Map<String, Integer> getSymbols() {
        return symbols;
    }

    public void addSymbol(String symbol, int address) {
        symbols.put(symbol, address);
    }

    public Integer getAddress(String symbol) {
        return symbols.get(symbol);
    }

    public boolean contains(String symbol) {
        return symbols.containsKey(symbol);
    }

    @Override
    public String toString() {
        return symbols.toString();
    }
}
