package sicxesimulator.assembler;

import sicxesimulator.assembler.models.IntermediateRepresentation;
import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.assembler.processing.FirstPassProcessor;
import sicxesimulator.assembler.processing.SecondPassProcessor;

import java.util.List;
import java.util.logging.Logger;

public class Assembler {
    private static final Logger logger = Logger.getLogger(Assembler.class.getName());

    // Processadores para cada passagem
    private FirstPassProcessor firstPassProcessor;
    private SecondPassProcessor secondPassProcessor;

    public Assembler() {
        this.firstPassProcessor = new FirstPassProcessor();
        this.secondPassProcessor = new SecondPassProcessor();
    }

    /**
     * Agrega e orquestra o processo de montagem:
     * Executa a primeira passagem (para gerar a representação intermediária)
     * e depois a segunda passagem (para gerar o código objeto).
     */
    public ObjectFile assemble(List<String> sourceLines) {
        IntermediateRepresentation ir = firstPass(sourceLines);
        return secondPass(ir);
    }

    /**
     * Realiza a primeira passagem delegando para a classe FirstPassProcessor.
     */
    public IntermediateRepresentation firstPass(List<String> sourceLines) {
        return firstPassProcessor.process(sourceLines);
    }

    /**
     * Realiza a segunda passagem delegando para a classe SecondPassProcessor.
     */
    public ObjectFile secondPass(IntermediateRepresentation ir) {
        return secondPassProcessor.generateObjectFile(ir);
    }

    /**
     * Reinicializa o estado do Assembler e de seus processadores.
     */
    public void reset() {
        firstPassProcessor.reset();
        secondPassProcessor.reset();
    }
}
