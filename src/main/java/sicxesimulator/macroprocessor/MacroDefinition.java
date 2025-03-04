package sicxesimulator.macroprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a definição de uma macro, contendo seu nome, parâmetros e corpo (lista de linhas).
 */

public class MacroDefinition {
    private String name;
    private final List<String> parameters;
    private final List<String> body;

    public MacroDefinition() {
        this.parameters = new ArrayList<>();
        this.body = new ArrayList<>();
    }

    /**
     * Analisa a linha de cabeçalho de macro, extraindo o nome e os parâmetros.
     * Exemplo de header esperado:
     *  "MACRONAME &ARG1, &ARG2"
     *
     * @param headerLine a linha com o nome e os parâmetros da macro.
     */
    public void parseHeader(String headerLine) {
        // Remove espaços em branco e divide a linha em duas partes:
        //  - a primeira parte é o nome da macro;
        //  - a segunda (opcional) contém os parâmetros, separados por vírgula.
        String[] tokens = headerLine.trim().split("\\s+", 2);
        this.name = tokens[0];
        if (tokens.length > 1) {
            // Se houver parâmetros, separa-os por vírgula e remove espaços extras
            String paramsPart = tokens[1];
            String[] params = paramsPart.split(",");
            for (String param : params) {
                this.parameters.add(param.trim());
            }
        }
    }

    /**
     * Adiciona uma linha ao corpo do macro.
     *
     * @param line - a linha a ser adicionada.
     */
    public void addLine(String line) {
        body.add(line);
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<String> getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "MacroDefinition [name=" + name + ", parameters=" + parameters + ", body=" + body + "]";
    }
}
