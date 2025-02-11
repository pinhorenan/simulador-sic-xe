package sicxesimulator.simulation.systems;

import sicxesimulator.simulation.virtualMachine.*;
import sicxesimulator.simulation.virtualMachine.operations.Instruction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    /**
     * Lê um arquivo de código de montagem e retorna uma lista de instruções.
     * @Param filePath - Caminho do arquivo a ser lido.
     * \@return Lista de instruções, ou null se ocorrer algum erro.
     */
    public List<Instruction> loadInstructionsFromFile(String filePath) {
        List<Instruction> instructions = new ArrayList<>();
        int lineNumber = 0;

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Ignora linhas vazias e comentários (que começam com ".")
                if (line.isEmpty() || line.startsWith(".")) {
                    continue;
                }

                // Divide a linha em partes (mnemônico, label, argumentos, etc.)
                String[] tokens = line.split("\\s+");
                String label = "";
                String name;
                List<String> args = new ArrayList<>();

                if (tokens.length == 1) {
                    // Exemplo: "RSUB"
                    name = tokens[0];
                } else if (tokens.length == 2) {
                    // Exemplo: "LDA NUM1"
                    name = tokens[0];
                    args.add(tokens[1]);
                } else if (tokens.length > 2) {
                    // Exemplo: "NUM1 WORD 4"  ou  "LDA NUM1, X"
                    label = tokens[0];
                    name = tokens[1];

                    for (int i = 2; i < tokens.length; i++) {
                        args.add(tokens[i]);
                    }

                    // TODO ADICIONAR O RESTO.
                } else {
                    continue; // Linha inválida
                }

                // Cria e adiciona a instrução à lista
                instructions.add(new Instruction(label, name, args.toArray(new String[0]), null, lineNumber));
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            return null;
        }

        return instructions;
    }

    /**
     * Salva o estado atual da memória em um arquivo.
     * @param memory - Memória a ser salva.
     * @param filePath - Caminho do arquivo destino.
     */
    public void saveMemoryToFile(Memory memory, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < memory.getSize(); i++) {
                writer.write(String.format("%06X\n", memory.read(i)));
                // TODO; Revisar.
            }
            System.out.println("Memória salva em " + filePath);
        }   catch (IOException e) {
            System.out.println("Erro ao salvar a memória: " + e.getMessage());
        }
    }

    public void loadMemoryFromFile(Memory memory, String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;;
            int address = 0;
            while ((line = reader.readLine()) != null) {
                memory.write(address++, String.valueOf(Integer.parseInt(line, 16)));
            } // TODO; Revisar.
            System.out.println("Memória carregada de " + filePath);
        } catch (IOException e) {
            System.out.println("Erro ao carregar a memória: " + e.getMessage());
        }
    }
}
