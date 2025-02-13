package sicxesimulator.simulation.systems;

import sicxesimulator.simulation.virtualMachine.Machine;
import sicxesimulator.simulation.virtualMachine.operations.Instruction;

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
            case "comandos":
                cleanConsole();
                System.out.println(
                        "-----------------------------------------------  Comandos  -----------------------------------------------\n" +
                        "analisar_arq [arquivo]\t\t\t\t- Analise e verificacao de sintaxe\n" +
                        "carregar_instrucoes [arquivo]\t\t\t- Carrega instrucoes assembly\n" +
                        "exec\t\t\t\t\t\t\t\t- Executa todas as instrucoes\n" +
                        "prox\t\t\t\t\t\t\t\t- Executa a proxima instrucao\n" +
                        "visualizar_mem [endereco]\t\t\t- Visualiza memoria a partir do endereco\n" +
                        "visualizar_reg [registrador]\t\t\t- Visualiza o valor de um registrador\n" +
                        "alterar_mem [endereco] [valor]\t\t- Altera o conteúdo da memoria\n" +
                        "alterar_reg [registrador] [valor]\t\t- Altera o valor de um registrador\n" +
                        "salvar_arq [arquivo]\t\t\t\t\t- Salva a memoria em um arquivo\n" +
                        "carregar_arq [arquivo]\t\t\t\t- Carrega a memoria de um arquivo\n" +
                        "creditos\t\t\t\t\t\t\t- Exibe os creditos\n" +
                        "sair\t\t\t\t\t\t\t\t- Encerra o simulador\n" +
                        "-----------------------------------------------------------------------------------------------------------\n"
                );
                break;
            case "creditos":
                cleanConsole();
                System.out.println(
                        "------------------------------------------------  Creditos  ------------------------------------------------\n" +
                        "Arthur Alves - Organizacao, discussao, ajustes.\n" +
                        "Fabricio Bartz - Organizacao, discussao, ajustes.\n" +
                        "Gabriel Moura (Shikamaru) - Construcao, definicao e ajustes dos registradores e memoria.\n" +
                        "Leonardo Braga - Ajustes nas flags de operacoes.\n" +
                        "Luis Eduardo Rasch (Neji) - Construcao e ajuste do console, leitura e analise dos arquivos, e testes.\n" +  
                        "Renan Pinho (Naruto) - Construcao das instrucoes e simulador, ajustes em todo o codigo e transpiler.\n" +
                        "-------------------------------------------------------------------------------------------------------------\n"
                );
                break;
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
                System.out.println("Montagem concluída com sucesso.");
                break;
            case "visualizar_mem":
                if (args.length != 2) {
                    System.out.println("Uso: visualizar_mem [endereco]");
                    return;
                }
                int address = Integer.parseInt(args[1], 16);
                if (address < 0 || address >= virtualMachine.getMemory().getSize()) {
                    System.out.println("Endereco invalido.");
                    return;
                }
                System.out.println("Mem[" + String.format("%04X", address) + "]: " + virtualMachine.getMemory().read(address));
                break;
            case "visualizar_reg":
                if (args.length != 2) {
                    System.out.println("Uso: visualizar_reg [registrador]");
                    return;
                }
                String regName = args[1].toUpperCase();
                if (!contains(regName)) {
                    System.out.println("Registrador invalido.");
                    return;
                }
                System.out.println(regName + " = " + virtualMachine.getRegister(regName).getValue());
                break;
            case "alterar_mem":
                if (args.length != 3) {
                    System.out.println("Uso: alterar_mem [endereco] [valor]");
                    return;
                }
                int addr = Integer.parseInt(args[1], 16);
                String newValue = args[2];
                virtualMachine.getMemory().write(addr, newValue);
                System.out.println("Memoria alterada com sucesso.");
                break;
            case "alterar_reg":
                if (args.length != 3) {
                    System.out.println("Uso: alterar_reg [registrador] [valor]");
                    return;
                }
                String regToChange = args[1].toUpperCase();
                String regVal = args[2];
                if (!contains(regToChange)) {
                    System.out.println("Registrador invalido.");
                    return;
                }
                virtualMachine.getRegister(regToChange).setValue(regVal);
                System.out.println("Registrador " + regToChange + " alterado para " + regVal);
                break;
            case "salvar_arq":
                if (args.length != 2) {
                    System.out.println("Uso: salvar_arq [arquivo]");
                    return;
                }
                fileHandler.saveMemoryToFile(virtualMachine.getMemory(), args[1]);
                break;
            case "carregar_arq":
                if (args.length != 2) {
                    System.out.println("Uso: carregar_arq [arquivo]");
                    return;
                }
                fileHandler.loadMemoryFromFile(virtualMachine.getMemory(), args[1]);
                break;
            case "iniciar":
                if (instructions == null) {
                    System.out.println("Nenhuma instrucao carregada. Use 'montar'.");
                    return;
                }
                interpreter.setStartAddress(0);
                System.out.println("Interpretador iniciado.");
                break;
            case "prox":
                interpreter.runNextInstruction();
                break;
            case "exec":
                while (!interpreter.isFinished()) {
                    interpreter.runNextInstruction();
                }
                System.out.println("Execucao concluída.");
                break;
            case "sair":
                cleanConsole();
                outputStream.print("Encerrando simulador"); // Sem quebra de linha
                outputStream.flush();
            
                // Criar uma transição de pausa para os pontos
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
            
                // Criar a transição final para sair do sistema
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
}