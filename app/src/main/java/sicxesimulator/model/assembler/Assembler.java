package sicxesimulator.model.assembler;

import java.io.*;
import java.util.*;

public class Assembler {
    private Map<String, Integer> symbolTable = new HashMap<>();
    private List<AssemblyLine> intermediateLines = new ArrayList<>();
    private int startAddress = 0;
    private int programLength = 0;
    private int baseRegister = 0;

    /**
     * Realiza a montagem completa: escreve as linhas-fonte em um arquivo temporário,
     * executa passOne e passTwo para gerar o arquivo objeto.
     */
    public void assembleToFile(List<String> sourceLines, String outputFilePath) throws IOException {
        // Escreve o código fonte em um arquivo temporário
        File tempSourceFile = File.createTempFile("tempSource", ".asm");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempSourceFile))) {
            for (String line : sourceLines) {
                writer.write(line);
                writer.newLine();
            }
        }
        // Executa a Passagem 1 para construir a symbolTable e linhas intermediárias
        passOne(tempSourceFile.getAbsolutePath());
        // Executa a Passagem 2 para gerar o arquivo objeto com os registros H, T, M e E
        passTwo(outputFilePath);
        // Remove o arquivo temporário
        tempSourceFile.delete();
    }

    public Map<String, Integer> getSymbolTable() {
        return symbolTable;
    }

    public void passOne(String sourceFile) throws IOException {
        int locctr = 0;
        boolean programStarted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith(";"))
                    continue; // Ignora linhas vazias ou comentários

                AssemblyLine asmLine = parseLine(line);

                if (asmLine.getOperation().equalsIgnoreCase("START")) {
                    locctr = Integer.parseInt(asmLine.getOperand(), 16);
                    startAddress = locctr;
                    asmLine.setAddress(locctr);
                    programStarted = true;
                } else if (asmLine.getOperation().equalsIgnoreCase("END")) {
                    programLength = locctr - startAddress;
                    asmLine.setAddress(locctr);
                    intermediateLines.add(asmLine);
                    break;
                }

                if (programStarted) {
                    // Atualiza a tabela de símbolos caso haja rótulo
                    if (!asmLine.getLabel().isEmpty()) {
                        if (symbolTable.containsKey(asmLine.getLabel())) {
                            throw new AssemblerException("Símbolo duplicado: " + asmLine.getLabel());
                        }
                        symbolTable.put(asmLine.getLabel(), locctr);
                    }

                    asmLine.setAddress(locctr);

                    // Atualiza o LOCCTR com o tamanho da instrução ou diretiva
                    locctr += calculateOperationSize(asmLine.getOperation(), asmLine.getOperand());

                    intermediateLines.add(asmLine);
                }
            }
        }
    }

    public void passTwo(String objectFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(objectFile))) {
            // Cabeçalho: H<ProgramName:6><StartAddress:6><ProgramLength:6>
            writer.write(String.format("H%-6s%06X%06X",
                    intermediateLines.get(0).getLabel(), startAddress, programLength));

            List<String> currentTextRecord = new ArrayList<>();
            int currentStartAddress = startAddress;

            for (AssemblyLine line : intermediateLines) {
                if (line.getOperation().equalsIgnoreCase("START") || line.getOperation().equalsIgnoreCase("END"))
                    continue;

                String objectCode = generateObjectCode(line, currentStartAddress, baseRegister);
                if (objectCode.isEmpty())
                    continue;

                if (currentTextRecord.isEmpty()) {
                    currentStartAddress = line.getAddress();
                }

                currentTextRecord.add(objectCode);

                // Se o registro T ultrapassar 30 bytes, escreve-o
                int totalBytes = currentTextRecord.stream().mapToInt(code -> code.length() / 2).sum();
                if (totalBytes > 30) {
                    writeTextRecord(writer, currentStartAddress, currentTextRecord);
                    currentTextRecord.clear();
                }
            }

            if (!currentTextRecord.isEmpty()) {
                writeTextRecord(writer, currentStartAddress, currentTextRecord);
            }

            writeModificationRecords(writer);
            writer.write(String.format("\nE%06X", startAddress));
        }
    }

    private String generateObjectCode(AssemblyLine line, int pc, int baseRegister) {
        // Tratar instruções sem operando – por exemplo, RSUB
        if (line.getOperation().equalsIgnoreCase("RSUB")) {
            return "4C0000";
        }
        if (isDirective(line.getOperation()))
            return "";

        int opcode = getOpcode(line.getOperation());
        int[] addressingMode = decodeAddressingMode(line.getOperand(), pc, baseRegister);
        int disp = addressingMode[0];
        int flags = addressingMode[1];

        if (line.getOperation().startsWith("+")) { // Formato 4
            return String.format("%02X%05X", opcode, disp & 0xFFFFF);
        } else { // Formato 3
            return String.format("%02X%03X", (opcode << 2) | (flags >> 4), disp & 0xFFF);
        }
    }

    private boolean isDirective(String operation) {
        return Arrays.asList("START", "END", "BYTE", "WORD", "RESB", "RESW", "BASE", "NOBASE")
                .contains(operation.toUpperCase());
    }

    public AssemblyLine parseLine(String line) {
        String label = "";
        String operation = "";
        String operand = "";
        String comment = "";

        line = line.trim();

        int commentIndex = line.indexOf(';');
        if (commentIndex != -1) {
            comment = line.substring(commentIndex + 1).trim();
            line = line.substring(0, commentIndex).trim();
        }

        String[] tokens = line.split("\\s+", 3);

        if (tokens.length == 1) {
            operation = tokens[0];
        } else if (tokens.length == 2) {
            if (isOperation(tokens[0])) {
                operation = tokens[0];
                operand = tokens[1];
            } else {
                label = tokens[0];
                operation = tokens[1];
            }
        } else if (tokens.length == 3) {
            label = tokens[0];
            operation = tokens[1];
            operand = tokens[2];
        }

        return new AssemblyLine(label, operation, operand, comment);
    }

    private int calculateOperationSize(String operation, String operand) {
        return switch (operation.toUpperCase()) {
            case "WORD" -> 3;
            case "RESW" -> 3 * Integer.parseInt(operand);
            case "RESB" -> Integer.parseInt(operand);
            case "BYTE" -> calculateByteSize(operand);
            case "CLEAR", "ADDR", "SUBR", "MULR", "DIVR", "COMPR", "SHIFTL", "SHIFTR", "RMO", "TIXR" -> 2;
            case "FIX", "FLOAT", "HIO", "NORM", "SIO", "TIO" -> 1;
            case "START", "END", "BASE", "NOBASE" -> 0;
            case "RSUB" -> 3;
            default -> getInstructionSize(operation, operand);
        };
    }

    private int calculateByteSize(String operand) {
        if (operand.startsWith("C'")) {
            return operand.length() - 3;
        } else if (operand.startsWith("X'")) {
            return (operand.length() - 3) / 2;
        } else {
            throw new AssemblerException("Formato inválido para BYTE: " + operand);
        }
    }

    private int getInstructionSize(String operation, String operand) {
        if (operand.startsWith("+")) {
            return 4;
        }
        return 3;
    }

    private boolean isOperation(String token) {
        String[] operations = {
                "START", "END", "LDA", "ADD", "SUB", "STA", "LDX", "RSUB", "JSUB",
                "J", "JEQ", "JLT", "JGT", "COMP", "TIX", "CLEAR", "BASE", "NOBASE",
                "WORD", "BYTE", "RESW", "RESB"
        };
        return Arrays.asList(operations).contains(token.toUpperCase());
    }

    private int[] decodeAddressingMode(String operand, int pc, int baseRegister) {
        // Se o operando for vazio, podemos considerar 0 (caso de RSUB, por exemplo)
        if (operand.isEmpty()) {
            return new int[]{0, 0};
        }
        boolean immediate = operand.startsWith("#");
        boolean indirect = operand.startsWith("@");
        boolean indexed = operand.endsWith(",X");
        boolean extended = operand.startsWith("+");

        String cleanOperand = operand.replace("#", "").replace("@", "").replace(",X", "").replace("+", "");

        int address;
        if (symbolTable.containsKey(cleanOperand)) {
            address = symbolTable.get(cleanOperand);
        } else if (cleanOperand.matches("[0-9A-Fa-f]+")) {
            address = Integer.parseInt(cleanOperand, 16);
        } else {
            throw new AssemblerException("Operando inválido: " + operand);
        }

        int disp;
        if (extended) {
            disp = address;
        } else if (immediate || indirect) {
            disp = address - pc;
        } else {
            if (address - baseRegister >= 0 && address - baseRegister <= 4095) {
                disp = address - baseRegister;
            } else {
                disp = address - pc;
            }
        }

        int flags = 0;
        if (immediate) flags |= 0x01;
        if (indirect) flags |= 0x02;
        if (indexed) flags |= 0x10;
        if (extended) flags |= 0x01;

        return new int[]{disp, flags};
    }

    private int getOpcode(String operation) {
        return switch (operation.replace("+", "").toUpperCase()) {
            case "LDA" -> 0x00;
            case "ADD" -> 0x18;
            case "SUB" -> 0x1C;
            case "STA" -> 0x0C;
            case "LDX" -> 0x04;
            case "RSUB" -> 0x4C;
            case "J" -> 0x3C;
            case "JEQ" -> 0x30;
            case "JLT" -> 0x38;
            case "JGT" -> 0x34;
            case "JSUB" -> 0x48;
            case "COMP" -> 0x28;
            case "TIX" -> 0x2C;
            case "CLEAR" -> 0xB4;
            case "ADDR" -> 0x90;
            case "SUBR" -> 0x94;
            case "MULR" -> 0x98;
            case "DIVR" -> 0x9C;
            case "COMPR" -> 0xA0;
            case "SHIFTL" -> 0xA4;
            case "SHIFTR" -> 0xA8;
            case "RMO" -> 0xAC;
            case "TIXR" -> 0xB8;
            default -> throw new AssemblerException("Operação desconhecida: " + operation);
        };
    }

    // Escreve um registro T no arquivo objeto
    private void writeTextRecord(BufferedWriter writer, int startAddress, List<String> textRecord) throws IOException {
        StringBuilder recordData = new StringBuilder();
        for (String code : textRecord) {
            recordData.append(code);
        }
        int length = recordData.length() / 2;
        writer.write(String.format("\nT%06X%02X%s", startAddress, length, recordData.toString()));
    }

    // Escreve os registros de modificação (M) para instruções de formato 4
    private void writeModificationRecords(BufferedWriter writer) throws IOException {
        for (AssemblyLine line : intermediateLines) {
            if (line.getOperation().startsWith("+")) {
                writer.write(String.format("\nM%06X05", line.getAddress() + 1));
            }
        }
    }
}
