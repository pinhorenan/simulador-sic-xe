package sicxesimulator;

import java.io.*;
import java.util.*;

/**
 * Montador para a arquitetura SIC/XE.
 */
public class Assembler {
    // Tabela de símbolos que mapeia rótulos para seus endereços de memória
    private Map<String, Integer> symbolTable;
    // Lista que armazenará o código objeto gerado
    private List<String> objectCode;
    // Endereço inicial do programa
    private int startAddress;
    // Contador de localização para rastrear endereços de instrução
    private int locationCounter;
    
    // Tabela de opcodes mapeando cada mnemônico SIC/XE para seu código hexadecimal
    private static final Map<String, String> OPCODE_TABLE = Map.ofEntries(
        Map.entry("ADD", "18"), Map.entry("ADDR", "90"),
        Map.entry("AND", "40"), Map.entry("CLEAR", "4"), Map.entry("COMP", "28"),
        Map.entry("COMPR", "A0"), Map.entry("DIV", "24"), Map.entry("DIVR", "9C"),
        Map.entry("J", "3C"), Map.entry("JEQ", "30"), Map.entry("JGT", "34"),
        Map.entry("JLT", "38"), Map.entry("JSUB", "48"), Map.entry("LDA", "00"),
        Map.entry("LDB", "68"), Map.entry("LDCH", "50"), Map.entry("LDL", "08"),
        Map.entry("LDS", "6C"), Map.entry("LDT", "74"), Map.entry("LDX", "04"),
        Map.entry("MUL", "20"), Map.entry("OR", "44"), Map.entry("RSUB", "4C"),
        Map.entry("STA", "0C"), Map.entry("STB", "78"), Map.entry("STCH", "54"),
        Map.entry("STL", "14"), Map.entry("STS", "7C"), Map.entry("STT", "84"),
        Map.entry("STX", "10"), Map.entry("SUB", "1C"), Map.entry("SUBR", "94"),
        Map.entry("TIX", "2C"), Map.entry("TIXR", "B8"), Map.entry("WD", "DC")
    );

    /**
     * Construtor do Montador.
     * Inicializa a tabela de símbolos e a lista de código objeto.
     */
    public Assembler() {
        symbolTable = new HashMap<>();
        objectCode = new ArrayList<>();
    }

    /**
     * Método principal para montar um arquivo fonte SIC/XE.
     * Executa as duas passagens do montador e gera o arquivo objeto.
     */
    public void assemble(String sourceFile, String outputFile) throws IOException {
        List<String> lines = readSourceFile(sourceFile);
        firstPass(lines);
        secondPass(lines, outputFile);
    }

    /**
     * Lê o arquivo fonte e retorna uma lista de linhas.
     */
    private List<String> readSourceFile(String sourceFile) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Primeira passagem do montador.
     * Coleta rótulos e calcula os endereços de memória.
     */
    private void firstPass(List<String> lines) {
        locationCounter = 0;
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0 || parts[0].startsWith(".")) continue;

            if (parts[0].equalsIgnoreCase("START")) {
                startAddress = Integer.parseInt(parts[1], 16);
                locationCounter = startAddress;
                continue;
            }
            
            if (!OPCODE_TABLE.containsKey(parts[0])) {
                symbolTable.put(parts[0], locationCounter);
            }
            
            locationCounter += 3;
        }
    }

    /**
     * Segunda passagem do montador.
     * Converte as instruções em código objeto e escreve no arquivo de saída.
     */
    private void secondPass(List<String> lines, String outputFile) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            for (String line : lines) {
                String objectCodeLine = generateObjectCode(line);
                if (objectCodeLine != null) {
                    objectCode.add(objectCodeLine);
                    bw.write(objectCodeLine);
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Converte uma linha do código-fonte em código de máquina SIC/XE.
     */
    private String generateObjectCode(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].startsWith(".")) {
            return null;
        }

        // Obtém o opcode correspondente ao mnemônico
        String opcode = OPCODE_TABLE.getOrDefault(parts[0], "??");
        String operand = "0000";
        
        // Processa operandos e modos de endereçamento
        if (parts.length > 1) {
            String operandValue = parts[1];
            boolean immediate = operandValue.startsWith("#");
            boolean indirect = operandValue.startsWith("@");
            
            operandValue = operandValue.replace("#", "").replace("@", "");
            operand = String.format("%04X", symbolTable.getOrDefault(operandValue, 0));
            
            // Define os bits de endereçamento (n, i)
            String niBits = immediate ? "01" : indirect ? "10" : "11";
            opcode = Integer.toHexString(Integer.parseInt(opcode, 16) | Integer.parseInt(niBits, 2)).toUpperCase();
        }

        return opcode + operand;
    }
}
