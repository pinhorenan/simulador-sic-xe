package sicxesimulator.assembler;

import sicxesimulator.assembler.records.AssemblyLine;
import sicxesimulator.assembler.records.IntermediateRepresentation;
import sicxesimulator.assembler.util.Parser;
import sicxesimulator.assembler.util.InstructionSizeCalculator;
import sicxesimulator.models.SymbolTable;
import sicxesimulator.utils.Check;

import java.util.*;

/**
 * Classe que realiza a primeira passagem do montador, gerando uma representação intermediária.
 */
class AssemblerFirstPass {

    /**
     * Processa as linhas de código-fonte e gera uma IntermediateRepresentation.
     *
     * @param sourceLines Lista de linhas de código assembly.
     * @return Representação intermediária contendo linhas de assembly, símbolos e endereços.
     */
    protected IntermediateRepresentation process(List<String> originalSourceLines, List<String> sourceLines) {
        int locationCounter = 0;
        boolean endFound = false;
        String programName = null;
        int startAddress = 0;

        // Coleções para acumular os dados
        List<AssemblyLine> assemblyLines = new ArrayList<>();
        var symbolTable = new SymbolTable();
        Set<String> importedSymbols = new HashSet<>();

        // Percorre cada linha do código-fonte
        for (int i = 0; i < sourceLines.size(); i++) {
            String originalLine = sourceLines.get(i);
            String line = removeInlineComments(originalLine).trim();

            // Ignorar linhas vazias ou comentadas
            if (originalLine.isEmpty() || originalLine.startsWith(";")) {
                continue;
            }

            // Dividir a linha em partes
            String[] parts = originalLine.split("\\s+", 3);
            String label = null;
            String mnemonic = null;
            String operand = null;

            // Detecta o mnemônico e, possivelmente, o rótulo
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
                throw new IllegalArgumentException("Linha invalida na linha " + (i + 1) + ": " + originalLine);
            }

            // Processa diretivas especiais
            if(processDirectives(mnemonic, line, symbolTable, importedSymbols)) {
                // Diretivas processadas (START, EXTDEF, EXTREF, END) não geram AssemblyLine
                if (mnemonic.equalsIgnoreCase("START")) {
                    try {
                        startAddress = Parser.parseAddress(operand);
                        locationCounter = startAddress;
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Erro ao processar START na linha " + (i + 1) + ": " + operand, e);
                    }
                    if (label != null) {
                        // Registra o símbolo local e define o nome do programa
                        symbolTable.addSymbol(label, locationCounter, true);
                        programName = label;
                    }
                    continue;
                }
                if (mnemonic.equalsIgnoreCase("END")) {
                    endFound = true;
                }
                continue;
            }

            // Registra o rótulo (se houver) na tabela de símbolos
            if (label != null) {
                if (symbolTable.contains(label)) {
                    var symbol = symbolTable.getSymbolInfo(label);
                    symbol.address = locationCounter;
                    symbol.isPublic = true;
                } else {
                    symbolTable.addSymbol(label, locationCounter, false);
                }
            }

            // Calcula o tamanho da instrução e registra a AssemblyLine
            int size = InstructionSizeCalculator.calculateSize(mnemonic, operand);
            AssemblyLine asmLine = new AssemblyLine(label, mnemonic, operand, locationCounter);
            assemblyLines.add(asmLine);
            locationCounter += size;
        }

        if (!endFound) {
            throw new IllegalArgumentException("Diretiva END nao encontrada.");
        }

        // Cria a IntermediateRepresentation utilizando coleções imutáveis
        return new IntermediateRepresentation(
                Collections.unmodifiableList(assemblyLines),
                Collections.unmodifiableList(originalSourceLines),
                symbolTable,
                Collections.unmodifiableSet(importedSymbols),
                programName,
                startAddress
        );
    }

    /**
     * Remove comentários inline delimitados por ";".
     * @param line Linha original.
     * @return Linha sem o conteúdo de comentário.
     */
    private String removeInlineComments(String line) {
        int commentIndex = line.indexOf(";");
        return (commentIndex != -1) ? line.substring(0, commentIndex) : line;
    }

    /**
     * Processa diretivas especiais (START, EXTDEF, EXTREF).
     *
     * @param mnemonic       Mnemônico da linha.
     * @param line           Linha completa.
     * @param symbolTable    Tabela de símbolos a ser atualizada.
     * @param importedSymbols Conjunto de símbolos importados.
     * @return true se a diretiva foi processada; false caso contrário.
     */
    private boolean processDirectives(String mnemonic, String line, SymbolTable symbolTable, Set<String> importedSymbols) {
        String operandFull = line.substring(mnemonic.length()).trim();

        if (mnemonic.equalsIgnoreCase("EXTDEF")) {
            if (!operandFull.isEmpty()) {
                String[] symbols = operandFull.split(",");
                for (String symbol : symbols) {
                    symbolTable.addSymbol(symbol.trim().toUpperCase(), 0, true);
                }
            }
            return true;
        }

        if (mnemonic.equalsIgnoreCase("EXTREF")) {
            if (!operandFull.isEmpty()) {
                String[] symbols = operandFull.split(",");
                for (String symbol : symbols) {
                    String sym = symbol.trim().toUpperCase();
                    importedSymbols.add(sym);
                    symbolTable.addSymbol(sym, 0, false);
                }
            }
            return true;
        }

        // START e END são tratados separadamente em process()
        return mnemonic.equalsIgnoreCase("START") || mnemonic.equalsIgnoreCase("END");
    }
}
