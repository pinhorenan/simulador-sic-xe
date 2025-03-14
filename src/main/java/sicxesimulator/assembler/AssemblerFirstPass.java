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

            line = line.trim();
            // Remover comentários inline (delimitador ';')
            int commentIndex = line.indexOf(";");
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex).trim();
            }
            // Ignora linhas vazias ou linhas que são apenas comentários (iniciadas por ".")
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
                throw new IllegalArgumentException("Linha invalida na linha " + lineNumber + ": " + line);
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

            // Se a diretiva EXTDEF for encontrada, os símbolos são exportados.
            if (mnemonic.equalsIgnoreCase("EXTDEF")) {
                // Extrai totalmente o texto após o mnemônico, para suportar múltiplos símbolos separados por vírgula.
                String operandFull = line.substring(mnemonic.length()).trim();
                if (!operandFull.isEmpty()) {
                    String[] symbols = operandFull.split(",");
                    for (String symbol : symbols) {
                        symbol = symbol.trim().toUpperCase(); // Força para maiúsculas para consistência.
                        midCode.addExportedSymbol(symbol);
                        // Adiciona na symbolTable com endereço (a ser resolvido posteriormente) e atributo isPublic=true.
                        midCode.getSymbolTable().addSymbol(symbol, locationCounter, true);
                    }
                }
                continue;
            }


            // Se a diretiva EXTREF for encontrada, os símbolos são importados.
            if (mnemonic.equalsIgnoreCase("EXTREF")) {
                // Em vez de usar o 'operand' (limitado pelo split com limite 3), extrai totalmente o texto após o mnemônico.
                String operandFull = line.substring(mnemonic.length()).trim();
                if (!operandFull.isEmpty()) {
                    String[] symbols = operandFull.split(",");
                    for (String symbol : symbols) {
                        symbol = symbol.trim().toUpperCase();
                        midCode.addImportedSymbol(symbol);
                        // Adiciona na symbolTable com endereço 0, "isPublic = false"
                        midCode.getSymbolTable().addSymbol(symbol, 0, false);
                    }
                }
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
