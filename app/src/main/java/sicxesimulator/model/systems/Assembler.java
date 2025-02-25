package sicxesimulator.model.systems;

import sicxesimulator.model.components.operations.Instruction;
import sicxesimulator.utils.FileHandler;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Assembler {

    private Map<String, Integer> symbolTable = new HashMap<>();
    private List<Instruction> instructions = new ArrayList<>();
    private String programName = "PROG"; // Default
    private int programLength = 0;
    private int locationCounter = 0;
    private int startAddress = 0;
    private int executionStartAddress = 0;

    private List<TextRecord> textRecords = new ArrayList<>();
    private TextRecord currentRecord;

    private static final Map<String, String> OPCODE_TABLE = new HashMap<>();
    private static final Set<String> DIRECTIVES = new HashSet<>(Arrays.asList(
            "START", "END", "BYTE", "WORD", "RESB", "RESW", "BASE", "NOBASE", "EQU", "ORG", "LTORG"
    ));

    static {
        OPCODE_TABLE.put("ADD", "18");
        OPCODE_TABLE.put("ADDR", "90");
        OPCODE_TABLE.put("AND", "40");
        OPCODE_TABLE.put("CLEAR", "4");
        OPCODE_TABLE.put("COMP", "28");
        OPCODE_TABLE.put("COMPR", "A0");
        OPCODE_TABLE.put("DIV", "24");
        OPCODE_TABLE.put("DIVR", "9C");
        OPCODE_TABLE.put("J", "3C");
        OPCODE_TABLE.put("JEQ", "30");
        OPCODE_TABLE.put("JGT", "34");
        OPCODE_TABLE.put("JLT", "38");
        OPCODE_TABLE.put("JSUB", "48");
        OPCODE_TABLE.put("LDA", "00");
        OPCODE_TABLE.put("LDB", "68");
        OPCODE_TABLE.put("LDCH", "50");
        OPCODE_TABLE.put("LDL", "08");
        OPCODE_TABLE.put("LDS", "6C");
        OPCODE_TABLE.put("LDT", "74");
        OPCODE_TABLE.put("LDX", "04");
        OPCODE_TABLE.put("MUL", "20");
        OPCODE_TABLE.put("OR", "44");
        OPCODE_TABLE.put("RSUB", "4C");
        OPCODE_TABLE.put("STA", "0C");
        OPCODE_TABLE.put("STB", "78");
        OPCODE_TABLE.put("STCH", "54");
        OPCODE_TABLE.put("STL", "14");
        OPCODE_TABLE.put("STS", "7C");
        OPCODE_TABLE.put("STT", "84");
        OPCODE_TABLE.put("STX", "10");
        OPCODE_TABLE.put("SUB", "1C");
        OPCODE_TABLE.put("SUBR", "94");
        OPCODE_TABLE.put("TIX", "2C");
        OPCODE_TABLE.put("TIXR", "B8");
        OPCODE_TABLE.put("WD", "DC");
    }

    public void assemble(String sourceFile, String objFile) throws IOException {
        List<String> sourceLines = readSourceFile(sourceFile);
        assembleToFile(sourceLines, objFile);
    }

    public void assembleToFile(List<String> sourceLines, String objFile) throws IOException {
        firstPass(sourceLines);
        secondPass(sourceLines);
        writeObjectFile(objFile);
    }

    private List<String> readSourceFile(String fileName) throws IOException {
        FileHandler fileHandler = new FileHandler();
        List<String> lines = fileHandler.readFileLines(fileName);
        if (lines == null) throw new IOException("Falha ao ler arquivo fonte");
        return lines;
    }

    private void firstPass(List<String> lines) {
        locationCounter = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith(".")) continue;

            String[] parts = line.split("\\s+");
            if (parts.length == 0) continue;

            String label = null;
            String opcode;
            String[] operands;
            int index = 0;

            // Check for label
            if (!isOpcodeOrDirective(parts[0])) {
                label = parts[0];
                index++;
                if (index >= parts.length) {
                    System.err.println("Error: Missing opcode after label in line: " + line);
                    continue;
                }
            }

            opcode = parts[index].toUpperCase();
            index++;
            operands = Arrays.copyOfRange(parts, index, parts.length);

            // Handle START
            if (opcode.equals("START")) {
                if (operands.length > 0) {
                    try {
                        startAddress = Integer.parseInt(operands[0], 16);
                        locationCounter = startAddress;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid start address: " + operands[0]);
                    }
                }
                if (label != null) {
                    programName = label;
                    symbolTable.put(label, locationCounter);
                }
                continue;
            }

            if (opcode.equals("END")) {
                if (operands.length > 0) {
                    String endLabel = operands[0];
                    if (!symbolTable.containsKey(endLabel)) {
                        throw new IllegalArgumentException("Símbolo não definido no END: " + endLabel);
                    }
                    executionStartAddress = symbolTable.get(endLabel); // Corrigido para executionStartAddress
                } else {
                    executionStartAddress = startAddress; // Valor padrão
                }

                continue;
            }

            // Add label to symbol table
            if (label != null) {
                if (symbolTable.containsKey(label)) {
                    System.err.println("Error: Duplicate label " + label);
                } else {
                    symbolTable.put(label, locationCounter);
                }
            }

            // Update location counter
            locationCounter += getInstructionSize(opcode, operands);
        }
    }

    private boolean isOpcodeOrDirective(String token) {
        return OPCODE_TABLE.containsKey(token.toUpperCase()) || DIRECTIVES.contains(token.toUpperCase());
    }

    private int getInstructionSize(String opcode, String[] operands) {
        switch (opcode) {
            case "START":
            case "END":
                return 0;
            case "BYTE":
                if (operands.length == 0) return 0;
                String operand = operands[0];
                if (operand.matches("(?i)C'.*'")) {
                    return operand.length() - 3;
                } else if (operand.matches("(?i)X'.*'")) {
                    String hex = operand.substring(2, operand.length() - 1);
                    return (hex.length() + 1) / 2;
                } else {
                    return 1;
                }
            case "WORD":
                return 3;
            case "RESB":
                return operands.length > 0 ? Integer.parseInt(operands[0]) : 0;
            case "RESW":
                return operands.length > 0 ? 3 * Integer.parseInt(operands[0]) : 0;
            default:
                return OPCODE_TABLE.containsKey(opcode) ? 3 : 0;
        }
    }

    private void secondPass(List<String> lines) {
        locationCounter = startAddress;
        currentRecord = new TextRecord(startAddress);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith(".")) continue;

            String[] parts = line.split("\\s+");
            if (parts.length == 0) continue;

            String label = null;
            String opcode;
            String[] operands;
            int index = 0;

            if (!isOpcodeOrDirective(parts[0])) {
                label = parts[0];
                index++;
                if (index >= parts.length) continue;
            }

            opcode = parts[index].toUpperCase();
            index++;
            operands = Arrays.copyOfRange(parts, index, parts.length);

            if (opcode.equals("START") || opcode.equals("END")) continue;

            int size = getInstructionSize(opcode, operands);
            List<Integer> bytes = generateObjectCode(opcode, operands);

            if (bytes.isEmpty()) {
                if (!currentRecord.isEmpty()) {
                    textRecords.add(currentRecord);
                    currentRecord = new TextRecord(locationCounter + size);
                }
                locationCounter += size;
                continue;
            }

            if (currentRecord.isEmpty()) {
                currentRecord.startAddress = locationCounter;
            } else if (currentRecord.startAddress + currentRecord.bytes.size() != locationCounter) {
                textRecords.add(currentRecord);
                currentRecord = new TextRecord(locationCounter);
            }

            currentRecord.bytes.addAll(bytes);
            locationCounter += size;
            programLength += bytes.size();
        }

        if (!currentRecord.isEmpty()) {
            textRecords.add(currentRecord);
        }
    }

    private List<Integer> generateObjectCode(String opcode, String[] operands) {
        List<Integer> bytes = new ArrayList<>();

        if (DIRECTIVES.contains(opcode)) {
            switch (opcode) {
                case "BYTE":
                    if (operands.length == 0) break;
                    String operand = operands[0];
                    if (operand.startsWith("C'")) {
                        String str = operand.substring(2, operand.length() - 1);
                        for (char c : str.toCharArray()) {
                            bytes.add((int) c);
                        }
                    } else if (operand.startsWith("X'")) {
                        String hex = operand.substring(2, operand.length() - 1);
                        if (hex.length() % 2 != 0) hex = "0" + hex;
                        for (int i = 0; i < hex.length(); i += 2) {
                            bytes.add(Integer.parseInt(hex.substring(i, i + 2), 16));
                        }
                    } else {
                        bytes.add(Integer.parseInt(operand) & 0xFF);
                    }
                    break;
                case "WORD":
                    if (operands.length == 0) break;
                    String symbol = operands[0];
                    int value;
                    if (symbolTable.containsKey(symbol)) { // Trata símbolos
                        value = symbolTable.get(symbol);
                    } else {
                        try {
                            value = Integer.decode(symbol.startsWith("0X") ? symbol : "0X" + symbol);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Operando inválido para WORD: " + symbol);
                        }
                    }
                    // ... (restante do código existente) ...
                    break;
            }
        } else if (OPCODE_TABLE.containsKey(opcode)) {
            String opHex = OPCODE_TABLE.get(opcode);
            int opcodeByte = Integer.parseInt(opHex, 16);

            int address = 0;
            boolean indexed = false;
            if (operands.length > 0) {
                String processed = processOperand(operands[0]);
                String[] parts = processed.split(",", 2);
                address = Integer.parseInt(parts[0], 16);
                indexed = parts.length > 1 && parts[1].equalsIgnoreCase("X");
            }

            address |= indexed ? 0x8000 : 0;
            bytes.add(opcodeByte);
            bytes.add((address >> 8) & 0xFF);
            bytes.add(address & 0xFF);
        }

        return bytes;
    }

    private String processOperand(String operand) {
        String[] parts = operand.split(",", 2);
        String symbol = parts[0];
        String suffix = parts.length > 1 ? "," + parts[1] : "";

        if (symbolTable.containsKey(symbol)) {
            return String.format("%04X", symbolTable.get(symbol)) + suffix;
        } else {
            try {
                int value = symbol.startsWith("0X")
                        ? Integer.parseInt(symbol.substring(2), 16)
                        : Integer.parseInt(symbol);
                return String.format("%04X", value) + suffix;
            } catch (NumberFormatException e) {
                System.err.println("Undefined symbol: " + symbol);
                return "0000" + suffix;
            }
        }
    }

    private void writeObjectFile(String fileName) throws IOException {
        FileHandler fileHandler = new FileHandler();
        List<String> objectRecords = new ArrayList<>();

        // Header
        objectRecords.add(String.format("H%-6s%06X%06X",
                programName,
                startAddress,
                programLength
        ));

        // Text Records
        for (TextRecord record : textRecords) {
            if (record.bytes.isEmpty()) continue;
            String data = record.bytes.stream()
                    .map(b -> String.format("%02X", b))
                    .collect(Collectors.joining());
            objectRecords.add(String.format("T%06X%02X%s",
                    record.startAddress,
                    record.bytes.size(),
                    data
            ));
        }

        // End Record
        objectRecords.add("E" + String.format("%06X", executionStartAddress));


        fileHandler.saveObjectFile(objectRecords, fileName);
    }

    private static class TextRecord {
        int startAddress;
        List<Integer> bytes = new ArrayList<>();

        TextRecord(int start) {
            startAddress = start;
        }

        boolean isEmpty() {
            return bytes.isEmpty();
        }
    }

    public Map<String, Integer> getSymbolTable() {
        return symbolTable;
    }
}