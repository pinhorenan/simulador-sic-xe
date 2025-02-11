package sicxesimulator.simulation;

import sicxesimulator.simulation.virtualMachine.*;
import sicxesimulator.simulation.systems.*;

import java.util.Scanner;

import static sicxesimulator.simulation.systems.Console.cleanConsole;


public class Simulation {

    public Simulation() {
        Machine virtualMachine = new Machine();
        FileHandler fileHandler = new FileHandler();
        Interpreter interpreter = new Interpreter();
        Assembler assembler = new Assembler();
        Console console = new Console(virtualMachine, fileHandler, interpreter, assembler);

        cleanConsole();
        System.out.println("Simulador SIC/XE");
        System.out.println("Digite \"comandos\" para mais informações.\n\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                console.treatCommand(input);
            }
        }
    }
}
