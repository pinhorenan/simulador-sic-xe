package sicxesimulator.software.macroprocessor;

import sicxesimulator.common.utils.Constants;
import sicxesimulator.common.utils.FileUtils;

import java.io.IOException;
import java.util.*;

/**
 * Processador de macros do montador SIC/XE.
 *
 * <p>Responsável por detectar definições de macros, armazená-las e expandi-las
 * no arquivo-fonte. Também realiza substituição de parâmetros formais por argumentos reais.</p>
 */
public class MacroProcessor {
    private final Map<String, MacroDefinition> macroTable = new HashMap<>();

    /**
     * Processa o arquivo-fonte, detectando e expandindo macros.
     *
     * <p>O conteúdo expandido é salvo em {@code outputFile}, dentro de {@link Constants#TEMP_DIR}.</p>
     *
     * @param inputFile Nome do arquivo-fonte de entrada (com macros).
     * @param outputFile Nome do arquivo de saída (ex: "MASMAPRG.ASM").
     * @throws IOException Em caso de falha na leitura ou escrita de arquivos.
     */
    public void process(String inputFile, String outputFile) throws IOException {
        // Limpa definições antigas
        macroTable.clear();

        // Lê o conteúdo do arquivo de entrada utilizando FileUtils
        String inputContent = FileUtils.readFile(inputFile);
        String[] sourceLines = inputContent.split("\\r?\\n");
        List<String> outputLines = new ArrayList<>();
        Deque<MacroDefinition> macroStack = new ArrayDeque<>();

        for (String line : sourceLines) {
            String trimmed = line.trim();

            // Linha vazia: preservar conforme contexto
            if (trimmed.isEmpty()) {
                if (macroStack.isEmpty()) {
                    outputLines.add(line);
                } else {
                    macroStack.peek().addLine(line);
                }
                continue;
            }

            String[] parts = trimmed.split("\\s+", 3);
            // Se a linha tiver pelo menos dois tokens e o segundo for "MACRO", inicia definição
            if (parts.length >= 2 && parts[1].equalsIgnoreCase("MACRO")) {
                String macroName = parts[0].toUpperCase();
                // Se houver terceiro token, trata-o como lista de parâmetros
                List<String> params = new ArrayList<>();
                if (parts.length == 3) {
                    String paramPart = parts[2].trim();
                    // Se os parâmetros estiverem separados por vírgula
                    String[] paramTokens = paramPart.split(",");
                    for (String p : paramTokens) {
                        params.add(p.trim().toUpperCase());
                    }
                }
                MacroDefinition macroDef = new MacroDefinition(macroName, params);
                macroStack.push(macroDef);
                continue;
            }

            // Fim da definição de macro
            if (!macroStack.isEmpty() && trimmed.equalsIgnoreCase("MEND")) {
                MacroDefinition completedMacro = macroStack.pop();
                macroTable.put(completedMacro.getName().toUpperCase(), completedMacro);
                continue;
            }

            // Se estamos em uma definição, adiciona a linha à macro corrente
            if (!macroStack.isEmpty()) {
                macroStack.peek().addLine(line);
            } else {
                // Linha fora de macro: adiciona ao corpo principal
                outputLines.add(line);
            }
        }

        List<String> expandedLines = new ArrayList<>();
        for (String line : outputLines) {
            try {
                List<String> expanded = expandLine(line);
                expandedLines.addAll(expanded);
            } catch (Exception e) {
                // TODO: Melhorar tratamento de erros, inserir um log aqui.
                // Em caso de erro, preserva a linha original
                expandedLines.add(line);
            }
        }

        // Converte a lista de linhas em uma única string separada por quebras de linha, removendo as linhas em branco
        List<String> filtered = new ArrayList<>();
        for (String line : expandedLines) {
            if (!line.trim().isEmpty()) {
                filtered.add(line);
            }
        }

        String expandedContent = String.join("\n", filtered);

        // Salva o arquivo de saída no diretório TEMP_DIR utilizando FileUtils
        FileUtils.writeFileInDir(Constants.TEMP_DIR, outputFile, expandedContent);
        // TODO: Adicionar log de sucesso
    }

    /**
     * Tenta expandir uma linha caso ela seja uma chamada de macro.
     *
     * @param line Linha original do código-fonte.
     * @return Lista de linhas expandidas ou a linha original.
     */
    private List<String> expandLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return List.of(line);
        }
        String[] tokens = trimmed.split("\\s+");
        // Caso simples: linha com apenas um token (possível chamada de macro sem rótulo e sem parâmetros)
        if (tokens.length == 1) {
            String token = tokens[0].toUpperCase();
            if (macroTable.containsKey(token)) {
                MacroDefinition macro = macroTable.get(token);
                return expandMacro(macro, Collections.emptyList(), null);
            } else {
                return List.of(line);
            }
        } else {
            // Linha com pelo menos 2 tokens
            String possibleMacro = tokens[1].toUpperCase();
            if (macroTable.containsKey(possibleMacro)) {
                MacroDefinition macro = macroTable.get(possibleMacro);
                List<String> args = new ArrayList<>();

                // Linha com 3 tokens ou mais
                if (tokens.length >= 3) {
                    String argPart = tokens[2];
                    String[] argTokens = argPart.split(",");
                    for (String a : argTokens) {
                        args.add(a.trim());
                    }
                }
                // Se a linha tiver um rótulo, preserve-o apenas na primeira linha da expansão.
                String label = tokens[0];
                return expandMacro(macro, args, label);
            } else {
                return List.of(line);
            }
        }
    }

    /**
     * Expande uma macro substituindo parâmetros por argumentos reais.
     *
     * @param macro Definição da macro.
     * @param args Argumentos passados na chamada (pode estar vazio).
     * @param label Rótulo opcional que será aplicado à primeira linha expandida.
     * @return Linhas expandidas da macro.
     */
    private List<String> expandMacro(MacroDefinition macro, List<String> args, String label) {
        List<String> expanded = new ArrayList<>();
        Map<String, String> paramMap = new HashMap<>();
        List<String> params = macro.getParameters();

        if (!params.isEmpty()) {
            if (args.size() != params.size()) {
                throw new IllegalArgumentException("Numero de argumentos (" + args.size() +
                        ") nao corresponde ao numero de parametros (" + params.size() + ") para a macro " + macro.getName());
            }
            for (int i = 0; i < params.size(); i++) {
                paramMap.put(params.get(i), args.get(i));
            }
        }
        boolean firstLine = true;
        for (String macroLine : macro.getBody()) {
            String expandedLine = macroLine;
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                expandedLine = expandedLine.replace(entry.getKey(), entry.getValue());
            }
            if (firstLine && label != null && !label.isEmpty()) {
                expandedLine = label + " " + expandedLine;
                firstLine = false;
            }
            expanded.addAll(expandLine(expandedLine));
        }
        return expanded;
    }
}
