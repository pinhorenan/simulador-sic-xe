package sicxesimulator.software.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa a tabela de símbolos de um módulo ou do programa final,
 * permitindo indicar se cada símbolo é público (exportado) ou local (privado).
 */
public class SymbolTable implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    /// ===== Métodos Getters =====

    public Symbol getSymbolInfo(String name) {
        return symbols.get(name);
    }

    public Integer getSymbolAddress(String symbolName) {
        Symbol symbol = symbols.get(symbolName);
        return (symbol != null) ? symbol.address : null;
    }

    public Map<String, Symbol> getAllSymbols() {
        return symbols;
    }

    /**
     * Adiciona um símbolo à tabela de símbolos.
     * @param name Nome do símbolo.
     * @param address Endereço do símbolo.
     * @param isPublic Verdadeiro se o símbolo é público (exportado).
     */
    public void addSymbol(String name, int address, boolean isPublic) {
        symbols.put(name, new Symbol(name, address, isPublic));
    }

    /**
     * Verifica se a tabela de símbolos contém um símbolo com o nome especificado.
     * @param name Nome do símbolo a ser verificado.
     * @return Verdadeiro se o símbolo estiver presente na tabela.
     */
    public boolean contains(String name) {
        return symbols.containsKey(name);
    }

    @Override
    public String toString() {
        return symbols.toString();
    }
}
