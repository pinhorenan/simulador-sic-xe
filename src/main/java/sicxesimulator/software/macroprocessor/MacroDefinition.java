package sicxesimulator.software.macroprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa a definição de uma macro, agora com suporte a parâmetros*.
 */
class MacroDefinition {
    private final String name;
    private final List<String> parameters;
    private final List<String> body;

    public MacroDefinition(String name, List<String> parameters) {
        this.name = name;
        this.parameters = parameters;
        this.body = new ArrayList<>();
    }

    /// ===== Métodos Getters =====

    public String getName() {
        return name;
    }

    public List<String> getBody() {
        return body;
    }

    public List<String> getParameters() {
        return parameters;
    }

    /**
     * Adiciona uma linha ao corpo da macro.
     * @param line Linha a ser adicionada.
     */
    public void addLine(String line) {
        body.add(line);
    }
}
