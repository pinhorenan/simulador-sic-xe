package app.src.main.java.sicxesimulator.simulation.systems;

import app.src.main.java.sicxesimulator.simulation.virtualMachine.Machine;
import app.src.main.java.sicxesimulator.simulation.virtualMachine.operations.Instruction;

import java.util.List;

public class Console {
    private final Machine virtualMachine;
    private final FileHandler fileHandler;
    private final Interpreter interpreter;
    private List<Instruction> instructions;
    private final Assembler assembler;

    // Registradores válidos para visualização/alteração
    private static final String[] VALID_OPTIONS = {"A", "X", "L", "PC", "B", "S", "T", "F", "SW"};

    public Console(Machine virtualMachine, FileHandler fileHandler, Interpreter interpreter, Assembler assembler) {
        this.virtualMachine = virtualMachine;
        this.fileHandler = fileHandler;
        this.interpreter = interpreter;
        this.assembler = assembler;
    }

    public static void cleanConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void treatCommand(String command) {
        String[] args = command.split(" ");
        switch (args[0]) {
            case "comandos":
                cleanConsole();
                System.out.println(
                        "\t------------------------Comandos------------------------\n" +
                            "montar [arquivo]               - Análise e verificação de sintaxe\n" +
                            "carregar_instrucoes [arquivo]  - Carrega instruções assembly\n" +
                            "exec                           - Executa todas as instruções\n" +
                            "prox                           - Executa a próxima instrução\n" +
                            "visualizar_mem [endereço]      - Visualiza memória a partir do endereço\n" +
                            "visualizar_reg [registrador]   - Visualiza o valor de um registrador\n" +
                            "alterar_mem [endereço] [valor] - Altera o conteúdo da memória\n" +
                            "alterar_reg [registrador] [valor]  - Altera o valor de um registrador\n" +
                            "salvar_arq [arquivo]           - Salva a memória em um arquivo\n" +
                            "carregar_arq [arquivo]         - Carrega a memória de um arquivo\n" +
                            "creditos                       - Exibe os créditos\n" +
                            "sair                           - Encerra o simulador\n" +
                        "\t-------------------------------------------------------\n"
                );
                break;
            case "creditos":
                cleanConsole();
                System.out.println(
                        "\t-----------------------Créditos-----------------------\n" +
                                "\t-----------------------------------------------------\n"
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
                    System.out.println("Uso: visualizar_mem [endereço]");
                    return;
                }
                int address = Integer.parseInt(args[1], 16);
                if (address < 0 || address >= virtualMachine.getMemory().getSize()) {
                    System.out.println("Endereço inválido.");
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
                    System.out.println("Registrador inválido.");
                    return;
                }
                System.out.println(regName + " = " + virtualMachine.getRegister(regName).getValue());
                break;
            case "alterar_mem":
                if (args.length != 3) {
                    System.out.println("Uso: alterar_mem [endereço] [valor]");
                    return;
                }
                int addr = Integer.parseInt(args[1], 16);
                String newValue = args[2];
                virtualMachine.getMemory().write(addr, newValue);
                System.out.println("Memória alterada com sucesso.");
                break;
            case "alterar_reg":
                if (args.length != 3) {
                    System.out.println("Uso: alterar_reg [registrador] [valor]");
                    return;
                }
                String regToChange = args[1].toUpperCase();
                String regVal = args[2];
                if (!contains(regToChange)) {
                    System.out.println("Registrador inválido.");
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
                    System.out.println("Nenhuma instrução carregada. Use 'montar'.");
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
                System.out.println("Execução concluída.");
                break;
            case "sair":
                cleanConsole();
                System.out.println("Encerrando simulador...");
                System.exit(0);
                break;
            default:
                System.out.println("Comando inválido.");
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
}