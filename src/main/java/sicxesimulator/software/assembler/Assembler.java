package sicxesimulator.software.assembler;

import sicxesimulator.software.assembler.data.IntermediateRepresentation;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.common.utils.Constants;

import java.io.File;
import java.util.List;

/**
 * Classe principal responsável por orquestrar o processo completo de montagem.
 *
 * Executa a primeira e segunda passagens do montador e gera os arquivos de
 * saída (como .meta e .obj) a partir do código-fonte assembly fornecido.
 */
public class Assembler {
    private final AssemblerFirstPass firstPass;
    private final AssemblerSecondPass secondPass;

    public Assembler() {
        firstPass = new AssemblerFirstPass();
        secondPass = new AssemblerSecondPass();
    }

    /**
     * Executa o processo completo de montagem.
     *
     * @param originalSource Código-fonte original (sem macros expandidas).
     * @param expandedSource Código-fonte com macros expandidas.
     * @return {@link ObjectFile} representando o código objeto final.
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
     * Executa a primeira passagem, gerando a representação intermediária.
     *
     * @param originalSourceCode Código original (exibição e referência).
     * @param sourceCodeWithMacrosExpanded Código com macros resolvidas.
     * @return {@link IntermediateRepresentation} gerada.
     */
    public IntermediateRepresentation firstPass(List<String> originalSourceCode, List<String> sourceCodeWithMacrosExpanded) {
        return firstPass.process(originalSourceCode, sourceCodeWithMacrosExpanded);
    }


    /**
     * Executa a segunda passagem a partir da representação intermediária.
     *
     * @param midCode {@link IntermediateRepresentation} gerada na primeira passagem.
     * @return {@link ObjectFile} final.
     */
    public ObjectFile secondPass(IntermediateRepresentation midCode) {
        return secondPass.generateObjectFile(midCode);
    }
}
