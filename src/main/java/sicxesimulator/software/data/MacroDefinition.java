package sicxesimulator.software.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa uma macro definida no código-fonte SIC/XE.
 *
 * <p>Cada macro mantém:</p>
 * <ul>
 *   <li><b>name</b> – identificador único (armazenado em CAIXA ALTA);</li>
 *   <li><b>parameters</b> – lista imutável de parâmetros formais (ex.: <code>A,B,C</code>);</li>
 *   <li><b>body</b> – linhas de código que compõem o corpo da macro.<br>
 *       O corpo é construído incrementalmente pelo pré-processador,
 *       mas é exposto externamente como coleção <em>somente-leitura</em>.</li>
 * </ul>
 *
 * <p>Após criada, apenas o corpo pode receber novas linhas através de
 * {@link #addLine(String)}. Nome e parâmetros permanecem imutáveis.</p>
 */
public final class MacroDefinition implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final String       name;
    private final List<String> parameters;          // lista imutável
    private final List<String> body = new ArrayList<>();

    /**
     * Cria uma macro vazia.
     *
     * @param name       nome da macro (não nulo)
     * @param parameters lista de parâmetros formais; se {@code null} equivale a lista vazia
     * @throws NullPointerException se {@code name} for nulo
     */
    public MacroDefinition(String name, List<String> parameters) {
        this.name       = Objects.requireNonNull(name, "name");
        this.parameters = List.copyOf(parameters == null ? List.of() : parameters);
    }

    /* ------------------------------------------------------------------ */
    /*  Getters                                                           */
    /* ------------------------------------------------------------------ */

    public String        getName()        { return name; }
    public List<String>  getParameters()  { return parameters; }
    public List<String>  getBody()        { return Collections.unmodifiableList(body); }

    /* ------------------------------------------------------------------ */
    /*  Mutação controlada                                                */
    /* ------------------------------------------------------------------ */

    /** Adiciona uma linha crua ao corpo da macro. */
    public void addLine(String line)      { body.add(line); }

    /* ------------------------------------------------------------------ */

    @Override public String toString() {
        return "Macro[" + name +
                ", params=" + parameters +
                ", lines="  + body.size() + ']';
    }
}
