package sicxesimulator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

@SuppressWarnings("unused")

public class Instruçoes {
    private String label;
    private String nome;
    private String[] args;
    private String endereço; // Serão determinados pelo interpretador 
    private int numero_linha;

    public Instruçoes(String label, String nome, String[] args, String endereço, int numero_linha) {
        this.label = label;
        this.nome = nome;
        this.args = args;
        this.endereço = endereço;
        this.numero_linha = numero_linha;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setName(String nome) {
        this.nome = nome;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public static List<Instruçoes> ler_arquivo(String arquivo) {
        List<Instruçoes> lista_instruçoes = new ArrayList<>();
        int numero_linha = 0;

        try (Scanner scanner = new Scanner(new File(arquivo))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                numero_linha++;

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

                Instruçoes instruction = new Instruçoes(
                    inputList[0], // label
                    inputList[1], // nome
                    argsArray.toArray(new String[0]), // args
                    null, // endereço (determinado dps)
                    numero_linha
                );
                lista_instruçoes.add(instruction);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + arquivo);
            return null;
        }

        return lista_instruçoes;
    }
} 


