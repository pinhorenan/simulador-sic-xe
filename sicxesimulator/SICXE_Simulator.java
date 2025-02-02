package sicxesimulator;

import java.util.Scanner;

public class SICXE_Simulator {

    private static final int TAM_BYTE = 2; // Characters
    private static final String[] OPÇOES_VALIDAS = {"A", "X", "L", "PC", "B", "S", "T", "F"};

    public static void main(String[] args) {
        limparConsole();
        System.out.println("Simulador SIC/XE");
        System.out.println("Digite \"comandos\" para mais informações.\n\n");

        Console prompt = new Console();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                prompt.commandHandler(input);
            }
        }
    }

    public static void limparConsole() {
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
    }

    static class Console {
        private String[] vetor_instruçoes = null;
        private Memory memoria = null;
        private Registery registrador = null;
        private Interpreter interpretador = null;

        public void commandHandler(String command) {
            String[] args = command.split(" ");

            switch (args[0]) {
                case "comandos":
                    limparConsole();
                    System.out.println("comandos\t\t\tLista de comandos disponíveis:\n" +
                            "creditos\t\t\tCréditos da execução do trabalho\n" +
                            "analisar_arq\t\t\tInicia a análise e verificação de sintaxe\n" +
                            "visualizar_mem\t\t\tExporta a memória atual para um arquivo de texto\n" +
                            "visualizar_reg\t\t\tVisualiza o valor de um determinado registrador\n" +
                            "alterar_mem\t\tAltera uma seção da memória\n" +
                            "alterar_reg\t\tAltera um determinado registrador\n" +
                            "iniciar\t\t\tInicia o interpretador\n" +
                            "prox\t\t\tIncrementa o interpretador por uma instrução\n" +
                            "parar\t\t\tPara o interpretador\n" +
                            "sair\t\t\tSai do simulador\n" +
                            "exec\t\t\tExecuta todo o arquivo de montagem\n" +
                            "exportar_mem\t\t\tExporta a memória atual para um arquivo txt");
                    break;
                case "creditos":
                    limparConsole();
                    System.out.println("\t------------------------Creditos------------------------");
                    System.out.println("\t\tSimulador SIC/XE | Rock lee vs Gaara - Linkin park.amv");
                    System.out.println("Arthur Alves (XXX)\t\t - XXX.");
                    System.out.println("Fabrício (XXX)\t\t\t XXX.");
                    System.out.println("Gabriel Moura (Shikamaru)\tDefinição e controle dos registradores e memória.");
                    System.out.println("Leonardo Braga(XXX)\t\t - XXX.");
                    System.out.println("Luis Eduardo Rasch(Neji)\tConstrução do console e analise dos arquivos.");
                    System.out.println("Renan Pinho (Naruto)\t\t - XXX.");
                    System.out.println("\t-------------------------------------------------------\n");
                    break;
                case "analisar_arq":
                    limparConsole();
                    if (args.length != 2) {
                        System.out.println("Uso do comando: analisar_arq [arquivo]");
                        return;
                    }

                    this.vetor_instruçoes = Parser.readFile(args[1]);
                    if (this.vetor_instruçoes == null) {
                        System.out.println("Falha na leitura do arquivo");
                        return;
                    }

                    this.memoria = new Memory();
                    this.registrador = new Registery();
                    break;
                case "visualizar_mem":
                    limparConsole();
                    if (args.length != 2) {
                        System.out.println("Uso do comando: visualizar_mem [endereço]");
                        return;
                    }

                    if (this.memoria == null) {
                        System.out.println("Use analisar_arq em um arquivo antes de visualizar a memória");
                        return;
                    }

                    String address = args[1];
                    if (address.length() == TAM_BYTE * 2) {
                        String memoryValue = this.memoria.getMemory(address);
                        if (memoryValue == null) {
                            System.out.println("Endereço inválido ou fora do alcance");
                        } else {
                            System.out.println(memoryValue);
                        }
                    } else {
                        System.out.println("Defina um endereço válido");
                    }
                    break;
                case "visualizar_reg":
                    limparConsole();
                    if (args.length != 2) {
                        System.out.println("Uso do comando: visualizar_reg [registrador]");
                        return;
                    }

                    if (this.registrador == null) {
                        System.out.println("Use analisar_arq em um arquivo antes de visualizar o registrador");
                        return;
                    }

                    String regChoice = args[1];
                    if (contains(OPÇOES_VALIDAS, regChoice)) {
                        System.out.println(this.registrador.getRegister(regChoice));
                    }
                    break;
                case "iniciar":
                    limparConsole();
                    this.interpretador = new Interpreter(this.vetor_instruçoes, this.memoria, this.registrador);
                    this.interpretador.assignAddress();
                    break;
                case "prox":
                    limparConsole();
                    this.interpretador.executeNextInstruction();
                    break;
                case "exec":
                    limparConsole();
                    while (true) {
                        String done = this.interpretador.executeNextInstruction();
                        if (done != null) {
                            break;
                        }
                    }
                    break;
                case "exportar_mem":
                    limparConsole();
                    if (this.memoria == null) {
                        System.out.println("Use analisar_arq em um arquivo antes de exportar a memória");
                    } else {
                        this.memoria.showMem();
                    }
                    break;
                case "alterar_reg":
                    limparConsole();
                    if (args.length != 3) {
                        System.out.println("Uso do comando: alterar_reg [registrador] [valor]");
                        return;
                    }

                    String regChoiceChange = args[1];
                    String value = args[2];

                    if (value.length() % 2 != 0 || value.length() > 6) {
                        System.out.println("Defina um valor válido");
                        return;
                    }

                    if (contains(OPÇOES_VALIDAS, regChoiceChange)) {
                        System.out.println("Ajustando registrador de " + regChoiceChange + " para " + value);
                    } else {
                        System.out.println("Defina um registrador válido");
                    }
                    break;
                case "alterar_mem":
                    limparConsole();
                    if (args.length != 3) {
                        System.out.println("Uso do comando: alterar_mem [endereço] [valor_byte]");
                        return;
                    }

                    String addressChange = args[1];
                    String valueChange = args[2];

                    if (valueChange.length() != TAM_BYTE) {
                        System.out.println("Defina um valor válido");
                        return;
                    }

                    if (!this.memoria.setMemory(addressChange, valueChange)) {
                        System.out.println("Valor ou endereço inválido - Falha na alteração da memória");
                    }
                    break;
                case "parar":
                    limparConsole();
                    System.out.println("Parando Interpretador");
                    break;
                case "sair":
                    limparConsole();
                    
                    String[] exitMessages = {
                        "Trabalho duro é inútil para aqueles que não acreditam em si mesmos.\n - Naruto Uzumaki",
                        "Lar é onde tem alguém sempre pensando em você.\n - Naruto Uzumaki",
                        "Se você não gosta do seu destino, não o aceite. Em vez disso, tenha a coragem de mudá-lo do jeito que você quer que ele seja.\n - Naruto Uzumaki",
                        "Saber o que é certo e escolher ignorá-lo é um ato de covardia.\n - Kakashi Hatake",
                        "Não há vantagem alguma em viver a vida correndo.\n - Shikamaru Nara",
                        "Nunca desvie seus olhos, porque se uma abertura surge, mesmo nosso poder insignificante pode ser suficiente para determinar o destino do mundo.\n - Shikamaru Nara",
                        "A vida das pessoas não termina quando eles morrem. Termina quando elas perdem a fé.\n - Itachi Uchiha",
                        "Não julgue alguém pela sua aparência, mas pelo tamanho do seu coração e seus sonhos.\n - Itachi Uchiha",
                    };
                    int randomIndex = (int) (Math.random() * exitMessages.length);
                    System.out.println(exitMessages[randomIndex] + "\n\n");

                    System.exit(0);
                    break;
                default:
                    limparConsole();
                    System.out.println("Comando Inválido");
                    break;
            }
        }

        private boolean contains(String[] array, String value) {
            for (String s : array) {
                if (s.equals(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    // CLASSES PLACEHOLDER PRA TESTAR O CONSOLE
    static class Memory {

        public String getMemory(String address) {
            
            return null;
        }

        public boolean setMemory(String address, String value) {
            
            return false;
        }

        public void showMem() {
            
        }
    }

    static class Registery {
        public String getRegister(String register) {
            
            return null;
        }

        public boolean setRegister(String register, String value) {
            
            return false;
        }
    }

    static class Interpreter {
        private String[] vetor_instruçoes;
        private Memory memoria;
        private Registery registrador;

        public Interpreter(String[] vetor_instruçoes, Memory memoria, Registery registrador) {
            this.vetor_instruçoes = vetor_instruçoes;
            this.memoria = memoria;
            this.registrador = registrador;
        }

        public void assignAddress() {
            
        }

        public String executeNextInstruction() {
            
            return null;
        }
    }

    static class Parser {
        public static String[] readFile(String filename) {
            
            return null;
        }
    }
}