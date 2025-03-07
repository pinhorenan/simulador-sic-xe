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
        int startByteAddress = objectFile.getStartAddress();

        // Valida o endereço de início
        validateStartAddress(startByteAddress);
        int startWordAddress = startByteAddress / 3; // Conversão para índice de palavra

        byte[] objectCode = objectFile.getObjectCode();
        validateObjectCode(objectCode);
        validateMemoryBounds(memory, startWordAddress, objectCode.length);

        loadProgramIntoMemory(memory, startWordAddress, objectCode);
        initializeProgramCounter(startByteAddress); // Usa o endereço em bytes sem multiplicar

        logSuccess(startByteAddress, objectCode);
    }

    private void validateObjectCode(byte[] objectCode) {
        if (objectCode.length % 3 != 0) {
            throw new IllegalArgumentException("Código objeto deve ter tamanho múltiplo de 3 bytes.");
        }
    }

    private void validateStartAddress(int startByteAddress) {
        if (startByteAddress % 3 != 0) {
            throw new IllegalArgumentException("Endereço inicial deve ser múltiplo de 3.");
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

    private void initializeProgramCounter(int startAddress) {
        // Corrigido: usa o startAddress diretamente, pois já está em bytes.
        machine.getControlUnit().setIntValuePC(startAddress);
    }

    private void logSuccess(int startByteAddress, byte[] objectCode) {
        logger.info(() -> String.format(
                "Programa carregado:\nEndereço inicial (byte): 0x%06X\nTamanho: %d bytes",
                startByteAddress,
                objectCode.length
        ));
    }

    public Machine getMachine() {
        return machine;
    }
}
