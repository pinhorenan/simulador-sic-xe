package sicxesimulator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Representa uma instrução do arquivo de montagem.
 * Cada instrução possui um rótulo (label), nome (mnemonic), argumentos, endereço (a ser determinado pelo interpretador)
 * e o número da linha onde foi encontrada no arquivo.
 */
@SuppressWarnings("unused")
public class Instructions {

    /**
     * Rótulo da instrução.
     */
    private String label;
    /**
     * Nome ou mnemônico da instrução.
     */
    private String name;
    /**
     * Array de argumentos da instrução.
     */
    private String[] args;
    /**
     * Endereço associado à instrução.
     */
    private String address;
    /**
     * Número da linha no arquivo onde a instrução foi encontrada.
     */
    private int lineNumber;

    /**
     * Construtor.
     *
     * @param label O rótulo da instrução.
     * @param name O nome ou mnemônico da instrução.
     * @param args Os argumentos da instrução.
     * @param address O endereço da instrução.
     * @param lineNumber O número da linha em que a instrução aparece no arquivo.
     */
    public Instructions(String label, String name, String[] args, String address, int lineNumber) {
        this.label = label;
        this.name = name;
        this.args = args;
        this.address = address;
        this.lineNumber = lineNumber;
    }

    /**
     * Define um novo rótulo para a instrução.
     *
     * @param label O novo rótulo.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Define um novo nome (mnemônico) para a instrução.
     *
     * @param name O novo nome da instrução.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Define os argumentos da instrução.
     *
     * @param args Os novos argumentos.
     */
    public void setArgs(String[] args) {
        this.args = args;
    }

    /**
     * Lê um arquivo de montagem e retorna uma lista de instruções.
     * O arquivo deve conter as instruções de montagem e o método processa cada linha,
     * ignorando linhas vazias ou que iniciam com ponto.
     *
     * @param file O caminho do arquivo a ser lido.
     * @return Uma lista de objetos Instructions, ou null se o arquivo não for encontrado.
     */
    public static List<Instructions> readFile(String file) {
        List<Instructions> instructionsList = new ArrayList<>();
        int lineNumber = 0;

        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                lineNumber++;

                // Ignora linhas vazias ou que comecem com ".".
                if (line.isEmpty() || line.startsWith(".")) {
                    continue;
                }

                String[] inputList;
                List<String> argsArray = new ArrayList<>();

                // Verifica se a linha contém valores literais em formato hexadecimal ou de caracteres.
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
                    inputList[0],                       // label
                    inputList[1],                       // name
                    argsArray.toArray(new String[0]),   // args
                    null,                               // address
                    lineNumber                          // line number
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


