package sicxesimulator.software.assembler;

import sicxesimulator.common.utils.Constants;
import sicxesimulator.software.data.IntermediateRepresentation;
import sicxesimulator.software.data.ObjectFile;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Orquestra o processo de montagem de código SIC/XE:
 * executa a primeira e a segunda passagem,
 * e persiste o resultado em arquivo .meta.
 */
public class Assembler {

    private final AssemblerFirstPass firstPass;
    private final AssemblerSecondPass secondPass;

    /**
     * Inicializa os componentes de montagem.
     */
    public Assembler() {
        this.firstPass = new AssemblerFirstPass();
        this.secondPass = new AssemblerSecondPass();
    }

    /**
     * Executa a montagem completa e grava o arquivo meta.
     *
     * @param originalSourceLines Código-fonte original (sem expansão de macros).
     * @param expandedSourceLines Código-fonte com macros expandidas.
     * @return {@link ObjectFile} com o código objeto gerado.
     * @throws IllegalArgumentException se alguma lista de linhas for nula.
     */
    public ObjectFile assemble(List<String> originalSourceLines, List<String> expandedSourceLines) {
        Objects.requireNonNull(originalSourceLines, "originalSourceLines não pode ser nulo");
        Objects.requireNonNull(expandedSourceLines, "expandedSourceLines não pode ser nulo");

        IntermediateRepresentation intermediate = runFirstPass(originalSourceLines, expandedSourceLines);
        ObjectFile result = runSecondPass(intermediate);
        persistMetaFile(intermediate.programName(), result);

        return result;
    }

    /**
     * Executa a primeira passagem do montador.
     *
     * @param originalSourceLines Linhas originais do código-fonte.
     * @param expandedSourceLines Linhas após expansão de macros.
     * @return {@link IntermediateRepresentation} com dados para a segunda passagem.
     */
    public IntermediateRepresentation runFirstPass(List<String> originalSourceLines, List<String> expandedSourceLines) {
        return firstPass.process(originalSourceLines, expandedSourceLines);
    }

    /**
     * Executa a segunda passagem do montador.
     *
     * @param intermediate Representação intermediária gerada na primeira passagem.
     * @return {@link ObjectFile} final contendo o código de máquina e metadados.
     */
    public ObjectFile runSecondPass(IntermediateRepresentation intermediate) {
        return secondPass.generateObjectFile(intermediate);
    }

    /**
     * Persiste o arquivo .meta no diretório configurado.
     *
     *  @param programName Nome base para o arquivo meta.
     * @param objectFile Instância de {@link ObjectFile} a ser serializada.
     */
    private void persistMetaFile(String programName, ObjectFile objectFile) {
        String fileName = programName + ".meta";
        File metaFile = new File(Constants.SAVE_DIR, fileName);
        objectFile.saveToFile(metaFile);
    }
}
