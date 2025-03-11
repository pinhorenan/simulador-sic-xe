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
        int totalProgramSize = 0;
        for (ObjectFile obj : objectFiles) {
            totalProgramSize += obj.getMachineCode().length;
        }
        byte[] linkedObjectCode = new byte[totalProgramSize];
        int cumulativeOffset = 0;

        // Se fullRelocation for true, ajusta cada instrução; caso contrário, concatena sem alteração.
        for (ObjectFile obj : objectFiles) {
            int relocationOffset = moduleRelocationOffsets.get(obj);
            byte[] objCode = obj.getMachineCode().clone();
            if (fullRelocation) {
                // Aplica a relocação: para cada palavra (3 bytes) ajusta o valor
                for (int i = 0; i < objCode.length; i += 3) {
                    int value = ((objCode[i] & 0xFF) << 16) |
                            ((objCode[i + 1] & 0xFF) << 8) |
                            (objCode[i + 2] & 0xFF);
                    value += relocationOffset;
                    objCode[i]     = (byte) ((value >> 16) & 0xFF);
                    objCode[i + 1] = (byte) ((value >> 8) & 0xFF);
                    objCode[i + 2] = (byte) (value & 0xFF);
                }
            }
            System.arraycopy(objCode, 0, linkedObjectCode, cumulativeOffset, objCode.length);
            cumulativeOffset += objCode.length;
        }

        int finalStartAddress;
        if (fullRelocation) {
            // Já relocacionado: startAddress em palavras será loadAddress/3.
            finalStartAddress = loadAddress / 3;
        } else {
            // Se não for relocacionado, mantém o startAddress original do primeiro módulo
            // (pressupondo que os módulos foram montados com startAddress em palavras)
            finalStartAddress = objectFiles.get(0).getStartAddress();
        }

        ObjectFile finalObj = new ObjectFile(finalStartAddress, linkedObjectCode, globalSymbolTable, programName);
        finalObj.setRelocated(fullRelocation);
        return finalObj;
    }

}
