package sicxesimulator.software.loader;

import sicxesimulator.hardware.Memory;
import sicxesimulator.data.ObjectFile;
import sicxesimulator.data.records.RelocationRecord;
import sicxesimulator.data.Symbol;
import sicxesimulator.data.SymbolTable;

/**
 * Carregador de módulos objeto na memória.
 * Agora, se o objeto NÃO estiver fullyRelocated,
 * este Loader aplica as relocações.
 */
public class Loader {

    /**
     * Carrega um módulo objeto na memória, aplicando relocação se necessário.
     * @param finalObject Módulo objeto a ser carregado.
     * @param memory      Memória onde o módulo será carregado.
     * @param baseAddress Endereço base onde o módulo será carregado.
     */
    public void loadObjectFile(
            ObjectFile finalObject,
            Memory memory,
            int baseAddress
    ) {
        byte[] code = finalObject.getObjectCode();
        int codeLength = code.length;

        // Verifica se cabe na memória
        if (!fitsInMemory(memory, baseAddress, codeLength)) {
            throw new IllegalArgumentException(
                    "Não cabe na memória (base + code.length > memorySize)."
            );
        }

        // Copia o código para memória
        copyCodeToMemory(memory, baseAddress, code);

        // Se o objeto não está "fullyRelocated", então precisamos aplicar
        // as relocations agora
        if (!finalObject.isFullyRelocated()) {
            if (finalObject.getRelocationRecords() != null) {
                applyRelocations(memory, baseAddress, finalObject.getRelocationRecords());
            }
            // E atualiza a SymbolTable, pois os símbolos agora ficam no baseAddress
            updateSymbolTableAddresses(finalObject.getSymbolTable(), baseAddress);

            // Marcamos que agora está realocado
            finalObject.setFullyRelocated(true);
        }
    }

    private boolean fitsInMemory(Memory memory, int baseAddress, int codeLength) {
        return baseAddress + codeLength <= memory.getSize();
    }

    private void copyCodeToMemory(Memory memory, int baseAddress, byte[] code) {
        for (int i = 0; i < code.length; i++) {
            memory.writeByte(baseAddress + i, code[i] & 0xFF);
        }
    }

    /**
     * Aplica todos os relocation records no código já carregado na memória.
     */
    private void applyRelocations(
            Memory memory,
            int baseAddress,
            Iterable<RelocationRecord> relocations
    ) {
        for (RelocationRecord rec : relocations) {
            applyRelocationInMemory(memory, baseAddress, rec);
        }
    }

    /**
     * Ajusta a tabela de símbolos para refletir o baseAddress atual.
     */
    private void updateSymbolTableAddresses(SymbolTable symbolTable, int baseAddress) {
        for (var entry : symbolTable.getAllSymbols().entrySet()) {
            Symbol symbol = entry.getValue();
            symbol.address += baseAddress;
        }
    }

    /**
     * Aplica uma relocação no código objeto carregado.
     * Observação: aqui, assumimos que se pcRelative() == true, subtrai-se 3 (Formato 3).
     * Se houver Formato 4, precisaria ajustar para subtrair 4 nesses casos.
     */
    private void applyRelocationInMemory(
            Memory memory,
            int baseAddress,
            RelocationRecord rec
    ) {
        int offset = rec.offset();
        int length = rec.length();

        // Lê o valor atual
        int val = 0;
        for (int i = 0; i < length; i++) {
            val = (val << 8) | (memory.readByte(offset + i) & 0xFF);
        }

        // Soma o baseAddress
        int newVal = val + baseAddress;

        // Se for PC-relativo, subtrai 3. (Ajuste para Formato 3)
        if (rec.pcRelative()) {
            newVal -= 3;
        }

        // Grava de volta
        for (int i = length - 1; i >= 0; i--) {
            memory.writeByte(offset + i, (newVal & 0xFF));
            newVal >>>= 8;
        }
    }
}
