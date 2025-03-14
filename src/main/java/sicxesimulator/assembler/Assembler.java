package sicxesimulator.assembler;

import sicxesimulator.models.IntermediateRepresentation;
import sicxesimulator.models.ObjectFile;

import java.io.File;
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
        // Executa a primeira passagem com o código já expandido
        IntermediateRepresentation midCode = firstPass(expandedSource);
        midCode.setRawSourceCode(originalSource);

        ObjectFile meta = secondPass(midCode);

        meta.saveToFile(new File(midCode.getProgramName() + ".meta"));

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
