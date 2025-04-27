package sicxesimulator.software.loader;

import sicxesimulator.hardware.memory.Memory;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.RelocationRecord;
import sicxesimulator.software.data.Symbol;
import sicxesimulator.software.data.SymbolTable;

import java.util.Objects;

/**
 * Carrega um {@link ObjectFile} na memória principal,
 * copiando o código e aplicando relocação (se ainda pendente).
 *
 * <p>Fluxo geral:</p>
 * <ol>
 *   <li>Determina o endereço de carga ({@code effectiveLoadAddress}).<br>
 *       – Se o objeto já estiver marcado como <i>fullyRelocated</i>, usa {@link ObjectFile#getStartAddress()};<br>
 *       – Caso contrário, utiliza o {@code baseAddress} informado.</li>
 *   <li>Verifica se o código cabe na memória.</li>
 *   <li>Copia os bytes para a memória.</li>
 *   <li>Se ainda não realocado:<br>
 *       4.1. Ajusta endereços da {@link SymbolTable};<br>
 *       4.2. Aplica todos os {@link RelocationRecord}s;<br>
 *       4.3. Marca o objeto como realocado.</li>
 * </ol>
 */
public class Loader {

    /**
     * Carrega o objeto na memória.
     *
     * @param obj          objeto já linkado
     * @param memory       memória principal
     * @param baseAddress  endereço base desejado (usado somente se {@code obj} não estiver realocado)
     * @throws IllegalArgumentException se o código não cabe na memória
     * @throws NullPointerException     se {@code obj} ou {@code memory} forem nulos
     */
    public void loadObjectFile(ObjectFile obj, Memory memory, int baseAddress) {
        Objects.requireNonNull(obj,    "objectFile não pode ser nulo");
        Objects.requireNonNull(memory, "memory não pode ser nulo");

        final int effectiveLoadAddress = obj.isFullyRelocated()
                ? obj.getStartAddress()
                : baseAddress;

        byte[] code = obj.getObjectCode();
        if (effectiveLoadAddress + code.length > memory.getSize()) {
            throw new IllegalArgumentException("Programa não cabe na memória.");
        }

        copyCodeToMemory(memory, effectiveLoadAddress, code);

        if (!obj.isFullyRelocated()) {
            updateSymbolTable(obj.getSymbolTable(), effectiveLoadAddress);
            applyRelocations(memory, effectiveLoadAddress, obj);
            obj.setFullyRelocated(true);
        }
    }

    /* -------------------------------------------------------------------------- */
    /*  Helpers                                                                   */
    /* -------------------------------------------------------------------------- */

    /** Copia todo o array de bytes para a memória. */
    private void copyCodeToMemory(Memory memory, int loadAddr, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            memory.writeByte(loadAddr + i, bytes[i] & 0xFF);
        }
    }

    /** Soma {@code loadAddr} a todos os símbolos da tabela. */
    private void updateSymbolTable(SymbolTable table, int loadAddr) {
        for (Symbol s : table.getAllSymbols().values()) {
            s.address += loadAddr;
        }
    }

    /** Aplica todos os {@link RelocationRecord}s pendentes. */
    private void applyRelocations(Memory memory, int loadAddr, ObjectFile obj) {
        SymbolTable symTab = obj.getSymbolTable();
        for (RelocationRecord rec : obj.getRelocationRecords()) {
            relocateField(memory, loadAddr, rec, symTab);
        }
    }

    /** Aplica uma única entrada de relocação na memória. */
    private void relocateField(Memory memory,
                               int loadAddr,
                               RelocationRecord rec,
                               SymbolTable symTab) {
        int offset = rec.offset();
        int len    = rec.length();

        /* lê valor atual */
        int value = 0;
        for (int i = 0; i < len; i++) {
            value = (value << 8) | (memory.readByte(loadAddr + offset + i) & 0xFF);
        }

        Integer symAddr = symTab.getSymbolAddress(rec.symbol());
        if (symAddr == null) throw new IllegalStateException("Símbolo não encontrado: " + rec.symbol());

        int newVal = value + symAddr - (rec.pcRelative() ? 3 : 0);

        /* grava de volta (big-endian) */
        for (int i = len - 1; i >= 0; i--) {
            memory.writeByte(loadAddr + offset + i, newVal & 0xFF);
            newVal >>>= 8;
        }
    }
}
