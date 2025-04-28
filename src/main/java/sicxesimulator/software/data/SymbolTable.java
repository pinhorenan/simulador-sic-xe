package sicxesimulator.software.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabela de símbolos de um módulo ou do programa final.
 * Chaves sempre armazenadas em CAIXA ALTA.
 */
public class SymbolTable implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final Map<String, Symbol> symbols = new HashMap<>();

    /* API pública ---------------------------------------------------------- */

    public void addSymbol(String name, int address, boolean isPublic) {
        symbols.put(name.toUpperCase(), new Symbol(name.toUpperCase(), address, isPublic));
    }

    public boolean contains(String name)              { return symbols.containsKey(name.toUpperCase()); }
    public Symbol  getSymbolInfo(String name)         { return symbols.get(name.toUpperCase()); }
    public Integer getSymbolAddress(String name)      {
        Symbol s = symbols.get(name.toUpperCase());
        return s == null ? null : s.address;
    }
    public Map<String, Symbol> getAllSymbols()        { return Collections.unmodifiableMap(symbols); }

    @Override public String toString()                { return symbols.values().toString(); }
}
