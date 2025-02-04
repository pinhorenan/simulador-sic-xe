package sicxesimulator;

import java.util.Scanner;

import static sicxesimulator.Console.cleanConsole;

/**
  Classe principal do Simulador SIC/XE.
  Essa classe inicializa o simulador, processa os comandos do usuário e gerencia as operações do console.
 */
@SuppressWarnings("unused")
public class Simulator {

    /**
     * Inicia o simulador.
     *
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        cleanConsole();
        System.out.println("Simulador SIC/XE");
        System.out.println("Digite \"comandos\" para mais informações.\n\n");

        Console prompt = new Console();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                prompt.treatCommand(input);
            }
        }
    }
}
