package sicxesimulator.macroprocessor;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MacroProcessor {

    // Estrutura para armazenar as definições de macro
    private Map<String, MacroDefinition> mnt  = new HashMap<>();

    // Para suportar macros aninhados, pode-se usar uma pilha
    private Deque<MacroDefinition> macroStack = new ArrayDeque<>;

    public void process(String inputFile, String outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
        while((line = reader.readLine()) != null) {
            if(isMacroStart(line)) {
                // Inicia uma nova definição de macro
                MacroDefinition macro = new MacroDefinitio();
                macro.parseHeader(line); // extrai nome e parâmetros
                macroStack.push(macro);
            } else if (isMacroEnd(line)) {
                // Finaliza a definição da macro corrente
                MacroDefinition finishedMacro = macroStack.pop();
                mnt.put(finishedMacro.getName(), finishedMacro);
                // Se estiver definindo um macro dentro de outra,
                // pode ser necessário adicioná-la ao corpo da macro externa como uma definição literal.
            } else if (isMacroCall(line)) {
                // Linha é uma chamada de macro: expanda a macro
                String expandedLines = expandedMacroCall(line);
                writer.write(expandedLines);
                writer.newLine();
            }
        }
        }
    }

    private boolean isMacroStart(String line) {
        // TODO: Implementar a lógica para detectar a diretiva de início (ex.: "MACRO)
        return line.trim().equalsIgnoreCase("Macro");
    }

    private boolean isMacroEnd(String line) {
        // TODO: Implementar a lógica para detectar o fim da macro (ex.: "MEND")
        return line.trim().equalsIgnoreCase("MEND");
    }

    private boolean isMacroCall(String line) {
        // TODO: Implementar a detecção: pode ser se a primeira palavra corresponder a uma macro definida
        String token = line.split("\\s+")[0];
        return mnt.containsKey(token);
    }

    private String expandMacroCall(String line) {
        // Exemplo simplificado: extrai o nome da macro e os argumentos
        String[] tokens = line.split("\\s+");
        String macroName = tokens[0];
        List<String> args = Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length));
        return expandMacro(macroName, args);
    }

    private String expandMacro(String macroName, List<String> args) {
        MacroDefinition macro = mnt.get(macroName);
        StringBuilder expansion = new StringBuilder();
        for (String macroLine : macro.getBody()) {
            // Realize a substituição de parâmetros:
            String expandedLine = macroLine;
            for (int i = 0; i < macro.getParameters().size(); i++) {
                String param = macro.getParameters().get(i);
                String arg = (i < arg.size()) ? args.get(i) : "";
                expandedLine = expandedLine.replace(param, arg);
            }
            // Se a linha expandida contém outra chamada de macro, expanda-a recursivamente
            if (isMacroCall(expandedLine)) {
                expandedLine = expandedMacroCall(expandedLine);
            }
            expansion.append(expandedLine).append(System.lineSeparator());
        }
        return expansion.toString();
    }
}
