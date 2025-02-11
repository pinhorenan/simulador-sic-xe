package sicxesimulator.simulation.systems;

import sicxesimulator.simulation.virtualMachine.operations.Instruction;
import sicxesimulator.simulation.virtualMachine.*;

import java.util.List;

/**
 * Classe interna que representa o console do simulador.
 * Responsável por tratar os comandos do usuário e interagir com os componentes do simulador.
 */
public class Console {
    private final Machine virtualMachine;
    private final FileHandler fileHandler;
    private final Interpreter interpreter;
    private final Assembler assembler;

    /**
     * Opções válidas para os registradores.
     */
    private static final String[] VALID_OPTIONS = {"A", "X", "L", "PC", "B", "S", "T", "F"};


    public Console(Machine virtualMachine, FileHandler fileHandler, Interpreter interpreter, Assembler assembler) {
        this.virtualMachine = virtualMachine;
        this.fileHandler = fileHandler;
        this.interpreter = interpreter;
        this.assembler = assembler;
    }

    /**
     * Limpa o console imprimindo várias linhas em branco.
     */
    public static void cleanConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Trata o comando digitado pelo usuário.
     *
     * @param command O comando a ser digitado.
     */
    
     public void treatCommand(String command) {
        String[] args = command.split(" ");
        Memory memory = virtualMachine.getMemory();

        switch (args[0]) {
                // Aqui imprime os comandos.
            case "comandos":
                cleanConsole();
                System.out.println(
                            "\t------------------------Comandos------------------------\n" +
                            "Inicie com:\n" +
                            "\tanalisar_arq\t\tInicia a análise e verificação de sintaxe\n" +

                            "Comandos do Interpretador:\n" +
                            "\texec\t\t\tExecuta todo o arquivo de montagem\n" +
                            "\tiniciar\t\t\tInicia o interpretador\n" +
                            "\tprox\t\t\tIncrementa o interpretador por uma instrução\n" +
                            "\tparar\t\t\tPara o interpretador\n" +
                            
                            "Comandos de Memória:\n" +
                            "\tcarregar_arq\t\tCarrega um arquivo para a memória\n" +
                            "\tsalvar_arq\t\tSalva a memória atual em um arquivo\n" +
                            "\tvisualizar_mem\t\tVisualiza a memória (em HEX) de um determinado endereço de memória\n" +
                            "\tvisualizar_reg\t\tVisualiza o valor de um determinado registrador\n" +
                            "\talterar_mem\t\tAltera uma seção da memória\n" +
                            "\talterar_reg\t\tAltera um determinado registrador\n" +
                            
                            "Outros Comandos:\n" +
                            "\tcomandos\t\tLista de comandos disponíveis\n" +
                            "\tcreditos\t\tCréditos da execução do trabalho\n" +
                            "\tsair\t\t\tSai do simulador\n" +
                            "\t-------------------------------------------------------\n"
                            );
                break;

                // Aqui imprime os créditos.
            case "creditos":
                cleanConsole();
                System.out.println(
                            "\t-----------------------Creditos-----------------------\n" +
                            "\tSimulador SIC/XE | Rock lee vs Gaara - Linkin park.amv\n" +
                            "Arthur Alves (XXX)\t\tXXX.\n" +
                            "Fabrício Bartz (XXX)\t\t\tXXX.\n" +
                            "Gabriel Moura (Shikamaru)\tConstrução, definição e ajustes dos registradores e memória.\n" +
                            "Leonardo Braga (XXX)\t\tAjustes nas flags de operações.\n" +
                            "Luis Eduardo Rasch (Neji)\tConstrução e ajuste do console, leitura e analise dos arquivos, e testes.\n" +
                            "Renan Pinho (Naruto)\t\tConstrução das instruções e simulador, ajustes em todo o código e transpiler.\n" +
                            "\t-----------------------------------------------------\n"
                            );
                break;

               // Aqui utilizamos fileHandler.loadInstructionsFromFile().
            case "carregar_instrucoes":
                System.out.println("\n");
                if (args.length != 2) {
                    System.out.println("Uso correto do comando: carregar_instrucoes [arquivo]");
                    System.out.println("\n");
                    return;
                }

                List<Instruction> instructionList = fileHandler.loadInstructionsFromFile(args[1]);

                if (instructionList == null) {
                    System.out.println("Falha na leitura do arquivo");
                    System.out.println("\n");
                    return;
                }
                else {
                    Interpreter.setInstructions(instructionList);
                    System.out.println("Instruções carregadas com sucesso.");
                }

                break;

                // Aqui utilizamos a memory.read().
            case "visualizar_mem":
                System.out.println("\n");
                if (args.length != 2) {
                    System.out.println("Uso correto do comando: visualizar_mem [endereço]");
                    System.out.println("\n");
                    return;
                }

                if (memory == null) {
                    System.out.println("Use \"analisar_arq\" em um arquivo antes de visualizar a memória");
                    System.out.println("\n");
                    return;
                }

                int address = Integer.parseInt(args[1]);
                if (address >= 0 && address < memory.getSize()) {
                    System.out.println(memory.read(address));
                } else {
                    System.out.println("Endereço inválido ou fora do alcance");
                }
                System.out.println("\n");
                break;

                // Aqui utilizamos register.read(). TODO
            case "visualizar_reg":
                System.out.println("\n");
                if (args.length != 2) {
                    System.out.println("Uso correto do comando: visualizar_reg [registrador]");
                    System.out.println("\n");
                    return;
                }

                if (this.register == null) {
                    System.out.println("Use \"analisar_arq\" em um arquivo antes de visualizar o registrador");
                    System.out.println("\n");
                    return;
                }

                String regChoice = args[1];
                if (contains(regChoice)) {
                    System.out.println(this.register.getRegister(regChoice));
                }
                System.out.println("\n");
                break;

                // Aqui utilizamos Interpreter.run(). TODO
            case "iniciar":
                cleanConsole();
                this.interpreter = new Interpreter(this.instructions, this.memory, this.register);
                this.interpreter.setStartAddress(0);
                System.out.println("\n");
                break;

                // Aqui utilizamos Interpreter.runNextInstruction(). TODO
            case "prox":
                if (this.interpreter == null) {
                    this.interpreter = new Interpreter(this.instructions, this.memory, this.register);
                    this.interpreter.setStartAddress(0);
                }
                System.out.println("\n");
                this.interpreter.runNextInstruction();
                System.out.println("\n");
                break;

                // Aqui utilizamos um loop para rodar todas instruções do início ao fim. TODO
            case "exec":
                if (this.interpreter == null) {
                    this.interpreter = new Interpreter(this.instructions, this.memory, this.register);
                    this.interpreter.setStartAddress(0);
                }
                cleanConsole();
                while (true) {
                    String done = this.interpreter.runNextInstruction();
                    if (done != null) {
                        System.out.println("\n");
                        break;
                    }
                
                    /**
                     * Verifica se uma string é numérica.
                     *
                     * @param str A string a ser verificada.
                     * @return true se a string for numérica; false caso contrário.
                     */
                }
                break;

                // Aqui utilizamos FileHandler.saveMemoryToFile().
            case "salvar_arq":
                System.out.println("\n");
                if (memory == null) {
                    System.out.println("Use \"analisar_arq\" em um arquivo antes de exportar a memória");
                } else {
                    fileHandler.saveMemoryToFile(memory, args[1]);
                }
                System.out.println("\n");
                break;

                // Aqui utilizamos FileHandler.loadMemoryFromFile().
            case "carregar_arq":
                System.out.println("\n");
                if (args.length != 2) {
                    System.out.println("Uso correto do comando: carregar_arq [arquivo]");
                    System.out.println("\n");
                    return;
                }

                fileHandler.loadMemoryFromFile(memory, args[1]);
                System.out.println("\n");
                break;

                // Aqui utilizamos Register.setValue(). TODO
            case "alterar_reg":
                System.out.println("\n");
                if (args.length != 3) {
                    System.out.println("Uso correto do comando: alterar_reg [registrador] [valor]");
                    System.out.println("\n");
                    return;
                }

                String regChoiceChange = args[1];
                String value = args[2];

                if (value.length() % 2 != 0 || value.length() > 6) {
                    System.out.println("Defina um valor válido");
                    System.out.println("\n");
                    return;
                }

                if (contains(regChoiceChange)) {
                    System.out.println("Ajustando registrador de " + regChoiceChange + " para " + value);
                } else {
                    System.out.println("Defina um registrador válido");
                }
                System.out.println("\n");
                break;

                // Aqui utilizamos Memory.write().
            case "alterar_mem":
                System.out.println("\n");
                if (args.length != 3) {
                    System.out.println("Uso correto do comando: alterar_mem [endereço] [valor_byte]");
                    System.out.println("\n");
                    return;
                }

                int addressToChange = Integer.parseInt(args[1]);
                String newValue = args[2];

                if (addressToChange >= 0 && addressToChange < memory.getSize()) {
                    memory.write(addressToChange, newValue);
                    System.out.println("Memória alterada com sucesso");
                } else {
                    System.out.println("Endereço inválido ou fora do alcance");
                }
                System.out.println("\n");
                break;

                // Aqui utilizamos Interpreter.stop(). TODO
            case "parar":
                cleanConsole();
                System.out.println("Parando Interpretador");
                System.out.println("\n");
                break;

                // Aqui chamamos System.exit().
            case "sair":
                cleanConsole();

                String[] exitMessages = {
                        "Trabalho duro é inútil para aqueles que não acreditam em si mesmos.\n - Naruto Uzumaki",
                        "Sou eu, Zabuza Momochi, o Demônio do Gás Oculto.\n - Zabuza Momochi, AKA 'O Demônio do Gás Oculto'",
                        "Não pode ser, é ele, Zabuza Momochi, O Demônio do Gás Oculto. - Kakashi Hatake",
                        "Zabuza Momochi, O Demônio do Gás Oculto?\n - Naruto Uzumaki",
                        "Lar é onde tem alguém sempre pensando em você.\n - Naruto Uzumaki",
                        "O Naruto pode ser duro as vezes.\n - Kakashi Hatake",
                        "Se você não gosta do seu destino, não o aceite. Em vez disso, tenha a coragem de mudá-lo do jeito que você quer que ele seja.\n - Naruto Uzumaki",
                        "Saber o que é certo e escolher ignorá-lo é um ato de covardia.\n - Kakashi Hatake",
                        "Não há vantagem alguma em viver a vida correndo.\n - Shikamaru Nara",
                        "Nunca desvie seus olhos, porque se uma abertura surge, mesmo nosso poder insignificante pode ser suficiente para determinar o destino do mundo.\n - Shikamaru Nara",
                        "A vida das pessoas não termina quando eles morrem. Termina quando elas perdem a fé.\n - Itachi Uchiha",
                        "Não julgue alguém pela sua aparência, mas pelo tamanho do seu coração e seus sonhos.\n - Itachi Uchiha",
                };
                int randomIndex = (int) (Math.random() * exitMessages.length);
                System.out.println(exitMessages[randomIndex] + "\n");

                System.exit(0);
                break;

            default:
                cleanConsole();
                System.out.println("Comando Inválido");
                System.out.println("\n");
                break;
        }
    }

    /**
     * Verifica se um determinado valor está contido em um array de strings.
     *
     * @param value O valor a ser verificado.
     * @return true se o valor estiver presente; false caso contrário.
     */
    private boolean contains(String value) {
        for (String s : Console.VALID_OPTIONS) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }
}