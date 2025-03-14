package sicxesimulator.assembler;

import sicxesimulator.models.IntermediateRepresentation;
import sicxesimulator.models.ObjectFile;
import sicxesimulator.utils.Constants;
import sicxesimulator.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/// NOTA: O montador espera que o código fonte use endereços em bytes para o START.

public class Assembler {
    // Processadores para cada passagem
    private final AssemblerFirstPass firstPass;
    private final AssemblerSecondPass secondPass;

    public Assembler() {
        firstPass = new AssemblerFirstPass();
        secondPass = new AssemblerSecondPass();
    }

    /**
     * Agrega e orquestra o processo de montagem:
     * Executa a primeira passagem (para gerar a representação intermediária)
     * e depois a segunda passagem (para gerar o código objeto).
     */
    public ObjectFile assemble(List<String> originalSource, List<String> expandedSource) {
        // 1ª passagem
        IntermediateRepresentation midCode = firstPass(expandedSource);
        midCode.setRawSourceCode(originalSource);

        // 2ª passagem
        ObjectFile meta = secondPass(midCode);

        // Em vez de "writeFileInDir(..., meta.toString())", use a serialização real:
        String metaFileName = midCode.getProgramName() + ".meta";
        File metaFile = new File(Constants.SAVE_DIR, metaFileName);
        meta.saveToFile(metaFile);  // <- serialização binária

        return meta;
    }

    /**
     * Realiza a primeira passagem delegando para a classe FirstPassProcessor.
     */
    public IntermediateRepresentation firstPass(List<String> sourceCodeWithMacrosExpanded) {
        return firstPass.process(sourceCodeWithMacrosExpanded);
    }

    /**
     * Realiza a segunda passagem delegando para a classe SecondPassProcessor.
     */
    public ObjectFile secondPass(IntermediateRepresentation midCode) {
        return secondPass.generateObjectFile(midCode);
    }
}
