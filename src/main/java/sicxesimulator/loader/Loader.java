package sicxesimulator.loader;

import sicxesimulator.machine.Memory;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.models.RelocationRecord;
import sicxesimulator.models.Symbol;
import sicxesimulator.models.SymbolTable;

/**
 * Carregador de módulos objeto na memória.
 */
public class Loader {

    /**
     * Carrega um módulo objeto na memória, aplicando relocação se necessário.
     * @param finalObject Módulo objeto a ser carregado
     * @param memory Memória onde o módulo será carregado
     * @param baseAddress Endereço base onde o módulo será carregado
     */
    public void loadObjectFile(ObjectFile finalObject, Memory memory, int baseAddress) {
        byte[] code = finalObject.getObjectCode();
        int size = code.length;
        if (baseAddress + size > memory.getSize()) {
            throw new IllegalArgumentException("Não cabe na memória (base + code.length > memorySize).");
        }

        // Copia bytes
        for (int i = 0; i < size; i++) {
            memory.writeByte(baseAddress + i, code[i] & 0xFF);
        }

        // Se já está relocado, nada a fazer
        if (finalObject.isFullyRelocated()) {
            return;
        }

        // Senão, aplicamos os relocationRecords:
        if (finalObject.getRelocationRecords() != null) {
            for (RelocationRecord rec : finalObject.getRelocationRecords()) {
                applyRelocationInMemory(memory, baseAddress, rec);
            }
        }

        // Ajusta SymbolTable
        SymbolTable symbolTable = finalObject.getSymbolTable();
        for (var e : symbolTable.getAllSymbols().entrySet()) {
            Symbol symbol = e.getValue();
            symbol.address += baseAddress;
        }

        finalObject.setFullyRelocated(true);
    }

    /**
     * Aplica uma relocação em um módulo carregado na memória.
     * @param memory Memória onde o módulo foi carregado
     * @param baseAddress Endereço base onde o módulo foi carregado
     * @param rec Registro de relocação a ser aplicado
     */
    private void applyRelocationInMemory(Memory memory, int baseAddress, RelocationRecord rec) {
        int offset = rec.offset();
        int length = rec.length();

        // Ler valor atual
        int val = 0;
        for (int i = 0; i < length; i++) {
            val = (val << 8) | (memory.readByte(offset + i) & 0xFF);
        }

        // Modelo simples => soma base
        int newVal = val + baseAddress;

        // Se for PC-relativo, subtrai 3
        if (rec.pcRelative()) {
            newVal -= 3;
        }

        // Escreve de volta
        for (int i = length - 1; i >= 0; i--) {
            memory.writeByte(offset + i, (newVal & 0xFF));
            newVal >>>= 8;
        }
    }
}
