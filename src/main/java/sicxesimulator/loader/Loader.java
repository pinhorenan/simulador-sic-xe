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

    /**
     * Carrega o ObjectFile na memória, aplicando relocação final caso necessário.
     *
     * @param objectFile  O objeto contendo o código objeto a ser carregado.
     * @param loadAddress O endereço de carga absoluto (em bytes). Exemplo: se os módulos foram
     *                    montados com START 300 (palavras), use loadAddress = 900 (300 * 3).
     */
    public void load(ObjectFile objectFile, int loadAddress) {
        Memory memory = machine.getMemory();
        byte[] objectCode = objectFile.getMachineCode();
        validateObjectCode(objectCode);

        int startWordAddress;
        if (objectFile.isRelocated()) {
            // Se o ObjectFile já estiver relocacionado, o startAddress está em palavras.
            startWordAddress = objectFile.getStartAddress();
        } else {
            // Se não estiver relocacionado, o Loader aplica a relocação final.
            // Calcula o deslocamento final em bytes.
            // Exemplo: se o módulo foi montado com START 300 (palavras) e loadAddress é 900 bytes,
            // relocationOffset = 900 - (300 * 3) = 0, ou seja, não há deslocamento.
            int relocationOffset = loadAddress - (objectFile.getStartAddress() * 3);
            // Ajusta cada palavra (3 bytes) do código objeto com o deslocamento calculado.
            for (int i = 0; i < objectCode.length; i += 3) {
                int value = ((objectCode[i] & 0xFF) << 16) |
                        ((objectCode[i + 1] & 0xFF) << 8) |
                        (objectCode[i + 2] & 0xFF);
                value += relocationOffset;
                objectCode[i]     = (byte) ((value >> 16) & 0xFF);
                objectCode[i + 1] = (byte) ((value >> 8) & 0xFF);
                objectCode[i + 2] = (byte) (value & 0xFF);
            }
            // Após a relocação, configura o PC com o loadAddress convertido para palavras.
            startWordAddress = loadAddress / 3;
        }

        validateMemoryBounds(memory, startWordAddress, objectCode.length / 3);
        loadProgramIntoMemory(memory, startWordAddress, objectCode);
        // Configura o PC com o endereço de carga (em palavras)
        machine.getControlUnit().setIntValuePC(startWordAddress*3);
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
