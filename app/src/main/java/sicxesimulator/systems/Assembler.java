package sicxesimulator.systems;

import java.util.*;
import sicxesimulator.components.operations.Instruction;

/**
 * Montador para a arquitetura SIC/XE.
 */

// TODO Tradução das variáveis para os seus valores
public class Assembler {
    private Map<String, Integer> symbolTable = new HashMap<>();
    private List<Instruction> instructions = new ArrayList<>();
    private int locationCounter = 0;
    private int startAddress = 0;

    // Tabela de opcodes mapeando cada mnemônico SIC/XE para seu código hexadecimal
    private static final Map<String, String> OPCODE_TABLE = new HashMap<>();
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

    public List<Instruction> assemble(List<String> sourceLines) {
        firstPass(sourceLines);
        secondPass(sourceLines);
        return instructions;
    }

    private void firstPass(List<String> lines) {
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0 || parts[0].startsWith(".")) continue;

            if (parts[0].equalsIgnoreCase("START")) {
                startAddress = Integer.parseInt(parts[1], 16);
                locationCounter = startAddress;
                continue;
            }

            // Se a primeira palavra não é um mnemônico, assume-se label
            if (!OPCODE_TABLE.containsKey(parts[0].toUpperCase())) {
                symbolTable.put(parts[0], locationCounter);
            }
            locationCounter += 3; // Supõe-se instruções de tamanho fixo (3 bytes); TODO, Revisar.
        }
    }

    private void secondPass(List<String> lines) {
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 0 || parts[0].startsWith(".")) continue;

            String mnemonic;
            String operand = "";

            if (parts[0].equalsIgnoreCase("START")) {
                continue;
            }

            if (!OPCODE_TABLE.containsKey(parts[0].toUpperCase())) {
                // Primeira palavra é label, então segunda é mnemônico
                mnemonic = parts[1].toUpperCase();
                if (parts.length >= 3) {
                    operand = parts[2];
                }
            } else {
                mnemonic = parts[0].toUpperCase();
                if (parts.length >= 2) {
                    operand = parts[1];
                }
            }
            Instruction instruction = new Instruction("", mnemonic, new String[]{operand}, lineNumber);
            instructions.add(instruction);
        }
    }
}