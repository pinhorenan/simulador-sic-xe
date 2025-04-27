package sicxesimulator.software.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a definição de uma macro no pré-processador do montador SIC/XE.
 *
 * <p>Armazena o nome, os parâmetros formais e o corpo (linhas de código)
 * da macro conforme definida pelo usuário.</p>
 */
public class MacroDefinition {
    private final String name;
    private final List<String> parameters;
    private final List<String> body;

    /**
     * Cria uma nova definição de macro.
     *
     * @param name Nome da macro.
     * @param parameters Lista de parâmetros formais (pode estar vazia).
     */
    public MacroDefinition(String name, List<String> parameters) {
        this.name = name;
        this.parameters = parameters;
        this.body = new ArrayList<>();
    }

    /**
     * Retorna o nome da macro.
     *
     * @return Nome da macro.
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna o corpo da macro, composto por uma lista de linhas.
     *
     * @return Corpo da macro.
     */
    public List<String> getBody() {
        return body;
    }

    /**
     * Retorna a lista de parâmetros da macro.
     *
     * @return Lista de nomes de parâmetros.
     */
    public List<String> getParameters() {
        return parameters;
    }

    /**
     * Adiciona uma linha ao corpo da macro.
     *
     * @param line Linha de código a ser adicionada.
     */
    public void addLine(String line) {
        body.add(line);
    }
}
