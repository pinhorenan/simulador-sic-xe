package sicxesimulator.simulation.systems;

import java.util.*;
import sicxesimulator.simulation.virtualMachine.operations.Instruction;

/**
 * Montador para a arquitetura SIC/XE.
 */
public class Assembler {
    private Map<String, Integer> symbolTable = new HashMap<>();
    private List<Instruction> instructions = new ArrayList<>();
    private int locationCounter = 0;
    private int startAddress = 0;

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
            if (!OPCODE_TABLE.containsKey(parts[0])) {
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
            Instruction instruction = new Instruction("", mnemonic, new String[]{operand}, null, lineNumber);
            instructions.add(instruction);
        }
    }
}