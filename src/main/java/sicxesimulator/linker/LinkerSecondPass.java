package sicxesimulator.linker;

import sicxesimulator.models.ObjectFile;
import sicxesimulator.models.SymbolTable;

import java.util.List;
import java.util.Map;

/**
 * O SecondPassLinker é responsável por:
 * 1. Concatenar os códigos objeto dos módulos.
 * 2. Realizar a relocação completa do código objeto, se exigido, ajustando os endereços absolutos.
 * 3. Gerar o ObjectFile final com o código objeto linkado.
 * Observação: O loadAddress é informado em bytes, mas o startAddress do ObjectFile é armazenado em palavras (1 palavra = 3 bytes).
 */
public class LinkerSecondPass {

    /**
     * Processa a segunda passagem do linker.
     *
     * @param objectFiles              Lista de módulos (ObjectFile) a serem linkados.
     * @param moduleRelocationOffsets  Mapeamento dos offsets de relocação de cada módulo (em bytes),
     *                                 gerado na primeira passagem.
     * @param loadAddress              Endereço de carga inicial (em bytes) para o programa linkado.
     * @param fullRelocation           Se true, ajusta completamente os endereços absolutos no código objeto;
     *                                 caso contrário, a relocação final ficará a cargo do Loader.
     * @param globalSymbolTable        Tabela de símbolos global construída na primeira passagem.
     * @param programName              Nome concatenado dos módulos, representando o programa linkado.
     * @return                         Um ObjectFile final contendo o código objeto linkado.
     */
    public ObjectFile process(List<ObjectFile> objectFiles,
                              Map<ObjectFile, Integer> moduleRelocationOffsets,
                              int loadAddress,
                              boolean fullRelocation,
                              SymbolTable globalSymbolTable,
                              String programName) {
        // Calcula o tamanho total do programa (em bytes)
        int totalProgramSize = 0;
        for (ObjectFile obj : objectFiles) {
            totalProgramSize += obj.getObjectCode().length;
        }

        byte[] linkedObjectCode = new byte[totalProgramSize];
        int cumulativeOffset = 0;

        // Para cada módulo, aplica o offset (se fullRelocation for true) e concatena o código objeto.
        for (ObjectFile obj : objectFiles) {
            int relocationOffset = moduleRelocationOffsets.get(obj);
            byte[] objCode = obj.getObjectCode().clone();
            if (fullRelocation) {
                // Percorre cada palavra (3 bytes) e ajusta o valor absoluto.
                for (int i = 0; i < objCode.length; i += 3) {
                    int value = ((objCode[i] & 0xFF) << 16) |
                            ((objCode[i + 1] & 0xFF) << 8) |
                            (objCode[i + 2] & 0xFF);
                    value += relocationOffset;
                    // Atualiza os 3 bytes com o valor ajustado.
                    objCode[i]     = (byte) ((value >> 16) & 0xFF);
                    objCode[i + 1] = (byte) ((value >> 8) & 0xFF);
                    objCode[i + 2] = (byte) (value & 0xFF);
                }
            }
            System.arraycopy(objCode, 0, linkedObjectCode, cumulativeOffset, objCode.length);
            cumulativeOffset += objCode.length;
        }

        // O startAddress do ObjectFile final é armazenado em palavras.
        // Como loadAddress está em bytes, convertemos dividindo por 3.
        int finalStartAddress = loadAddress / 3;

        return new ObjectFile(finalStartAddress, linkedObjectCode, globalSymbolTable, programName);
    }
}
