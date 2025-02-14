package sicxesimulator.systems;

import sicxesimulator.components.Machine;
import sicxesimulator.components.operations.Instruction;
import sicxesimulator.utils.FileHandler;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Console {
    private final Machine virtualMachine;
    private final FileHandler fileHandler;
    private final Interpreter interpreter;
    private List<Instruction> instructions;
    private final Assembler assembler;
    private PrintStream outputStream;

    // Registradores validos para visualizacao/alteracao
    private static final String[] VALID_OPTIONS = {"A", "X", "L", "PC", "B", "S", "T", "F", "SW"};

    public Console(Machine virtualMachine, FileHandler fileHandler, Interpreter interpreter, Assembler assembler) {
        this.virtualMachine = virtualMachine;
        this.fileHandler = fileHandler;
        this.interpreter = interpreter;
        this.assembler = assembler;
    }

    public static void cleanConsole() {
        System.out.flush();
    }

    public void treatCommand(String command) {
        String[] args = command.split(" ");
        switch (args[0]) {
            case "montar":
                if (args.length != 2) {
                    System.out.println("Uso: montar [arquivo.asm]");
                    return;
                }
                List<String> sourceLines = fileHandler.readFileLines(args[1]);
                if (sourceLines == null) {
                    System.out.println("Erro ao ler o arquivo.");
                    return;
                }
                List<Instruction> assembledInstructions = assembler.assemble(sourceLines);
                if (assembledInstructions.isEmpty()) {
                    System.out.println("Falha ao montar o programa.");
                }
                instructions = assembledInstructions;
                System.out.println("Montagem concluida com sucesso.");
                break;
            case "prox":
                interpreter.runNextInstruction();
                break;
            case "exec":
                if (instructions == null) {
                    System.out.println("Nenhuma instrucao carregada. Use 'montar'.");
                    return;
                }
                while (!interpreter.isFinished()) {
                    interpreter.runNextInstruction();
                }
                System.out.println("Execucao concluida.");
                break;
            case "limpar":
                cleanConsole();
                outputStream.print("Encerrando simulador"); // Sem quebra de linha
                outputStream.flush();

                // Criar uma transicao de pausa para os pontos
                PauseTransition pause1 = new PauseTransition(Duration.seconds(1));
                pause1.setOnFinished(event -> {
                    outputStream.print(".");
                    outputStream.flush();
                });

                PauseTransition pause2 = new PauseTransition(Duration.seconds(2));
                pause2.setOnFinished(event -> {
                    outputStream.print(".");
                    outputStream.flush();
                });

                PauseTransition pause3 = new PauseTransition(Duration.seconds(3));
                pause3.setOnFinished(event -> {
                    outputStream.print(".");
                    outputStream.flush();
                });

                // Criar a transicao final para sair do sistema
                PauseTransition exitPause = new PauseTransition(Duration.seconds(4));
                exitPause.setOnFinished(event -> System.exit(0));

                // Executar as pausas em sequência
                pause1.play();
                pause2.play();
                pause3.play();
                exitPause.play();

                break;
            default:
                System.out.println("Comando invalido.");
                break;
        }
    }

    private boolean contains(String value) {
        for (String s : VALID_OPTIONS) {
            if (s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public void setOutput(PrintStream output) {
        this.outputStream = output;
        System.setOut(output);
    }

    public Machine getMachine() {
        return virtualMachine;
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    public void reset() {
        // Limpar registradores
        for (String register : VALID_OPTIONS) {
            virtualMachine.getRegister(register).setValue("0");  // Limpa os registradores
        }

        // Limpar memória
        virtualMachine.getMemory().clear();  // Supondo que a memória tenha um método clear() que limpa todos os valores

        // Limpar instruções montadas
        instructions = new ArrayList<>();  // Limpa a lista de instruções montadas

        // Reinicializar o arquivo lido
        fileHandler.clear();  // Supondo que o FileHandler tenha um método clear() para limpar o conteúdo lido

        System.out.println("Sistema resetado.");
    }
}