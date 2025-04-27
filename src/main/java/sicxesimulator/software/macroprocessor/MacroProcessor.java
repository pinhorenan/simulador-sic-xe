package sicxesimulator.software.macroprocessor;

import sicxesimulator.common.utils.Constants;
import sicxesimulator.common.utils.FileUtils;
import sicxesimulator.software.data.MacroDefinition;

import java.io.IOException;
import java.util.*;

/**
 * Processador de macros para o assembler SIC/XE.
 *
 * <p>Detecta definições de macros no código-fonte, armazena-as em tabela interna,
 * expande chamadas de macros substituindo parâmetros por argumentos reais
 * e grava o resultado em arquivo temporário.</p>
 *
 * <p>Fluxo principal:</p>
 * <ol>
 *   <li>Lê o conteúdo do arquivo de entrada.</li>
 *   <li>Analisa definições de macros (MACRO...MEND), populando {@code macroTable}.</li>
 *   <li>Expande cada linha, substituindo invocações de macro por seu corpo.</li>
 *   <li>Filtra linhas em branco e grava o arquivo de saída em {@link Constants#TEMP_DIR}.</li>
 * </ol>
 */
public class MacroProcessor {
    private final Map<String, MacroDefinition> macroTable = new HashMap<>();

    /**
     * Processa o arquivo-fonte, detectando e expandindo macros.
     *
     * @param inputFile  caminho do arquivo de entrada (contém macros)
     * @param outputFile nome do arquivo de saída expandido (ex: "PROG.ASM")
     * @throws IOException              em caso de erro de I/O
     * @throws NullPointerException     se {@code inputFile} ou {@code outputFile} forem nulos
     */
    public void process(String inputFile, String outputFile) throws IOException {
        Objects.requireNonNull(inputFile,  "inputFile não pode ser nulo");
        Objects.requireNonNull(outputFile, "outputFile não pode ser nulo");

        // 1) Leitura
        String content = FileUtils.readFile(inputFile);
        String[] sourceLines = content.split("\\r?\\n", -1);

        // 2) Primeira passagem: coleta definições de macro
        List<String> nonMacroLines = parseDefinitions(sourceLines);

        // 3) Segunda passagem: expande invocações de macro
        List<String> expandedLines = expandAll(nonMacroLines);

        // 4) Filtra linhas em branco
        List<String> filtered = filterEmptyLines(expandedLines);

        // 5) Grava o resultado
        String result = String.join("\n", filtered);
        FileUtils.writeFileInDir(Constants.TEMP_DIR, outputFile, result);
    }

    /**
     * Analisa linhas retroativamente, detecta blocos MACRO...MEND
     * e registra definições em {@code macroTable}.
     *
     * @param lines array de linhas do código-fonte original
     * @return lista de linhas sem as definições de macro
     */
    private List<String> parseDefinitions(String[] lines) {
        macroTable.clear();
        List<String> output = new ArrayList<>();
        Deque<MacroDefinition> stack = new ArrayDeque<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (stack.isEmpty()) {
                // detecta início de definição
                String[] parts = trimmed.split("\\s+", 3);
                if (parts.length >= 2 && parts[1].equalsIgnoreCase("MACRO")) {
                    String name = parts[0].toUpperCase();
                    List<String> params = new ArrayList<>();
                    if (parts.length == 3) {
                        for (String p : parts[2].split(",")) {
                            params.add(p.trim().toUpperCase());
                        }
                    }
                    stack.push(new MacroDefinition(name, params));
                } else {
                    output.add(line);
                }
            } else {
                // dentro de uma definição
                if (trimmed.equalsIgnoreCase("MEND")) {
                    MacroDefinition def = stack.pop();
                    macroTable.put(def.getName().toUpperCase(), def);
                } else {
                    stack.peek().addLine(line);
                }
            }
        }
        return output;
    }

    /**
     * Expande todas as linhas, substituindo invocações de macros
     * pelo corpo definido em {@code macroTable}.
     *
     * @param lines lista de linhas sem definições de macro
     * @return lista de linhas com macros expandidas
     */
    private List<String> expandAll(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            try {
                result.addAll(expandLine(line));
            } catch (Exception e) {
                // falha na expansão: preserva a linha original
                result.add(line);
            }
        }
        return result;
    }

    /**
     * Remove linhas vazias do código expandido.
     *
     * @param lines lista de linhas possivelmente com strings em branco
     * @return nova lista sem linhas vazias
     */
    private List<String> filterEmptyLines(List<String> lines) {
        List<String> filtered = new ArrayList<>();
        for (String l : lines) {
            if (!l.trim().isEmpty()) {
                filtered.add(l);
            }
        }
        return filtered;
    }

    /**
     * Expande uma única linha se ela invoca uma macro registrada.
     *
     * @param line linha de código a processar
     * @return lista de linhas expandidas ou a própria linha se não invocar macro
     */
    private List<String> expandLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return List.of(line);
        }
        String[] tokens = trimmed.split("\\s+", 3);
        String label = null, nameToken;
        List<String> args = Collections.emptyList();

        if (tokens.length == 1) {
            nameToken = tokens[0].toUpperCase();
        } else {
            label = tokens[0];
            nameToken = tokens[1].toUpperCase();
            if (tokens.length == 3) {
                args = Arrays.stream(tokens[2].split(","))
                        .map(String::trim)
                        .toList();
            }
        }

        MacroDefinition macro = macroTable.get(nameToken);
        if (macro == null) {
            return List.of(line);
        }
        return expandMacro(macro, args, label);
    }

    /**
     * Realiza a expansão de uma macro: substitui parâmetros por argumentos
     * e processa recursivamente possíveis chamadas internas.
     *
     * @param macro definição da macro
     * @param args lista de argumentos reais (pode ser vazia)
     * @param label rótulo a prefixar na primeira linha expandida (pode ser nulo)
     * @return linhas resultantes da expansão
     * @throws IllegalArgumentException se a quantidade de args não bater com a de params
     */
    private List<String> expandMacro(MacroDefinition macro,
                                     List<String> args,
                                     String label) {
        List<String> expanded = new ArrayList<>();
        List<String> params = macro.getParameters();

        if (args.size() != params.size()) {
            throw new IllegalArgumentException(
                    "Número de argumentos (" + args.size() +
                            ") não corresponde a parâmetros (" + params.size() + ")");
        }
        Map<String,String> map = new HashMap<>();
        for (int i = 0; i < params.size(); i++) {
            map.put(params.get(i), args.get(i));
        }

        boolean first = true;
        for (String bodyLine : macro.getBody()) {
            String out = bodyLine;
            for (Map.Entry<String,String> e : map.entrySet()) {
                out = out.replace(e.getKey(), e.getValue());
            }
            if (first && label != null && !label.isEmpty()) {
                out = label + " " + out;
                first = false;
            }
            expanded.addAll(expandLine(out));
        }
        return expanded;
    }
}
