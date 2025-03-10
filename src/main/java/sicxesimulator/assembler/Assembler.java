package sicxesimulator.assembler;

import sicxesimulator.models.IntermediateRepresentation;
import sicxesimulator.models.ObjectFile;

import java.util.List;

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
    public ObjectFile assemble(List<String> sourceLines) {
        IntermediateRepresentation midCode = firstPass(sourceLines);
        return secondPass(midCode);
    }

    /**
     * Realiza a primeira passagem delegando para a classe FirstPassProcessor.
     */
    public IntermediateRepresentation firstPass(List<String> sourceLines) {
        return firstPass.process(sourceLines);
    }

    /**
     * Realiza a segunda passagem delegando para a classe SecondPassProcessor.
     */
    public ObjectFile secondPass(IntermediateRepresentation midCode) {
        return secondPass.generateObjectFile(midCode);
    }

    /**
     * Reinicializa o estado do Assembler e de seus processadores.
     */
    public void reset() {
        firstPass.reset();
        secondPass.reset();
    }
}
