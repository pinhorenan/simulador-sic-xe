package sicxesimulator.utils;

import sicxesimulator.components.Memory;
import sicxesimulator.components.operations.Instruction;
import java.io.*;
import java.util.*;

public class FileHandler {

    private List<String> fileContent;

    public FileHandler() {
        this.fileContent = new ArrayList<>();
    }
    ///  WIP
    @SuppressWarnings("unused")
    public List<Instruction> loadInstructionsFromFile(String filePath) {
        // TODO
        // Se desejar usar o Assembler para montar, use o método readFileLines e passe para o Assembler.
        // Mas se você quiser ler diretamente instruções simples, pode continuar assim:
        List<Instruction> instructions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] tokens = line.split("\\s+");
                if (tokens.length == 0 || tokens[0].startsWith(".")) continue;
                String label = "";
                String mnemonic;
                String operand = "";
                if (tokens.length == 1) {
                    mnemonic = tokens[0];
                } else if (tokens.length == 2) {
                    mnemonic = tokens[0];
                    operand = tokens[1];
                } else {
                    label = tokens[0];
                    mnemonic = tokens[1];
                    operand = tokens[2];
                }
                instructions.add(new Instruction(label, mnemonic, new String[]{operand}, lineNumber));
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo de instruções: " + e.getMessage());
            return null;
        }
        return instructions;
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
}
