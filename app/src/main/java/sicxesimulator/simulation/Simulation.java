package app.src.main.java.sicxesimulator.simulation;

import app.src.main.java.sicxesimulator.simulation.virtualMachine.Machine;
import app.src.main.java.sicxesimulator.simulation.systems.Console;
import app.src.main.java.sicxesimulator.simulation.systems.FileHandler;
import app.src.main.java.sicxesimulator.simulation.systems.Interpreter;
import app.src.main.java.sicxesimulator.simulation.systems.Assembler;
import java.util.Scanner;
import static app.src.main.java.sicxesimulator.simulation.systems.Console.cleanConsole;

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