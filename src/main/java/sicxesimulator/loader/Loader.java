package sicxesimulator.loader;

import sicxesimulator.machine.Memory;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.models.RelocationRecord;
import sicxesimulator.models.SymbolTable;

public class Loader {

    /**
     * Carrega o executável final na memória.
     * Se o executável não tiver sido relocado (finalObj.isRelocated() == false),
     * este Loader aplicará as relocationRecords, somando baseAddress (modelo de "Carregador Relocador").
     *
     * @param finalObj   O objeto resultante do Linker (executável final ou parcial).
     * @param memory     Uma instância da classe Memory do seu simulador.
     * @param baseAddress Onde deve ser carregado (em bytes) na memória do simulador.
     * @throws IllegalArgumentException se exceder os limites de memória ou se existirem relocationRecords que não podem ser aplicados.
     */
    public void loadObjectFile(ObjectFile finalObj, Memory memory, int baseAddress) {
        byte[] code = finalObj.getObjectCode();

        // 1. Verifica se cabe na memória
        if (baseAddress + code.length > memory.getSize()) {
            throw new IllegalArgumentException("Memória insuficiente para carregar o programa (baseAddress + code.length > memorySize).");
        }

        // 2. Copia o machineCode para a memória, byte a byte
        for (int i = 0; i < code.length; i++) {
            memory.writeByte(baseAddress + i, code[i] & 0xFF);
        }

        // 3. Verifica se o arquivo já está relocado
        if (finalObj.isRelocated()) {
            // Carregador Absoluto: endereços já corrigidos pelo Linker
            // Basta copiar e pronto.
            return;
        }

        // Caso contrário, precisamos aplicar relocação no "code" que já foi copiado para a memória.
        // 4. Percorre os relocationRecords
        if (finalObj.getRelocationRecords() != null && !finalObj.getRelocationRecords().isEmpty()) {
            for (RelocationRecord rec : finalObj.getRelocationRecords()) {
                applyRelocationInMemory(memory, baseAddress, rec);
            }
        } else {
            // Se não há relocationRecords, mas isRelocated=false, pode ser que não haja endereços a ajustar
            // (ou seja, o ligador não gerou records, mas não marcou como relocado).
        }

        // 5. Ajusta a SymbolTable local (opcional, se quiser rastrear endereços em tempo de execução)
        SymbolTable symTab = finalObj.getSymbolTable();
        for (var entry : symTab.getAllSymbols().entrySet()) {
            SymbolTable.SymbolInfo info = entry.getValue();
            info.address += baseAddress;
        }

        // 6. Marca como relocado
        finalObj.setRelocated(true);
    }

    /**
     * Aplica um registro de relocação diretamente na memória do simulador.
     * Exemplo: soma baseAddress ao valor de length bytes (24 bits se length=3) contido no offset rec.getOffset().
     */
    private void applyRelocationInMemory(Memory memory, int baseAddress, RelocationRecord rec) {
        int offset = rec.offset(); // offset no code
        int length = rec.length(); // 1,2,3,4 bytes, etc.

        // Verifica se a faixa offset..offset+length cabe na memória
        if (offset + length > memory.getSize()) {
            throw new IllegalArgumentException("RelocationRecord fora dos limites da memória. offset=" + offset);
        }

        // Lê o valor atual (por ex, 3 bytes => 24 bits)
        int originalValue = 0;
        for (int i = 0; i < length; i++) {
            originalValue = (originalValue << 8) | (memory.readByte(offset + i) & 0xFF);
        }

        // Soma baseAddress (modelo simples)
        int newValue = originalValue + baseAddress;

        // Se for PC-relative, poderíamos subtrair algo, depende da forma de reloc no Linker, omitido aqui

        // Escreve de volta
        int temp = newValue;
        for (int i = length - 1; i >= 0; i--) {
            memory.writeByte(offset + i, temp & 0xFF);
            temp >>>= 8;
        }
    }
}
