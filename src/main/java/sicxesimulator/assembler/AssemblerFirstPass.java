package sicxesimulator.assembler;

import sicxesimulator.utils.SimulatorLogger;
import sicxesimulator.models.AssemblyLine;
import sicxesimulator.models.IntermediateRepresentation;
import sicxesimulator.utils.Check;

import java.util.List;

class AssemblerFirstPass {
    private int locationCounter = 0;
    private int startAddress = 0;

    /**
     * Processa as linhas de código-fonte e gera uma IntermediateRepresentation.
     *
     * @param sourceLines Lista de linhas de código assembly.
     * @return Representação intermediária contendo linhas de assembly, símbolos e endereços.
     */
    protected IntermediateRepresentation process(List<String> sourceLines) {
        boolean endFound = false;
        IntermediateRepresentation midCode = new IntermediateRepresentation();
        midCode.setRawSourceCode(sourceLines); // 🔹 Agora armazenamos o código-fonte original.

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
                if (Check.isMnemonic(parts[0])) {
                    mnemonic = parts[0];
                    if (parts.length > 1) {
                        operand = parts[1];
                    }
                } else if (parts.length > 1 && Check.isMnemonic(parts[1])) {
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
                } catch (Exception e) {
                    throw new IllegalArgumentException("Erro ao processar START na linha " + lineNumber + ": " + operand, e);
                }
                if (label != null) {
                    midCode.addSymbol(label, locationCounter);
                    midCode.setProgramName(label);
                }
                continue;
            }

            if (mnemonic.equalsIgnoreCase("END")) {
                endFound = true;
                continue;
            }

            if (label != null) {
                midCode.addSymbol(label, locationCounter);
            }

            int size = getInstructionSize(mnemonic, operand);
            AssemblyLine asmLine = new AssemblyLine(label, mnemonic, operand, locationCounter);
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
            String errorMsg = "Operando ausente para endereço.";
            SimulatorLogger.logError(errorMsg, null);
            throw new IllegalArgumentException(errorMsg);
        }
        if (operand.matches("\\d+")) {
            return Integer.parseInt(operand);
        } else if (operand.matches("[0-9A-Fa-f]+")) {
            return Integer.parseInt(operand, 16);
        }
        String errorMsg = "Formato inválido de endereço: " + operand;
        SimulatorLogger.logError(errorMsg, null);
        throw new IllegalArgumentException(errorMsg);
    }

    private int getInstructionSize(String mnemonic, String operand) {
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return 1;
        }
        if (mnemonic.equalsIgnoreCase("RESW") || mnemonic.equalsIgnoreCase("RESB")) {
            if (operand == null) {
                String errorMsg = "Operando ausente para a diretiva " + mnemonic;
                SimulatorLogger.logError(errorMsg, null);
                throw new IllegalArgumentException(errorMsg);
            }
            return mnemonic.equalsIgnoreCase("RESW") ? Integer.parseInt(operand) : (Integer.parseInt(operand) + 2) / 3;
        }
        if (mnemonic.equalsIgnoreCase("BYTE")) {
            if (operand == null) {
                String errorMsg = "Operando ausente para BYTE.";
                SimulatorLogger.logError(errorMsg, null);
                throw new IllegalArgumentException(errorMsg);
            }
            return operand.startsWith("C'") ? (operand.length() - 3 + 2) / 3 : operand.startsWith("X'") ? (operand.length() - 3 + 1) / 2 : 1;
        }
        return 1;
    }
}
