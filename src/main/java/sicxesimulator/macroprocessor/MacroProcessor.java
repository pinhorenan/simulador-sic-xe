package sicxesimulator.macroprocessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import sicxesimulator.logger.SimulatorLogger;

public class MacroProcessor {

    // Tabela global de macros: nome (maiúsculo) -> MacroDefinition
    private final Map<String, MacroDefinition> macroTable = new HashMap<>();

    /**
     * Processa o arquivo de entrada e gera o arquivo de saída com as macros expandidas.
     * @param inputFile Nome do arquivo-fonte de entrada.
     * @param outputFile Nome do arquivo de saída, normalmente "MASMAPRG.ASM".
     * @throws IOException Se ocorrer um erro de leitura/escrita.
     */
    public void process(String inputFile, String outputFile) throws IOException {
        SimulatorLogger.logExecution("MacroProcessor iniciado. Entrada: " + inputFile);
        List<String> sourceLines = Files.readAllLines(Paths.get(inputFile), StandardCharsets.UTF_8);
        List<String> outputLines = new ArrayList<>();
        Deque<MacroDefinition> macroStack = new ArrayDeque<>();

        // Primeira passagem: identificar e armazenar definições de macro
        for (int i = 0; i < sourceLines.size(); i++) {
            String line = sourceLines.get(i);
            String trimmed = line.trim();

            // Se a linha estiver em branco, preserva conforme o contexto atual
            if (trimmed.isEmpty()) {
                if (macroStack.isEmpty()) {
                    outputLines.add(line);
                } else {
                    macroStack.peek().addLine(line);
                }
                continue;
            }

            String[] parts = trimmed.split("\\s+");
            // Início da definição de macro: ex: "MACRO"
            if (parts.length >= 2 && parts[1].equalsIgnoreCase("MACRO")) {
                String macroName = parts[0];
                MacroDefinition macroDef = new MacroDefinition(macroName);
                SimulatorLogger.logExecution("Definindo macro: " + macroName + " (linha " + (i + 1) + ")");
                macroStack.push(macroDef);
                continue;
            }

            // Fim da definição: "MEND"
            if (!macroStack.isEmpty() && trimmed.equalsIgnoreCase("MEND")) {
                MacroDefinition completedMacro = macroStack.pop();
                macroTable.put(completedMacro.getName().toUpperCase(), completedMacro);
                SimulatorLogger.logExecution("Macro definida: " + completedMacro.getName() + " (linha " + (i + 1) + ")");
                continue;
            }

            // Se estiver em uma definição de macro, adiciona a linha à macro corrente
            if (!macroStack.isEmpty()) {
                macroStack.peek().addLine(line);
            } else {
                // Linha fora de macro: adiciona ao corpo principal
                outputLines.add(line);
            }
        }

        SimulatorLogger.logExecution("Macros registradas: " + macroTable.keySet());

        // Segunda passagem: expansão das macros no corpo principal
        List<String> expandedLines = new ArrayList<>();
        for (int i = 0; i < outputLines.size(); i++) {
            String line = outputLines.get(i);
            try {
                List<String> expanded = expandLine(line);
                expandedLines.addAll(expanded);
            } catch (Exception e) {
                SimulatorLogger.logError("Erro na expansão da linha " + (i + 1) + ": " + line, e);
                // Em caso de erro, preserva a linha original
                expandedLines.add(line);
            }
        }

        Files.write(Paths.get(outputFile), expandedLines, StandardCharsets.UTF_8);
        SimulatorLogger.logExecution("Processamento concluído. Arquivo gerado: " + outputFile);
    }

    /**
     * Expande recursivamente uma linha, tratando chamadas de macro.
     * Se a linha contiver apenas um token e este corresponder a uma macro, expande-a.
     * Se houver mais de um token, assume que o primeiro é rótulo e o segundo, mnemônico.
     * @param line Linha a ser expandida.
     * @return Lista de linhas resultantes da expansão.
     */
    private List<String> expandLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return List.of(line);
        }
        String[] tokens = trimmed.split("\\s+");

        // Caso simples: linha com apenas um token (possível chamada de macro sem rótulo)
        if (tokens.length == 1) {
            String token = tokens[0].toUpperCase();
            if (macroTable.containsKey(token)) {
                MacroDefinition macro = macroTable.get(token);
                List<String> expanded = new ArrayList<>();
                for (String macroLine : macro.getBody()) {
                    expanded.addAll(expandLine(macroLine));
                }
                return expanded;
            } else {
                return List.of(line);
            }
        } else {
            // Caso com rótulo: assume que o primeiro token é rótulo e o segundo, o mnemônico
            String mnemonic = tokens[1].toUpperCase();
            if (macroTable.containsKey(mnemonic)) {
                MacroDefinition macro = macroTable.get(mnemonic);
                List<String> expanded = new ArrayList<>();
                boolean firstLine = true;
                String label = tokens[0];
                for (String macroLine : macro.getBody()) {
                    List<String> subExpanded = expandLine(macroLine);
                    if (firstLine) {
                        // Preserva o rótulo na primeira linha da expansão
                        String newLine = label + " " + subExpanded.getFirst().trim();
                        expanded.add(newLine);
                        if (subExpanded.size() > 1) {
                            expanded.addAll(subExpanded.subList(1, subExpanded.size()));
                        }
                        firstLine = false;
                    } else {
                        expanded.addAll(subExpanded);
                    }
                }
                return expanded;
            } else {
                return List.of(line);
            }
        }
    }

    /**
     * Classe que representa a definição de uma macro.
     */
    private static class MacroDefinition {
        private final String name;
        private final List<String> body;

        public MacroDefinition(String name) {
            this.name = name;
            this.body = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void addLine(String line) {
            body.add(line);
        }

        public List<String> getBody() {
            return body;
        }
    }
}
