package sicxesimulator;

import java.util.List;

/**
 * Classe interna que representa o console do simulador.
 * Responsável por tratar os comandos do usuário e interagir com os componentes do simulador.
 */
public class Console {
    private List<Instruction> instructions = null;
    private Memory memory = null;
    private Register register = null;
    private Interpreter interpreter = null;

    /**
     * Tamanho em caracteres (bytes) para os registros.
     */
    private static final int BYTE_SIZE = 2; // Caracteres

    /**
     * Opções válidas para os registradores.
     */
    private static final String[] VALID_OPTIONS = {"A", "X", "L", "PC", "B", "S", "T", "F"};

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

        switch (args[0]) {
            case "comandos":
                cleanConsole();
                System.out.println("""
                            \t------------------------Comandos------------------------
                            Inicie com:
                            \tanalisar_arq\t\tInicia a análise e verificação de sintaxe
                            Comandos do Interpretador:
                            \texec\t\t\tExecuta todo o arquivo de montagem
                            \tiniciar\t\t\tInicia o interpretador
                            \tprox\t\t\tIncrementa o interpretador por uma instrução
                            \tparar\t\t\tPara o interpretador
                            
                            Comandos de Manipulação de Arquivos:
                            \tvisualizar_mem\t\tVisualiza a memória de um determinado endereço de memória
                            \tvisualizar_reg\t\tVisualiza o valor de um determinado registrador
                            \talterar_mem\t\tAltera uma seção da memória
                            \talterar_reg\t\tAltera um determinado registrador
                            \texportar_mem\t\tExporta a memória atual para um arquivo txt
                            
                            Outros Comandos:
                            \tcomandos\t\tLista de comandos disponíveis
                            \tcreditos\t\tCréditos da execução do trabalho
                            \tsair\t\t\tSai do simulador
                            \t-------------------------------------------------------
                            """);
                break;

            case "creditos":
                cleanConsole();
                System.out.println("""
                            \t-----------------------Creditos-----------------------
                            \tSimulador SIC/XE | Rock lee vs Gaara - Linkin park.amv
                            Arthur Alves (XXX)\t\tXXX.
                            Fabrício (XXX)\t\t\tXXX.
                            Gabriel Moura (Shikamaru)\tDefinição e controle dos registradores e memória.
                            Leonardo Braga (XXX)\t\tXXX.
                            Luis Eduardo Rasch (Neji)\tConstrução do console e analise dos arquivos.
                            Renan Pinho (Naruto)\t\tUM POUCO DE TUDO? TRADUTOR OFICIAL? MEMORIA? nao sei mexi em td.
                            \t-----------------------------------------------------
                            """);
                break;

            case "analisar_arq":
                System.out.println("\n");
                if (args.length != 2) {
                    System.out.println("Uso correto do comando: analisar_arq [arquivo]");
                    System.out.println("\n");
                    return;
                }

                this.instructions = Instruction.readFile("sicxesimulator/executaveis/"+args[1]);
                if (this.instructions == null) {
                    System.out.println("Falha na leitura do arquivo");
                    System.out.println("\n");
                    return;
                }
                else {
                    System.out.println("Arquivo lido com sucesso");
                }

                this.memory = new Memory();
                this.register = new Register();
                System.out.println("\n");
                break;

            case "visualizar_mem":
                System.out.println("\n");
                if (args.length != 2) {
                    System.out.println("Uso correto do comando: visualizar_mem [endereço]");
                    System.out.println("\n");
                    return;
                }

                if (this.memory == null) {
                    System.out.println("Use \"analisar_arq\" em um arquivo antes de visualizar a memória");
                    System.out.println("\n");
                    return;
                }

                int address = Integer.parseInt(args[1]);
                if (address >= 0 && address < this.memory.getMemory().size()) {
                    Word value = this.memory.getMemory().get(address);
                    System.out.println(value);
                } else {
                    System.out.println("Endereço inválido ou fora do alcance");
                }
                System.out.println("\n");
                break;

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
                if (contains(VALID_OPTIONS, regChoice)) {
                    System.out.println(this.register.getRegister(regChoice));
                }
                System.out.println("\n");
                break;

            case "iniciar":
                cleanConsole();
                this.interpreter = new Interpreter(this.instructions, this.memory, this.register);
                this.interpreter.setAddress();
                System.out.println("\n");
                break;

            case "prox":
                System.out.println("\n");
                this.interpreter.runNextInstruction();
                System.out.println("\n");
                break;

            case "exec":
                cleanConsole();
                while (true) {
                    String done = this.interpreter.runNextInstruction();
                    if (done != null) {
                        System.out.println("\n");
                        break;
                    }
                }
                break;

            case "exportar_mem":
                System.out.println("\n");
                if (this.memory == null) {
                    System.out.println("Use \"analisar_arq\" em um arquivo antes de exportar a memória");
                } else {
                    this.memory.printMemory();
                }
                System.out.println("\n");
                break;

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

                if (contains(VALID_OPTIONS, regChoiceChange)) {
                    System.out.println("Ajustando registrador de " + regChoiceChange + " para " + value);
                } else {
                    System.out.println("Defina um registrador válido");
                }
                System.out.println("\n");
                break;

            case "alterar_mem":
                System.out.println("\n");
                if (args.length != 3) {
                    System.out.println("Uso correto do comando: alterar_mem [endereço] [valor_byte]");
                    System.out.println("\n");
                    return;
                }

                int addressChange = Integer.parseInt(args[1]);
                // Aqui, Word é utilizado para representar uma palavra na memória.
                Word valueChange = new Word(args[2]);

                if (addressChange >= 0 && addressChange < this.memory.getMemory().size()) {
                    this.memory.setMemory(addressChange, valueChange);
                    System.out.println("Memória alterada com sucesso");
                } else {
                    System.out.println("Endereço inválido ou fora do alcance");
                }
                System.out.println("\n");
                break;

            case "parar":
                cleanConsole();
                System.out.println("Parando Interpretador");
                System.out.println("\n");
                break;

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
     * @param array O array de strings.
     * @param value O valor a ser verificado.
     * @return true se o valor estiver presente; false caso contrário.
     */
    private boolean contains(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }
}