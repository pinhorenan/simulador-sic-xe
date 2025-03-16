package sicxesimulator.software.loader;

import sicxesimulator.hardware.Memory;
import sicxesimulator.data.ObjectFile;
import sicxesimulator.data.records.RelocationRecord;
import sicxesimulator.data.Symbol;
import sicxesimulator.data.SymbolTable;

/**
 * Carregador de módulos objeto na memória.
 * Realiza o carregamento do código objeto em um endereço base na memória e aplica as relocações se necessário.
 */
public class Loader {

    /**
     * Carrega um módulo objeto na memória, aplicando relocação se necessário.
     * @param finalObject Módulo objeto a ser carregado.
     * @param memory      Memória onde o módulo será carregado.
     * @param baseAddress Endereço base onde o módulo será carregado.
     * @throws IllegalArgumentException se o código não couber na memória.
     */
    public void loadObjectFile(ObjectFile finalObject, Memory memory, int baseAddress) {
        byte[] code = finalObject.getObjectCode();
        int codeLength = code.length;

        // Verifica se o módulo cabe na memória a partir do endereço base
        if (!fitsInMemory(memory, baseAddress, codeLength)) {
            throw new IllegalArgumentException("Não cabe na memória (base + code.length > memorySize).");
        }

        // Copia o código objeto para a memória
        copyCodeToMemory(memory, baseAddress, code);

        // Se o objeto já estiver totalmente relocado, não é necessário aplicar relocação
        if (finalObject.isFullyRelocated()) {
            // Aplica os relocation records, se existirem
            if (finalObject.getRelocationRecords() != null) {
                applyRelocations(memory, baseAddress, finalObject.getRelocationRecords());
            }
            // Atualiza os endereços na SymbolTable do objeto
            updateSymbolTableAddresses(finalObject.getSymbolTable(), baseAddress);
            finalObject.setFullyRelocated(true);
        }
    }

    /**
     * Verifica se o código cabe na memória a partir do endereço base.
     * @param memory      Memória de destino.
     * @param baseAddress Endereço base.
     * @param codeLength  Tamanho do código objeto.
     * @return true se baseAddress + codeLength for menor ou igual ao tamanho da memória.
     */
    private boolean fitsInMemory(Memory memory, int baseAddress, int codeLength) {
        return baseAddress + codeLength <= memory.getSize();
    }

    /**
     * Copia o array de bytes do código objeto para a memória, a partir do endereço base.
     * @param memory      Memória de destino.
     * @param baseAddress Endereço base onde começar a copiar.
     * @param code        Código objeto.
     */
    private void copyCodeToMemory(Memory memory, int baseAddress, byte[] code) {
        for (int i = 0; i < code.length; i++) {
            memory.writeByte(baseAddress + i, code[i] & 0xFF);
        }
    }

    /**
     * Aplica todos os relocation records no código já carregado na memória.
     * @param memory          Memória onde o código foi carregado.
     * @param baseAddress     Endereço base onde o código foi carregado.
     * @param relocations Lista de relocation records a serem aplicados.
     */
    private void applyRelocations(Memory memory, int baseAddress, Iterable<RelocationRecord> relocations) {
        for (RelocationRecord rec : relocations) {
            applyRelocationInMemory(memory, baseAddress, rec);
        }
    }

    /**
     * Atualiza os endereços dos símbolos da tabela, adicionando o endereço base.
     * @param symbolTable Tabela de símbolos do objeto.
     * @param baseAddress Endereço base onde o objeto foi carregado.
     */
    private void updateSymbolTableAddresses(SymbolTable symbolTable, int baseAddress) {
        for (var entry : symbolTable.getAllSymbols().entrySet()) {
            Symbol symbol = entry.getValue();
            symbol.address += baseAddress;
        }
    }

    /**
     * Aplica uma relocação no código objeto carregado na memória.
     * Lê o valor atual na memória, soma o endereço base e, se for PC-relativo, subtrai 3,
     * em seguida, escreve o novo valor de volta.
     * @param memory      Memória onde o módulo foi carregado.
     * @param baseAddress Endereço base onde o módulo foi carregado.
     * @param rec         Registro de relocação a ser aplicado.
     * @throws IllegalArgumentException se o relocation record ultrapassar os limites do código.
     */
    private void applyRelocationInMemory(Memory memory, int baseAddress, RelocationRecord rec) {
        int offset = rec.offset();
        int length = rec.length();

        // Lê o valor atual a partir da memória
        int val = 0;
        for (int i = 0; i < length; i++) {
            val = (val << 8) | (memory.readByte(offset + i) & 0xFF);
        }

        // Aplica a correção: soma o baseAddress
        int newVal = val + baseAddress;

        // Se for PC-relativo, subtrai 3 (ajuste conforme o modelo SIC/XE)
        if (rec.pcRelative()) {
            newVal -= 3;
        }

        // Escreve o novo valor de volta na memória
        for (int i = length - 1; i >= 0; i--) {
            memory.writeByte(offset + i, (newVal & 0xFF));
            newVal >>>= 8;
        }
    }
}
