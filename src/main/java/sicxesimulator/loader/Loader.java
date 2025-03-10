package sicxesimulator.loader;

import sicxesimulator.models.ObjectFile;
import sicxesimulator.machine.Machine;
import sicxesimulator.machine.Memory;
import java.util.logging.Logger;

@SuppressWarnings("ClassCanBeRecord")
public class Loader {
    private static final Logger logger = Logger.getLogger(Loader.class.getName());
    private final Machine machine;

    public Loader(Machine machine) {
        if (machine == null) {
            throw new IllegalArgumentException("Máquina não pode ser nula.");
        }
        this.machine = machine;
    }

    public void load(ObjectFile objectFile) {
        Memory memory = machine.getMemory();
        // Agora, startAddress já está em termos de palavras
        int startWordAddress = objectFile.getStartAddress();

        byte[] objectCode = objectFile.getObjectCode();
        validateObjectCode(objectCode);
        validateMemoryBounds(memory, startWordAddress, objectCode.length / 3);

        loadProgramIntoMemory(memory, startWordAddress, objectCode);
        // Inicializa o PC usando o endereço em palavras, sem conversão
        machine.getControlUnit().setIntValuePC(startWordAddress);

        logSuccess(startWordAddress, objectCode);
    }


    private void validateObjectCode(byte[] objectCode) {
        if (objectCode.length % 3 != 0) {
            throw new IllegalArgumentException("Código objeto deve ter tamanho múltiplo de 3 bytes.");
        }
    }

    private void validateMemoryBounds(Memory memory, int startWordAddress, int wordCount) {
        if (startWordAddress < 0 || (startWordAddress + wordCount) > memory.getAddressRange()) {
            throw new IllegalArgumentException("Programa excede os limites da memória.");
        }
    }

    private void loadProgramIntoMemory(Memory memory, int startWordAddress, byte[] objectCode) {
        for (int i = 0; i < objectCode.length; i += 3) {
            byte[] word = new byte[3];
            System.arraycopy(objectCode, i, word, 0, 3);
            memory.writeWord(startWordAddress + (i / 3), word);
        }
    }

    private void logSuccess(int startWordAddress, byte[] objectCode) {
        logger.info(() -> String.format(
                "Programa carregado:\nEndereço inicial (palavra): 0x%04X\nTamanho: %d bytes",
                startWordAddress,
                objectCode.length
        ));
    }

    public Machine getMachine() {
        return machine;
    }
}
