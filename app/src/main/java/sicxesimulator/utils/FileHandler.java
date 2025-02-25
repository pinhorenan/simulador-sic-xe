package sicxesimulator.utils;

import sicxesimulator.model.components.Memory;
import java.io.*;
import java.util.*;

public class FileHandler {

    private List<String> fileContent;

    public FileHandler() {
        this.fileContent = new ArrayList<>();
    }

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

    public void loadMemoryFromFile(Memory memory, String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
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

    public void clear() {
        fileContent.clear();  // Limpa o conteúdo armazenado
    }

    public void saveObjectFile(List<String> objectRecords, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String record : objectRecords) {
                writer.write(record);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo objeto: " + e.getMessage());
        }
    }

    public List<String> readObjectFile(String filePath) {
        List<String> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            return records;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo objeto: " + e.getMessage());
        }
    }
}
