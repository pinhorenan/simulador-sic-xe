package sicxesimulator.assembler.models;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Integer> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
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
