package sicxesimulator;

import java.util.List;
import java.util.Scanner;

@SuppressWarnings("unused")

public class Simulador_SICXE {

    private static final int TAM_BYTE = 2; // Caracteres
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
                prompt.tratarComando(input);
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
        private Memoria memoria = null;
        private Registrador registrador = null;
        private Interpretador interpretador = null;

        public void tratarComando(String comando) {
            String[] args = comando.split(" ");

            switch (args[0]) {
                case "comandos":
                    limparConsole();
                    System.out.println("\t------------------------Comandos------------------------\n" +
                            "Inicie com:\n" +
                            "\tanalisar_arq\t\tInicia a análise e verificação de sintaxe\n" +
                    
                            "Comandos do Interpretador:\n" +                
                            "\texec\t\t\tExecuta todo o arquivo de montagem\n" +
                            "\tiniciar\t\t\tInicia o interpretador\n" +
                            "\tprox\t\t\tIncrementa o interpretador por uma instrução\n" +
                            "\tparar\t\t\tPara o interpretador\n" +
                            
                            "\nComandos de Manipulação de Arquivos:\n" +
                            "\tvisualizar_mem\t\tVisualiza a memória de um determinado endereço de memória\n" +
                            "\tvisualizar_reg\t\tVisualiza o valor de um determinado registrador\n" +
                            "\talterar_mem\t\tAltera uma seção da memória\n" +
                            "\talterar_reg\t\tAltera um determinado registrador\n" +
                            "\texportar_mem\t\tExporta a memória atual para um arquivo txt\n" +

                            "\nOutros Comandos:\n" +
                            "\tcomandos\t\tLista de comandos disponíveis\n" +
                            "\tcreditos\t\tCréditos da execução do trabalho\n" +
                            "\tsair\t\t\tSai do simulador\n" +
                            "\t-------------------------------------------------------\n");
                    break;

                case "creditos":
                    limparConsole();
                    System.out.println("\t-----------------------Creditos-----------------------\n" +
                    "\tSimulador SIC/XE | Rock lee vs Gaara - Linkin park.amv\n" +
                    "Arthur Alves (XXX)\t\tXXX.\n" +
                    "Fabrício (XXX)\t\t\tXXX.\n" +
                    "Gabriel Moura (Shikamaru)\tDefinição e controle dos registradores e memória.\n" +
                    "Leonardo Braga (XXX)\t\tXXX.\n" +
                    "Luis Eduardo Rasch (Neji)\tConstrução do console e analise dos arquivos.\n" +
                    "Renan Pinho (Naruto)\t\tXXX.\n" +
                    "\t-----------------------------------------------------\n");
                    break;

                case "analisar_arq":
                    System.out.println("\n");
                    if (args.length != 2) {
                        System.out.println("Uso correto do comando: analisar_arq [arquivo]");
                        System.out.println("\n");
                        return;
                    }

                    List<Instruçoes> instrucoes = Instruçoes.ler_arquivo("sicxesimulator/executaveis/"+args[1]);
                    if (instrucoes == null) {
                        System.out.println("Falha na leitura do arquivo");
                        System.out.println("\n");
                        return;
                    }
                    else {
                        System.out.println("Arquivo lido com sucesso");
                    }

                    this.memoria = new Memoria();
                    this.registrador = new Registrador();
                    System.out.println("\n");
                    break;

                case "visualizar_mem":
                    System.out.println("\n");
                    if (args.length != 2) {
                        System.out.println("Uso correto do comando: visualizar_mem [endereço]");
                        System.out.println("\n");
                        return;
                    }

                    if (this.memoria == null) {
                        System.out.println("Use \"analisar_arq\" em um arquivo antes de visualizar a memória");
                        System.out.println("\n");
                        return;
                    }

                    int address = Integer.parseInt(args[1]);
                    if (address >= 0 && address < this.memoria.GetMemoria().size()) {
                        PalavraMemoria valor_memoria = this.memoria.GetMemoria().get(address);
                        System.out.println(valor_memoria);
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

                    if (this.registrador == null) {
                        System.out.println("Use \"analisar_arq\" em um arquivo antes de visualizar o registrador");
                        System.out.println("\n");
                        return;
                    }

                    String regChoice = args[1];
                    if (contains(OPÇOES_VALIDAS, regChoice)) {
                        System.out.println(this.registrador.getRegistrador(regChoice));
                    }
                    System.out.println("\n");
                    break;

                case "iniciar":
                    limparConsole();
                    this.interpretador = new Interpretador(this.vetor_instruçoes, this.memoria, this.registrador);
                    this.interpretador.atribuirEndereco();
                    System.out.println("\n");
                    break;

                case "prox":
                    System.out.println("\n");
                    this.interpretador.executarProximaInstrucao();
                    System.out.println("\n");
                    break;

                case "exec":
                    limparConsole();
                    while (true) {
                        String done = this.interpretador.executarProximaInstrucao();
                        if (done != null) {
                            System.out.println("\n");
                            break;
                        }
                    }
                    break;

                case "exportar_mem":
                    System.out.println("\n");
                    if (this.memoria == null) {
                        System.out.println("Use \"analisar_arq\" em um arquivo antes de exportar a memória");
                    } else {
                        this.memoria.ShowMemoria();
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

                    if (contains(OPÇOES_VALIDAS, regChoiceChange)) {
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
                    PalavraMemoria valueChange = new PalavraMemoria(args[2]);

                    if (addressChange >= 0 && addressChange < this.memoria.GetMemoria().size()) {
                        this.memoria.SetMemoria(addressChange, valueChange);
                        System.out.println("Memória alterada com sucesso");
                    } else {
                        System.out.println("Endereço inválido ou fora do alcance");
                    }
                    System.out.println("\n");
                    break;

                case "parar":
                    limparConsole();
                    System.out.println("Parando Interpretador");
                    System.out.println("\n");
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
                    System.out.println(exitMessages[randomIndex] + "\n");

                    System.exit(0);
                    break;
                    
                default:
                    limparConsole();
                    System.out.println("Comando Inválido");
                    System.out.println("\n");
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

    static class Interpretador {
        private String[] vetorInstrucoes;
        private Memoria memoria;
        private Registrador registrador;

        public Interpretador(String[] vetorInstrucoes, Memoria memoria, Registrador registrador) {
            this.vetorInstrucoes = vetorInstrucoes;
            this.memoria = memoria;
            this.registrador = registrador;
        }

        public void atribuirEndereco() {
        }

        public String executarProximaInstrucao() {
            return null;
        }
    }
}
