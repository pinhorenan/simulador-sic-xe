package sicxesimulator.software.loader;

import sicxesimulator.hardware.memory.Memory;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.Symbol;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.software.data.RelocationRecord;

/**
 * Responsável por carregar um {@link ObjectFile} na memória principal,
 * aplicando relocações quando necessário.
 */
public class Loader {

    /**
     * Carrega o código objeto na memória.
     *
     * <p>Se o objeto já estiver realocado, usa o endereço inicial do próprio objeto.
     * Caso contrário, utiliza o endereço base fornecido.</p>
     *
     * <p>Além de copiar o código, realiza as seguintes etapas se o objeto não estiver realocado:</p>
     * <ol>
     *   <li>Atualiza os endereços da {@link SymbolTable} com base no endereço base.</li>
     *   <li>Aplica os registros de relocação sobre a memória.</li>
     *   <li>Marca o objeto como realocado.</li>
     * </ol>
     *
     * @param finalObject Objeto final já linkado e pronto para ser carregado.
     * @param memory Instância da memória principal.
     * @param baseAddress Endereço base onde o programa será carregado (caso não realocado).
     */
    public void loadObjectFile(ObjectFile finalObject, Memory memory, int baseAddress) {
        // Se o objeto já estiver totalmente realocado (modo absoluto), usamos o endereço de início presente no objeto;
        // caso contrário, usamos o baseAddress informado.
        int effectiveLoadAddress = finalObject.isFullyRelocated() ? finalObject.getStartAddress() : baseAddress;

        byte[] code = finalObject.getObjectCode();
        int codeLength = code.length;

        // Verifica se cabe na memória
        if (baseAddress + codeLength > memory.getSize()) {
            throw new IllegalArgumentException("Programa nao cabe na memoria (base + code.length > memoria)."
            );
        }

        // Copia o array de bytes do objeto para a memória
        copyCodeToMemory(memory, effectiveLoadAddress, code);

        // Se o objeto não estiver 100% realocado, aplicamos as relocações
        if (!finalObject.isFullyRelocated()) {
            // 1) Atualiza a SymbolTable
            updateSymbolTableAddresses(finalObject.getSymbolTable(), effectiveLoadAddress);

            // 2) Aplica as relocações com os símbolos já atualizados
            if (finalObject.getRelocationRecords() != null && !finalObject.getRelocationRecords().isEmpty()) {
                applyRelocations(memory, effectiveLoadAddress, finalObject);
            }

            // 3) Marca que agora está realocado
            finalObject.setFullyRelocated(true);
        }
    }

    /**
     * Copia o código objeto para a memória a partir do endereço de carga.
     *
     * @param memory Memória principal.
     * @param effectiveLoadAddress Endereço inicial de carga.
     * @param byteCode Código objeto a ser copiado.
     */
    private void copyCodeToMemory(Memory memory, int effectiveLoadAddress, byte[] byteCode) {
        for (int i = 0; i < byteCode.length; i++) {
            memory.writeByte(effectiveLoadAddress + i, byteCode[i] & 0xFF);
        }
    }

    /**
     * Aplica todos os registros de relocação especificados no objeto.
     *
     * @param memory Memória principal.
     * @param effectiveLoadAddress Endereço de início de carga.
     * @param finalObject Objeto contendo os registros de relocação e tabela de símbolos.
     */
    private void applyRelocations(Memory memory, int effectiveLoadAddress, ObjectFile finalObject) {
        SymbolTable symTab = finalObject.getSymbolTable();
        for (RelocationRecord rec : finalObject.getRelocationRecords()) {
            applyRelocationInMemory(memory, effectiveLoadAddress, rec, symTab);
        }
    }

    /**
     * Atualiza os endereços dos símbolos somando o endereço de carga.
     *
     * @param symbolTable Tabela de símbolos original.
     * @param effectiveLoadAddress Endereço base onde o programa foi carregado.
     */
    private void updateSymbolTableAddresses(SymbolTable symbolTable, int effectiveLoadAddress) {
        for (var entry : symbolTable.getAllSymbols().entrySet()) {
            Symbol symbol = entry.getValue();
            symbol.address += effectiveLoadAddress;
        }
    }

    /**
     * Aplica uma única entrada de relocação na memória.
     *
     * <p>O valor existente na memória no offset indicado é lido,
     * somado ao endereço do símbolo associado e gravado de volta.</p>
     *
     * <p>Se o campo {@code pcRelative} for {@code true}, subtrai 3 do valor final (para ajuste relativo ao PC).</p>
     *
     * @param memory Memória principal.
     * @param effectiveLoadAddress Endereço de início de carga.
     * @param rec Registro de relocação contendo offset e símbolo.
     * @param symTab Tabela de símbolos com endereços já atualizados.
     */
    private void applyRelocationInMemory(Memory memory, int effectiveLoadAddress, RelocationRecord rec, SymbolTable symTab) {
        // offset é a posição (relativa ao startAddress do .obj),
        // mas já "fundido" na concatenação do Linker
        int offset = rec.offset();
        int length = rec.length();

        // Lê valor atual na memória no offset especificado
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 8) | (memory.readByte(effectiveLoadAddress + offset + i) & 0xFF);
        }

        // Obter o endereço do símbolo na na SymbolTable
        Integer symbolAddress = symTab.getSymbolAddress(rec.symbol());
        if (symbolAddress == null) {
            throw new RuntimeException("Simbolo nao encontrado no Loader: " + rec.symbol());
        }

        // Soma o endereço do símbolo ao valor
        int newValue = value + symbolAddress;

        // Se pcRelative => subtrai 3
        if (rec.pcRelative()) {
            newValue -= 3;
        }

        // Grava o resultado de volta na memória
        int tmp = newValue;
        for (int i = length - 1; i >= 0; i--) {
            memory.writeByte(effectiveLoadAddress + offset + i, tmp & 0xFF);
            tmp >>>= 8;
        }
    }
}
