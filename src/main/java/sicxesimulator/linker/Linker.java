package sicxesimulator.linker;

import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.assembler.models.SymbolTable;

import java.util.List;

public class Linker {

    public ObjectFile link(List<ObjectFile> objectFiles) {
        if (objectFiles == null || objectFiles.isEmpty()) {
            throw new IllegalArgumentException("A lista de arquivos de objeto não pode ser nula ou vazia.");
        }

        // Criar um novo arquivo de objeto final
        byte[] combinedObjectCode = combineObjectCodes(objectFiles);

        // Resolver referências entre os arquivos de objeto (ajustar endereços, se necessário)
        List<String> resolvedSymbols = resolveSymbols(objectFiles);

        // Criar um novo ObjectFile com o código combinado
        ObjectFile linkedObjectFile = createLinkedObjectFile(combinedObjectCode, resolvedSymbols);

        System.out.println("Arquivos de objeto combinados com sucesso!");
        return linkedObjectFile;
    }

    private byte[] combineObjectCodes(List<ObjectFile> objectFiles) {
        // Combina todos os códigos de objeto dos arquivos em uma única sequência de bytes
        int totalLength = objectFiles.stream().mapToInt(f -> f.getObjectCode().length).sum();
        byte[] combined = new byte[totalLength];
        int currentPosition = 0;

        for (ObjectFile file : objectFiles) {
            byte[] objectCode = file.getObjectCode();
            System.arraycopy(objectCode, 0, combined, currentPosition, objectCode.length);
            currentPosition += objectCode.length;
        }

        return combined;
    }

    private List<String> resolveSymbols(List<ObjectFile> objectFiles) {
        // Aqui, você pode implementar a resolução de símbolos entre os arquivos (por exemplo, referências a funções e variáveis)
        // Por enquanto, retornamos uma lista fictícia de símbolos resolvidos
        return List.of("Symbol1", "Symbol2", "Symbol3");
    }

    private ObjectFile createLinkedObjectFile(byte[] combinedObjectCode, List<String> resolvedSymbols) {
        // Cria um novo arquivo de objeto a partir do código combinado e dos símbolos resolvidos
        int startAddress = 0;  // O endereço inicial pode ser ajustado conforme necessário
        SymbolTable symbolTable = new SymbolTable(); // TODO: Passar a tabela de símbolos correta

        // Cria e retorna o arquivo de objeto final
        return new ObjectFile(startAddress, combinedObjectCode, symbolTable, "linked_program.obj");
    }
}
