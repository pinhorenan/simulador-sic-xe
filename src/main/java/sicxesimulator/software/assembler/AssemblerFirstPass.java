package sicxesimulator.software.assembler;

import sicxesimulator.common.utils.Checker;
import sicxesimulator.software.data.AssemblyLine;
import sicxesimulator.software.data.IntermediateRepresentation;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.software.util.InstructionSizeCalculator;
import sicxesimulator.software.util.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Realiza a primeira passagem (passo de montagem) do programa SIC/XE.
 * <p>
 * Interpreta o código-fonte assembly, processa diretivas, gera a tabela de símbolos
 * e calcula endereços, produzindo uma representação intermediária para a segunda passagem.
 */
public class AssemblerFirstPass {

    /**
     * Processa linhas de código-fonte para montar a representação intermediária.
     *
     * @param originalSourceLines Linhas originais do código assembly (para referência).
     * @param preprocessedLines   Linhas após expansão de macros e remoção de comentários.
     * @return {@link IntermediateRepresentation} contendo linhas e símbolos para montagem.
     * @throws IllegalArgumentException se faltar diretiva END, ou linhas nulas.
     */
    public IntermediateRepresentation process(List<String> originalSourceLines, List<String> preprocessedLines) {
        Objects.requireNonNull(originalSourceLines, "originalSourceLines não pode ser nulo");
        Objects.requireNonNull(preprocessedLines, "preprocessedLines não pode ser nulo");

        int locationCounter = 0;
        boolean endFound = false;
        String programName = null;
        int startAddress = 0;

        List<AssemblyLine> assemblyLines = new ArrayList<>();
        SymbolTable symbolTable = new SymbolTable();
        Set<String> importedSymbols = new HashSet<>();

        for (int i = 0; i < preprocessedLines.size(); i++) {
            String rawLine = preprocessedLines.get(i);
            String line = removeInlineComments(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+", 3);
            String label = null;
            String mnemonic;
            String operand = null;

            // Extração de label, mnemônico e operando
            if (Checker.isMnemonic(parts[0])) {
                mnemonic = parts[0];
                if (parts.length > 1) operand = parts[1];
            } else if (parts.length > 1 && Checker.isMnemonic(parts[1])) {
                label = parts[0];
                mnemonic = parts[1];
                if (parts.length > 2) operand = parts[2];
            } else {
                throw new IllegalArgumentException(
                        String.format("Linha inválida [%d]: %s", i + 1, rawLine)
                );
            }

            // Diretivas especiais
            if (handleDirective(mnemonic, line, symbolTable, importedSymbols)) {
                if (mnemonic.equalsIgnoreCase("START")) {
                    startAddress = Parser.parseAddress(operand);
                    locationCounter = startAddress;
                    programName = label;
                } else if (mnemonic.equalsIgnoreCase("END")) {
                    endFound = true;
                }
                continue;
            }

            // Definição de símbolo e label
            if (label != null) {
                if (symbolTable.contains(label)) {
                    symbolTable.getSymbolInfo(label).address = locationCounter;
                    symbolTable.getSymbolInfo(label).isPublic = true;
                } else {
                    symbolTable.addSymbol(label, locationCounter, false);
                }
            }

            // Montagem de linha
            int size = InstructionSizeCalculator.calculateSize(mnemonic, operand);
            assemblyLines.add(new AssemblyLine(label, mnemonic, operand, locationCounter));
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
     * Remove comentários inline iniciados por ';'.
     *
     * @param line Linha que pode conter comentário.
     * @return Conteúdo antes do comentário.
     */
    private String removeInlineComments(String line) {
        int idx = line.indexOf(';');
        return (idx >= 0) ? line.substring(0, idx) : line;
    }

    /**
     * Processa diretivas EXTDEF, EXTREF, START e END.
     *
     * @param mnemonic        Mnemônico da diretiva.
     * @param fullLine        Linha completa sem remover mnemônico.
     * @param symbolTable     Tabela de símbolos para registrar definições.
     * @param importedSymbols Conjunto para coletar símbolos externamente referenciados.
     * @return true se a linha for diretiva reconhecida; false caso contrário.
     */
    private boolean handleDirective(String mnemonic, String fullLine, SymbolTable symbolTable, Set<String> importedSymbols) {
        String operands = fullLine.substring(mnemonic.length()).trim();
        switch (mnemonic.toUpperCase()) {
            case "EXTDEF" -> {
                for (String sym: operands.split(",")) {
                    symbolTable.addSymbol(sym.trim().toUpperCase(), 0, true);
                }
                return true;
            }
            case "EXTREF" -> {
                for (String sym : operands.split(",")) {
                    String s = sym.trim().toUpperCase();
                    importedSymbols.add(s);
                    symbolTable.addSymbol(s, 0, false);
                }
                return true;
            }
            case "START", "END" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
