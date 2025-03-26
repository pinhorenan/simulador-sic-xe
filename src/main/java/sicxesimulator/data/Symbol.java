package sicxesimulator.data;

import java.io.Serializable;

/**
 * Representa um símbolo na tabela de símbolos de um módulo.
 */
public class Symbol implements Serializable {
    public final String name;
    public int address;
    public boolean isPublic;

    public Symbol(String name, int address, boolean isPublic) {
        this.name = name;
        this.address = address;
        this.isPublic = isPublic;
    }

    @Override
    public String toString() {
        return String.format("%s@%04X%s", name, address, isPublic ? "(public)" : "");
    }
}