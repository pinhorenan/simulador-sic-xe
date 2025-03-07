package sicxesimulator.assembler;

import sicxesimulator.assembler.models.IntermediateRepresentation;
import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.assembler.processing.FirstPassProcessor;
import sicxesimulator.assembler.processing.SecondPassProcessor;

import java.util.ArrayList;
import java.util.List;

public class Assembler {
    // Processadores para cada passagem
    private final FirstPassProcessor firstPassProcessor;
    private final SecondPassProcessor secondPassProcessor;
    private final List<ObjectFile> generatedObjectFiles;

    public Assembler() {
        firstPassProcessor = new FirstPassProcessor();
        secondPassProcessor = new SecondPassProcessor();
        generatedObjectFiles = new ArrayList<>();
    }

    /**
     * Agrega e orquestra o processo de montagem:
     * Executa a primeira passagem (para gerar a representação intermediária)
     * e depois a segunda passagem (para gerar o código objeto).
     */
    public ObjectFile assemble(List<String> sourceLines) {
        IntermediateRepresentation midCode = firstPass(sourceLines);
        ObjectFile generatedObject = secondPass(midCode);
        generatedObjectFiles.add(generatedObject);
        return generatedObject;
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
    public ObjectFile secondPass(IntermediateRepresentation midCode) {
        return secondPassProcessor.generateObjectFile(midCode);
    }

    // Getters

    public List<ObjectFile> getGeneratedObjectFiles() {
        return generatedObjectFiles;
    }

    public ObjectFile getLastGeneratedObjectFile() {
        return generatedObjectFiles.get(generatedObjectFiles.size() - 1);
    }

    /**
     * Reinicializa o estado do Assembler e de seus processadores.
     */
    public void reset() {
        firstPassProcessor.reset();
        secondPassProcessor.reset();
    }
}
