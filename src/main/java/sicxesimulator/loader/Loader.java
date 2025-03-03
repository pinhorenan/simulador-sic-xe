package sicxesimulator.loader;

import sicxesimulator.machine.Memory;
import sicxesimulator.machine.cpu.ControlUnit;
import java.io.IOException;

public class Loader {
    private final ControlUnit controlUnit;

    public Loader(ControlUnit controlUnit) {
        this.controlUnit = controlUnit;
    }

    /**
     * Carrega o código objeto na memória a partir do endereço especificado.
     *
     * @param memory       Referência à memória onde o código objeto será carregado.
     * @param startAddress Endereço de início do carregamento (definido pela diretiva START).
     * @param objectCode   Código objeto gerado pelo Assembler.
     * @throws IOException Se ocorrer um erro durante o carregamento.
     */
    public void load(Memory memory, int startAddress, byte[] objectCode) throws IOException {
        if (memory == null) {
            throw new IOException("A referência da memória é nula.");
        }
        if (objectCode == null || objectCode.length == 0) {
            throw new IOException("O código objeto está nulo ou vazio.");
        }
        if (startAddress < 0 || startAddress + objectCode.length > memory.getSize()) {
            throw new IOException("O programa não cabe na memória a partir do endereço de início especificado.");
        }

        // Define o endereço base na ControlUnit
        controlUnit.setBaseAddress(startAddress);

        // Escreve o código objeto na memória
        memory.writeBytes(startAddress, objectCode);
    }
}
