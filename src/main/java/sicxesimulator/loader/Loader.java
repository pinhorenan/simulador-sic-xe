package sicxesimulator.loader;

import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.machine.Machine;
import sicxesimulator.machine.memory.Memory;
import java.util.logging.Logger;

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
        int startWordAddress = objectFile.getStartAddress();
        byte[] objectCode = objectFile.getObjectCode();

        validateObjectCode(objectCode);
        validateMemoryBounds(memory, startWordAddress, objectCode.length);

        loadProgramIntoMemory(memory, startWordAddress, objectCode);
        initializeProgramCounter(startWordAddress);

        logSuccess(startWordAddress, objectCode);
    }

    private void validateObjectCode(byte[] objectCode) {
        if (objectCode.length % 3 != 0) {
            throw new IllegalArgumentException("Código objeto deve ter tamanho múltiplo de 3 bytes.");
        }
    }

    private void validateMemoryBounds(Memory memory, int startWordAddress, int codeLength) {
        int requiredWords = codeLength / 3;
        if (startWordAddress < 0 || (startWordAddress + requiredWords) > memory.getAddressRange()) {
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

    private void initializeProgramCounter(int startWordAddress) {
        int byteAddress = startWordAddress * 3; // Converte para endereço de byte
        machine.getControlUnit().setIntValuePC(byteAddress);
    }

    private void logSuccess(int startWordAddress, byte[] objectCode) {
        logger.info(() -> String.format(
                "Programa carregado:\nEndereço inicial (palavra): 0x%06X\nEndereço inicial (byte): 0x%06X\nTamanho: %d bytes",
                startWordAddress,
                startWordAddress * 3,
                objectCode.length
        ));
    }

    public Machine getMachine() {
        return machine;
    }
}