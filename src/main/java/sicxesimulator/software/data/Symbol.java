package sicxesimulator.software.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/** Entrada da {@link SymbolTable}. */
public class Symbol implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public final String  name;      // imutável
    public       int     address;   // ajustável por linker/loader
    public       boolean isPublic;  // true = exportado (EXTDEF)

    public Symbol(String name, int address, boolean isPublic) {
        this.name     = Objects.requireNonNull(name, "name");
        this.address  = address;
        this.isPublic = isPublic;
    }
    @Override public String toString() {
        return String.format("%s@%04X%s", name, address, isPublic ? " (public)" : "");
    }
}
