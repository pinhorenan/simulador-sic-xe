package sicxesimulator.assembler;

import sicxesimulator.assembler.records.IntermediateRepresentation;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.utils.Constants;

import java.io.File;
import java.util.List;

/**
 * Classe orquestradora do processo de montagem.
 * Executa a primeira e segunda passagens, gera os arquivos de saída e retorna o ObjectFile final.
 */
public class Assembler {
    private final AssemblerFirstPass firstPass;
    private final AssemblerSecondPass secondPass;

    public Assembler() {
        firstPass = new AssemblerFirstPass();
        secondPass = new AssemblerSecondPass();
    }

    /**
     * Realiza o processo completo de montagem:
     * 1ª passagem: Gera a IntermediateRepresentation.
     * 2ª passagem: Gera o código objeto e os arquivos associados.
     *
     * @param originalSource  Código-fonte original.
     * @param expandedSource  Código-fonte com macros expandidas.
     * @return ObjectFile contendo o código objeto final.
     */
    public ObjectFile assemble(List<String> originalSource, List<String> expandedSource) {
        // 1ª passagem
        IntermediateRepresentation midCode = firstPass(originalSource, expandedSource);

        // 2ª passagem
        ObjectFile meta = secondPass(midCode);

        // Serializa o ObjectFile binário (.meta)
        String metaFileName = midCode.programName() + ".meta";
        File metaFile = new File(Constants.SAVE_DIR, metaFileName);
        meta.saveToFile(metaFile);

        return meta;
    }

    /**
     * Chama a primeira passagem do montador.
     * @param originalSourceCode Código-fonte original (utilizado para exibir depois na interface).
     * @param sourceCodeWithMacrosExpanded Código-fonte com macros expandidas.
     * @return IntermediateRepresentation gerada na primeira passagem.
     */
    public IntermediateRepresentation firstPass(List<String> originalSourceCode, List<String> sourceCodeWithMacrosExpanded) {
        return firstPass.process(originalSourceCode, sourceCodeWithMacrosExpanded);
    }

    /**
     * Chama a segunda passagem do montador.
     * @param midCode Representação intermediária.
     * @return ObjectFile final.
     */
    public ObjectFile secondPass(IntermediateRepresentation midCode) {
        return secondPass.generateObjectFile(midCode);
    }
}
