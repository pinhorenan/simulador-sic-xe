package sicxesimulator.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa a tabela de símbolos de um módulo ou do programa final,
 * permitindo indicar se cada símbolo é público (exportado) ou local (privado).
 */
public class SymbolTable implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, SymbolInfo> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public void addSymbol(String name, int address, boolean isPublic) {
        symbols.put(name, new SymbolInfo(name, address, isPublic));
    }

    public void addSymbol(String name, int address) {
        addSymbol(name, address, false);
    }

    public SymbolInfo getSymbolInfo(String name) {
        return symbols.get(name);
    }

    public Integer getAddress(String name) {
        SymbolInfo info = symbols.get(name);
        return (info != null) ? info.address : null;
    }

    public boolean contains(String name) {
        return symbols.containsKey(name);
    }

    public Map<String, SymbolInfo> getAllSymbols() {
        return symbols;
    }

    @Override
    public String toString() {
        return symbols.toString();
    }

    public static class SymbolInfo implements Serializable {
        public final String name;
        public int address;
        public boolean isPublic;

        public SymbolInfo(String name, int address, boolean isPublic) {
            this.name = name;
            this.address = address;
            this.isPublic = isPublic;
        }

        @Override
        public String toString() {
            return String.format("%s@%04X%s", name, address, isPublic ? "(public)" : "");
        }
    }
}
