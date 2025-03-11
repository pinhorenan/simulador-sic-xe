package sicxesimulator.utils;

import sicxesimulator.machine.cpu.Register;
import sicxesimulator.machine.cpu.RegisterSet;

/**
 * Classe utilitária para mapeamento de valores.
 */
public abstract class Map {

    /**
     * Retorna o Opcode correspondente ao mnemônico.
     * @param mnemonic Mnemônico da instrução.
     * @return Opcode correspondente.
     */
    public static int mnemonicToOpcode(String mnemonic) {
        if (!Constants.OPCODES.containsKey(mnemonic.toUpperCase()))
            throw new IllegalArgumentException("Instrução desconhecida: " + mnemonic);
        return Constants.OPCODES.get(mnemonic.toUpperCase());
    }

    /**
     * Retorna o registrador conforme o número indicado.
     * @param num Número do registrador (0 a 5).
     * @return Registrador correspondente.
     */
    public static Register numberToRegister(int num, RegisterSet registers) {
        return switch (num) {
            case 0 -> registers.getRegister("A");
            case 1 -> registers.getRegister("X");
            case 2 -> registers.getRegister("L");
            case 3 -> registers.getRegister("B");
            case 4 -> registers.getRegister("S");
            case 5 -> registers.getRegister("T");
            default -> throw new IllegalArgumentException("Registrador inválido: " + num);
        };
    }

    /**
     * Retorna um valor em milissegundo correspondente à velocidade de simulação.
     * @param simulationSpeeed Velocidade de simulação (1 a 4). *Se 0 = default = tempo real.
     * @return Valor em milissegundo correspondente à velocidade de simulação.
     */
    public static int simulationSpeedToCycleDelay(int simulationSpeeed) {
        return switch (simulationSpeeed) {
            case 1 -> 1000;
            case 2 -> 500;
            case 3 -> 250;
            case 4 -> 100;
            default -> 0;
        };
    }
}
