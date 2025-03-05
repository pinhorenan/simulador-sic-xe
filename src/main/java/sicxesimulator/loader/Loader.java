package sicxesimulator.loader;

import sicxesimulator.assembler.models.ObjectFile;
import sicxesimulator.machine.Machine;
import sicxesimulator.machine.cpu.Register;
import sicxesimulator.machine.memory.Memory;
import java.util.logging.Logger;

public class Loader {
    private static final Logger logger = Logger.getLogger(Loader.class.getName());

    private Machine machine;

    /**
     * Construtor que recebe a instância da máquina.
     *
     * @param machine a máquina onde o programa será carregado.
     */
    public Loader(Machine machine) {
        if (machine == null) {
            throw new IllegalArgumentException("Machine cannot be null.");
        }
        this.machine = machine;
    }

    /**
     * Carrega o ObjectFile na memória e define o PC para o endereço inicial.
     *
     * @param objectFile ObjectFile contendo o endereço inicial e o código objeto.
     * @throws IllegalArgumentException se o código objeto não couber na memória.
     */
    public void load(ObjectFile objectFile) {
        Memory memory = machine.getMemory();

        int startAddress = objectFile.getStartAddress(); // endereço inicial em palavra

        byte[] objectCode = objectFile.getObjectCode();

        // Verifica se o tamanho do código objeto é múltiplo de 3
        if (objectCode.length % 3 != 0) {
            throw new IllegalArgumentException("O tamanho do código objeto deve ser múltiplo de 3 bytes.");
        }
        int numWords = objectCode.length / 3;

        // Verifica se o programa cabe na memória
        if (startAddress < 0 || startAddress + numWords > memory.getAddressRange()) {
            throw new IllegalArgumentException("O programa excede o intervalo de endereços da memória.");
        }

        // Itera sobre o código objeto, agrupando em palavras e carregando na memória.
        for (int i = 0; i < numWords; i++) {
            byte[] wordData = new byte[3];
            System.arraycopy(objectCode, i * 3, wordData, 0, 3);
            memory.writeWordByAddress(startAddress + i, wordData);
        }

        // Define o PC para o endereço inicial
        machine.getControlUnit().setPC(objectFile.getStartAddress());

        logger.info("Programa carregado na memória a partir do endereço de palavra "
                + startAddress);
        logger.info("PC definido para " + pcAddress);
    }

    /**
     * Retorna a instância da máquina utilizada.
     *
     * @return a máquina onde o programa foi carregado.
     */
    public Machine getMachine() {
        return machine;
    }
}
