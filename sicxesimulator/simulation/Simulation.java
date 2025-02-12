package sicxesimulator.simulation;

import sicxesimulator.simulation.virtualMachine.Machine;
import sicxesimulator.simulation.systems.Console;
import sicxesimulator.simulation.systems.FileHandler;
import sicxesimulator.simulation.systems.Interpreter;
import sicxesimulator.simulation.systems.Assembler;
import java.util.Scanner;
import static sicxesimulator.simulation.systems.Console.cleanConsole;

public abstract class Simulation {
    public static void run(){
        Machine virtualMachine = new Machine();
        FileHandler fileHandler = new FileHandler();
        Interpreter interpreter = new Interpreter(virtualMachine);
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