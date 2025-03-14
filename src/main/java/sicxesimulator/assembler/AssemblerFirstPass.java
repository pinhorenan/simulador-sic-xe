package sicxesimulator.assembler;

import sicxesimulator.utils.SimulatorLogger;
import sicxesimulator.models.AssemblyLine;
import sicxesimulator.models.IntermediateRepresentation;
import sicxesimulator.utils.Check;

import java.util.List;

class AssemblerFirstPass {
    private int locationCounter = 0;

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

            // Se a linha contém um mnemônico, ele é o primeiro elemento.
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

            // Se não encontrou um mnemônico, a linha é inválida.
            if (mnemonic == null) {
                throw new IllegalArgumentException("Linha inválida na linha " + lineNumber + ": " + line);
            }

            // Se a diretiva START for encontrada, o endereço de início é definido.
            if (mnemonic.equalsIgnoreCase("START")) {
                try {
                    int startAddress = parseAddress(operand);
                    locationCounter = startAddress;
                    midCode.setStartAddress(startAddress);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Erro ao processar START na linha " + lineNumber + ": " + operand, e);
                }
                if (label != null) {
                    midCode.addLocalSymbol(label, locationCounter);
                    midCode.setProgramName(label);
                }
                continue;
            }

            // TODO: Verificar se um símbolo marcado em EXTDEF realmente existe como label, caso contrário, gerar aviso.
            // Se a diretiva EXTDEF for encontrada, os símbolos são exportados.
            if (mnemonic.equalsIgnoreCase("EXTDEF")) {
                // Se houver operand, cada símbolo está separado por vírgula
                if (operand != null && !operand.isEmpty()) {
                    String[] symbols = operand.split(",");
                    for (String symbol : symbols) {
                        symbol = symbol.trim();

                        // Adicionamos o símbolo à tabela de símbolos com o atributo isPublic=true.
                        midCode.addExportedSymbol(symbol);
                    }
                }
                // Não soma locationCounter, e continua para a próxima linha
                continue;
            }

            // TODO: Impedir rótulos em linhas de EXTDEF/EXTREF (ajuste "opcional")
            // Se a diretiva EXTREF for encontrada, os símbolos são importados.
            if (mnemonic.equalsIgnoreCase("EXTREF")) {
                if (operand != null && !operand.isEmpty()) {
                    String[] symbols = operand.split(",");
                    for (String symbol : symbols) {
                        symbol = symbol.trim();
                        midCode.addImportedSymbol(symbol);
                    }
                }
                // Não soma locationCounter, e continua para a próxima linha
                continue;
            }

            // Se a diretiva END for encontrada, a montagem é encerrada.
            if (mnemonic.equalsIgnoreCase("END")) {
                endFound = true;
                continue;
            }

            // Se a linha contém um label, ele é adicionado à tabela de símbolos.
            if (label != null) {
                midCode.addLocalSymbol(label, locationCounter);
            }

            int size = InstructionSizeCalculator.calculateSize(mnemonic, operand);
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
}
