package sicxesimulator.assembler.processing;

import sicxesimulator.assembler.models.AssemblyLine;
import sicxesimulator.assembler.models.IntermediateRepresentation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class FirstPassProcessor {
    private static final Logger logger = Logger.getLogger(FirstPassProcessor.class.getName());

    private static final Set<String> VALID_MNEMONICS = new HashSet<>(Arrays.asList(
            "START", "END", "BYTE", "WORD", "RESB", "RESW", "BASE", "NOBASE", "EQU", "LTORG",
            "ADD", "ADDR", "AND", "CLEAR", "COMP", "COMPR", "DIV", "DIVR",
            "J", "JEQ", "JGT", "JLT", "JSUB", "LDA", "LDB", "LDCH", "LDL", "LDS",
            "LDT", "LDX", "MUL", "MULR", "OR", "RMO", "RSUB", "SHIFTL", "SHIFTR",
            "STA", "STB", "STCH", "STL", "STS", "STT", "STX", "SUB", "SUBR", "TIX",
            "TIXR"
    ));

    private int locationCounter = 0;
    private int startAddress = 0;

    public IntermediateRepresentation process(List<String> sourceLines) {
        boolean endFound = false;
        IntermediateRepresentation midCode = new IntermediateRepresentation();
        int lineNumber = 0;

        for (String line : sourceLines) {
            lineNumber++;
            line = line.trim();

            if (line.isEmpty() || line.startsWith(".")) {
                continue;
            }

            String[] parts = line.split("\\s+", 3);
            String label = null;
            String mnemonic = null;
            String operand = null;

            if (parts.length > 0) {
                if (isMnemonic(parts[0])) {
                    mnemonic = parts[0];
                    if (parts.length > 1) {
                        operand = parts[1];
                    }
                } else if (parts.length > 1 && isMnemonic(parts[1])) {
                    label = parts[0];
                    mnemonic = parts[1];
                    if (parts.length > 2) {
                        operand = parts[2];
                    }
                }
            }

            if (mnemonic == null) {
                throw new IllegalArgumentException("Linha inválida na linha " + lineNumber + ": " + line);
            }

            if (mnemonic.equalsIgnoreCase("START")) {
                try {
                    startAddress = parseAddress(operand);
                    locationCounter = startAddress;
                    midCode.setStartAddress(startAddress);
                    logger.info("Start Address definido como: " + Integer.toHexString(startAddress));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Erro ao processar START na linha " + lineNumber + ": " + operand);
                }
                if (label != null) {
                    midCode.addSymbol(label, locationCounter);
                    midCode.setProgramName(label);
                }
                continue;
            }

            if (mnemonic.equalsIgnoreCase("END")) {
                endFound = true;
                midCode.setFinalAddress(locationCounter);
                logger.info("Diretiva END encontrada na linha " + lineNumber);
                continue;
            }

            if (label != null) {
                midCode.addSymbol(label, locationCounter);
                logger.info("Registrando símbolo '" + label + "' no endereço: " + Integer.toHexString(locationCounter));
            }

            int size = getInstructionSize(mnemonic, operand);
            AssemblyLine asmLine = new AssemblyLine(label, mnemonic, operand, locationCounter, lineNumber);
            midCode.addAssemblyLine(asmLine);
            locationCounter += size;
        }

        if (!endFound) {
            throw new IllegalArgumentException("Diretiva END não encontrada.");
        }

        return midCode;
    }

    private int parseAddress(String operand) {
        if (operand == null) {
            throw new IllegalArgumentException("Operando ausente para endereço.");
        }
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        } else if (operand.matches("[0-9A-Fa-f]+")) {
            return Integer.parseInt(operand, 16);
        }
        throw new IllegalArgumentException("Formato inválido de endereço: " + operand);
    }

    private boolean isMnemonic(String token) {
        return VALID_MNEMONICS.contains(token.toUpperCase());
    }

    private int getInstructionSize(String mnemonic, String operand) {
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return 1;
        }
        if (mnemonic.equalsIgnoreCase("RESW") || mnemonic.equalsIgnoreCase("RESB")) {
            if (operand == null) {
                throw new IllegalArgumentException("Operando ausente para a diretiva " + mnemonic);
            }
            return mnemonic.equalsIgnoreCase("RESW") ? Integer.parseInt(operand) : (Integer.parseInt(operand) + 2) / 3;
        }
        if (mnemonic.equalsIgnoreCase("BYTE")) {
            if (operand == null) throw new IllegalArgumentException("Operando ausente para BYTE.");
            return operand.startsWith("C'") ? (operand.length() - 3 + 2) / 3 : operand.startsWith("X'") ? (operand.length() - 3 + 1) / 2 : 1;
        }
        return 1;
    }

    public void reset() {
        locationCounter = 0;
        startAddress = 0;
    }
}
