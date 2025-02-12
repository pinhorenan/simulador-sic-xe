package sicxesimulator.simulation.systems;

import sicxesimulator.simulation.virtualMachine.Memory;
import java.io.*;
import java.util.*;

public class FileHandler {

    // Lê um arquivo assembly e retorna uma lista de instruções
    public List<String> readFileLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            return null;
        }
        return lines;
    }


    // Salva o conteúdo da memória em um arquivo
    public void saveMemoryToFile(Memory memory, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            int size = memory.getSize();
            for (int i = 0; i < size; i++) {
                writer.write(String.format("%04X: %s", i, memory.read(i)));
                writer.newLine();
            }
            System.out.println("Memória salva com sucesso no arquivo: " + filePath);
        } catch (IOException e) {
            System.out.println("Erro ao salvar a memória: " + e.getMessage());
        }
    }

    // Carrega o conteúdo da memória a partir de um arquivo
    public void loadMemoryFromFile(Memory memory, String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Espera o formato "endereço: valor"
                String[] parts = line.split(":");
                if (parts.length < 2) continue;
                int address = Integer.parseInt(parts[0].trim(), 16);
                String value = parts[1].trim();
                memory.write(address, value);
            }
            System.out.println("Memória carregada com sucesso do arquivo: " + filePath);
        } catch (IOException e) {
            System.out.println("Erro ao carregar a memória: " + e.getMessage());
        }
    }
}