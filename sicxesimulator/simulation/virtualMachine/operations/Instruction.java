package sicxesimulator.simulation.virtualMachine.operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Representa uma instrução do arquivo de montagem.
 * Cada instrução possui um rótulo (label), nome (mnemonic), argumentos, endereço (a ser determinado pelo interpretador)
 * e o número da linha onde foi encontrada no arquivo.
 */
@SuppressWarnings("unused")
public class Instruction {

    private String label;
    private String name;
    private String[] args;
    private String address;
    private int lineNumber;


    public Instruction(String label, String name, String[] args, String address, int lineNumber) {
        this.label = label;
        this.name = name;
        this.args = args;
        this.address = address;
        this.lineNumber = lineNumber;
    }

    /// Getters

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String[] getArgs() {
        return args;
    }

    public String getAddress() {
        return address;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    /// Setters

    public void setLabel(String label) {
        this.label = label;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return String.format("(%d) %s %s %s", lineNumber, name, address, String.join(", ", args));
    }


    public static List<Instruction> readFile(String file) {
        List<Instruction> instructionsList = new ArrayList<>();
        int lineNumber = 0;

        // Exemplo de mnemônicos válidos (pode ser expandido conforme a necessidade)
        List<String> validMnemonics = Arrays.asList("ADD", "ADDR", "AND", "CLEAR", "LDX", "COMP",
        "COMPR", "DIV", "DIVR", "J", "JEQ", "JGT", "JLT", "JSUB", "LDA", "LDB", "LDCH", "LDL",
        "LDS", "LDT", "MUL", "MULR", "OR", "RMO", "RSUB", "SHIFTL", "SHIFTR", "STA", "STB", "STCH",
        "STL", "STS", "STT", "STX", "SUB", "SUBR", "TIX", "TIXR");
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            lineNumber++;

            // Ignora linhas vazias ou que comecem com "."
            if (line.isEmpty() || line.startsWith(".")) {
                continue;
            }

            // Divide a linha por espaços ou vírgulas (removendo tokens vazios)
            String[] tokens = line.split("[\\s,]+");

            String label = "";
            String mnemonic;
            List<String> argsList = new ArrayList<>();

            // Se o primeiro token estiver em validMnemonics, significa que não há label.
            if (validMnemonics.contains(tokens[0].toUpperCase())) {
                mnemonic = tokens[0];
                // Os argumentos começam no token 1
                argsList.addAll(Arrays.asList(tokens).subList(1, tokens.length));
            } else {
                // Caso contrário, o primeiro token é o label e o segundo é o mnemônico
                label = tokens[0];
                if (tokens.length < 2) {
                System.out.println("Erro de sintaxe na linha " + lineNumber);
                continue;
                }
                mnemonic = tokens[1];
                argsList.addAll(Arrays.asList(tokens).subList(2, tokens.length));
            }

                // Aqui você pode incluir lógica adicional para tratar literais como X'... ' Ou C'... se necessário.

                Instruction instruction = new Instruction(
                        label,
                        mnemonic,
                        argsList.toArray(new String[0]),
                        null,  // endereço será determinado posteriormente
                        lineNumber
                );
                instructionsList.add(instruction);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado: " + file);
            return null;
        }

        return instructionsList;
    }

} 


