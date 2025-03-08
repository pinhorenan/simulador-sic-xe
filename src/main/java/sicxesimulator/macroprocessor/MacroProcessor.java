package sicxesimulator.macroprocessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Processador de macros para o simulador SIC/XE.
 * Realiza a expansão de macros em uma única passagem.
 * Recebe um arquivo fonte e gera um arquivo de saída com as macros expandidas,
 * com o nome MASMAPRG.ASM (ou conforme definido).
 */
public class MacroProcessor {
    private static final Logger logger = Logger.getLogger(MacroProcessor.class.getName());

    // Tabela global de macros: nome (maiúsculo) -> MacroDefinition
    private final Map<String, MacroDefinition> macroTable = new HashMap<>();

    /**
     * Processa o arquivo de entrada e gera o arquivo de saída com macros expandidas.
     * @param inputFile Nome do arquivo fonte de entrada.
     * @param outputFile Nome do arquivo de saída, normalmente "MASMAPRG.ASM".
     * @throws IOException Se ocorrer um erro de leitura/escrita.
     */
    public void process(String inputFile, String outputFile) throws IOException {
        logger.info("Iniciando processamento de macros no arquivo: " + inputFile);
        List<String> sourceLines = Files.readAllLines(Paths.get(inputFile), StandardCharsets.UTF_8);
        List<String> outputLines = new ArrayList<>();
        Deque<MacroDefinition> macroStack = new ArrayDeque<>();

        for (String line : sourceLines) {
            logger.fine("Linha lida: " + line);
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                if (macroStack.isEmpty()) {
                    outputLines.add(line);
                } else {
                    macroStack.peek().addLine(line);
                }
                continue;
            }

            // Verifica se a linha é uma definição de macro verificando se o segundo token é "MACRO"
            String[] parts = trimmed.split("\\s+");
            if (parts.length >= 2 && parts[1].equalsIgnoreCase("MACRO")) {
                String macroName = parts[0];
                MacroDefinition macroDef = new MacroDefinition(macroName);
                macroStack.push(macroDef);
                logger.info("Início da definição da macro: " + macroName);
                continue;
            }

            // Detecta fim de definição de macro: "MEND" (case-insensitive)
            if (!macroStack.isEmpty() && trimmed.equalsIgnoreCase("MEND")) {
                MacroDefinition completedMacro = macroStack.pop();
                macroTable.put(completedMacro.getName().toUpperCase(), completedMacro);
                logger.info("Fim da definição da macro: " + completedMacro.getName());
                continue;
            }

            if (!macroStack.isEmpty()) {
                macroStack.peek().addLine(line);
            } else {
                outputLines.add(line);
            }
        }


        // Expansão das macros no corpo principal
        List<String> expandedLines = new ArrayList<>();
        logger.info("Iniciando expansão das linhas do corpo principal.");
        for (String line : outputLines) {
            List<String> expanded = expandLine(line);
            logger.fine("Linha original: \"" + line + "\" expandida para: " + expanded);
            expandedLines.addAll(expanded);
        }

        Files.write(Paths.get(outputFile), expandedLines, StandardCharsets.UTF_8);
        logger.info("Processamento de macros concluído. Arquivo gerado: " + outputFile);
    }

    /**
     * Expande recursivamente uma linha, verificando se a linha deve ser expandida por macro.
     * Se a linha contém apenas um token e esse token é uma macro, expande-a.
     * Se houver mais de um token, assume que o primeiro é um rótulo e o segundo o mnemônico.
     * Se o mnemônico for o nome de uma macro, expande a macro e preserva o rótulo na primeira linha.
     * @param line Linha a ser expandida.
     * @return Lista de linhas resultantes da expansão.
     */
    private List<String> expandLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return List.of(line);
        }
        String[] tokens = trimmed.split("\\s+");
        if (tokens.length == 1) {
            String token = tokens[0].toUpperCase();
            if (macroTable.containsKey(token)) {
                MacroDefinition macro = macroTable.get(token);
                List<String> expanded = new ArrayList<>();
                for (String macroLine : macro.getBody()) {
                    expanded.addAll(expandLine(macroLine));
                }
                logger.info("Expansão da macro: " + token + " -> " + expanded);
                return expanded;
            } else {
                return List.of(line);
            }
        } else {
            // Assume que se houver mais de um token, o primeiro é rótulo e o segundo mnemônico.
            String mnemonic = tokens[1].toUpperCase();
            if (macroTable.containsKey(mnemonic)) {
                MacroDefinition macro = macroTable.get(mnemonic);
                List<String> expanded = new ArrayList<>();
                boolean firstLine = true;
                String label = tokens[0];
                for (String macroLine : macro.getBody()) {
                    List<String> subExpanded = expandLine(macroLine);
                    if (firstLine) {
                        String newLine = label + " " + subExpanded.get(0).trim();
                        expanded.add(newLine);
                        if (subExpanded.size() > 1) {
                            expanded.addAll(subExpanded.subList(1, subExpanded.size()));
                        }
                        firstLine = false;
                    } else {
                        expanded.addAll(subExpanded);
                    }
                }
                logger.info("Expansão da macro: " + mnemonic + " com rótulo \"" + label + "\" -> " + expanded);
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
