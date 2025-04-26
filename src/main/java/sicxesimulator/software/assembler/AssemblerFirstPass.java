package sicxesimulator.software.assembler;

import sicxesimulator.software.assembler.data.AssemblyLine;
import sicxesimulator.software.assembler.data.IntermediateRepresentation;
import sicxesimulator.software.assembler.util.Parser;
import sicxesimulator.software.assembler.util.InstructionSizeCalculator;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.common.utils.Checker;

import java.util.*;

/**
 * Responsável por realizar a primeira passagem do montador SIC/XE.
 *
 * Interpreta o código-fonte assembly e constrói uma representação intermediária com as
 * instruções, símbolos e endereços necessários para a segunda passagem do montador.
 */
public class AssemblerFirstPass {

    /**
     * Processa o código-fonte e gera a representação intermediária necessária
     * para a montagem do programa.
     *
     * @param originalSourceLines Linhas originais do código-fonte assembly.
     * @param sourceLines Linhas processadas (com macros expandidas, sem comentários e espaços irrelevantes).
     * @return {@link IntermediateRepresentation} contendo as instruções, tabela de símbolos, programa e endereço inicial.
     */
    public IntermediateRepresentation process(List<String> originalSourceLines, List<String> sourceLines) {
        int locationCounter = 0;
        boolean endFound = false;
        String programName = null;
        int startAddress = 0;

        List<AssemblyLine> assemblyLines = new ArrayList<>();
        var symbolTable = new SymbolTable();
        Set<String> importedSymbols = new HashSet<>();

        for (int i = 0; i < sourceLines.size(); i++) {
            String originalLine = sourceLines.get(i);
            String line = removeInlineComments(originalLine).trim();

            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }

            String[] parts = line.split("\\s+", 3);
            String label = null;
            String mnemonic = null;
            String operand = null;

            if (parts.length > 0) {
                if (Checker.isMnemonic(parts[0])) {
                    mnemonic = parts[0];
                    if (parts.length > 1) {
                        operand = parts[1];
                    }
                } else if (parts.length > 1 && Checker.isMnemonic(parts[1])) {
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

            if (processDirectives(mnemonic, line, symbolTable, importedSymbols)) {
                if (mnemonic.equalsIgnoreCase("START")) {
                    try {
                        startAddress = Parser.parseAddress(operand);
                        locationCounter = startAddress;
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Erro ao processar START na linha " + (i + 1) + ": " + operand, e);
                    }
                    if (label != null) {
                        programName = label;
                    }
                    continue;
                }
                if (mnemonic.equalsIgnoreCase("END")) {
                    endFound = true;
                }
                continue;
            }

            if (label != null) {
                if (symbolTable.contains(label)) {
                    var symbol = symbolTable.getSymbolInfo(label);
                    symbol.address = locationCounter;
                    symbol.isPublic = true;
                } else {
                    symbolTable.addSymbol(label, locationCounter, false);
                }
            }

            int size = InstructionSizeCalculator.calculateSize(mnemonic, operand);
            AssemblyLine asmLine = new AssemblyLine(label, mnemonic, operand, locationCounter);
            assemblyLines.add(asmLine);
            locationCounter += size;
        }

        if (!endFound) {
            throw new IllegalArgumentException("Diretiva END não encontrada.");
        }

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
     * Remove comentários inline iniciados por ponto e vírgula (;).
     *
     * @param line Linha original do código.
     * @return Linha sem o conteúdo de comentário.
     */
    private String removeInlineComments(String line) {
        int commentIndex = line.indexOf(";");
        return (commentIndex != -1) ? line.substring(0, commentIndex) : line;
    }

    /**
     * Processa diretivas especiais do montador: EXTDEF, EXTREF, START e END.
     *
     * As diretivas START e END são tratadas em lógica separada dentro do métodO {@link #process}.
     *
     * @param mnemonic Mnemônico da linha.
     * @param line Linha completa do código.
     * @param symbolTable Tabela de símbolos a ser atualizada.
     * @param importedSymbols Conjunto de símbolos importados do programa.
     * @return {@code true} se a diretiva foi reconhecida e processada; {@code false} caso contrário.
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

        return mnemonic.equalsIgnoreCase("START") || mnemonic.equalsIgnoreCase("END");
    }
}
