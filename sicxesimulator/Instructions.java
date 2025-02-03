package sicxesimulator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("unused")

public class Instructions {
    private String label;
    private String name;
    private String[] args;
    private String address; // Serão determinados pelo interpretador
    private int lineNumber;


    public Instructions(String label, String name, String[] args, String address, int lineNumber) {
        this.label = label;
        this.name = name;
        this.args = args;
        this.address = address;
        this.lineNumber = lineNumber;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setName(String nome) {
        this.name = nome;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public static List<Instructions> readFile(String file) {
        List<Instructions> instructionsList = new ArrayList<>();
        int lineNumber = 0;

        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                lineNumber++;

                if (line.isEmpty() || line.startsWith(".")) {
                    continue;
                }

                String[] inputList;
                List<String> argsArray = new ArrayList<>();

                if (line.contains("X'") || line.contains("C'")) {
                    inputList = line.split(",|\\s+");
                    if (line.contains("C'")) {
                        String substring = line.split("'")[1];
                        argsArray.add(inputList[2]);
                        argsArray.add(substring);
                    } else {
                        for (int i = 2; i < inputList.length; i++) {
                            argsArray.add(inputList[i]);
                        }
                    }
                } else {
                    inputList = line.split(",|\\s+");
                    for (int i = 2; i < inputList.length; i++) {
                        argsArray.add(inputList[i]);
                    }
                }

                Instructions instruction = new Instructions(
                    inputList[0], // label
                    inputList[1], // nome
                    argsArray.toArray(new String[0]), // args
                    null, // endereço (determinado dps)
                    lineNumber
                );
                instructionsList.add(instruction);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file);
            return null;
        }

        return instructionsList;
    }
} 


