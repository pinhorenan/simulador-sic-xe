package sicxesimulator.loader;

import sicxesimulator.machine.Memory;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.models.RelocationRecord;
import sicxesimulator.models.SymbolTable;

public class Loader {

    /**
     * Carrega o executável final na memória, a partir de finalObj.
     * Se finalObj.isRelocated()==false, aplica relocationRecords somando baseAddress.
     */
    public void loadObjectFile(ObjectFile finalObj, Memory memory, int baseAddress) {
        byte[] code = finalObj.getObjectCode();
        int size = code.length;
        if (baseAddress + size > memory.getSize()) {
            throw new IllegalArgumentException("Não cabe na memória (base + code.length > memorySize).");
        }

        // Copia bytes
        for (int i = 0; i < size; i++) {
            memory.writeByte(baseAddress + i, code[i] & 0xFF);
        }

        // Se já está relocado, nada a fazer
        if (finalObj.getIsRelocated()) {
            return;
        }

        // Senão, aplicamos os relocationRecords:
        if (finalObj.getRelocationRecords() != null) {
            for (RelocationRecord rec : finalObj.getRelocationRecords()) {
                applyRelocationInMemory(memory, baseAddress, rec);
            }
        }

        // Ajusta SymbolTable (se quiser referenciar em tempo real)
        SymbolTable symTab = finalObj.getSymbolTable();
        for (var e : symTab.getAllSymbols().entrySet()) {
            SymbolTable.SymbolInfo info = e.getValue();
            info.address += baseAddress;
        }

        finalObj.setRelocated(true);
    }

    // Aplica a soma baseAddress ao campo de `length` bytes no offset rec.offset()
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

        // Se for PC-relativo, depende do seu design, ex: newVal -= 3;
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
