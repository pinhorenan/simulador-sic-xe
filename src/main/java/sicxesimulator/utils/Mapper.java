package sicxesimulator.utils;

import sicxesimulator.hardware.cpu.core.Register;
import sicxesimulator.hardware.cpu.core.RegisterSet;

/**
 * Classe utilitária para mapeamentos relacionados à arquitetura SIC/XE.
 *
 * Inclui:
 * - Conversão entre nomes de registradores e seus índices.
 * - Acesso a registradores por número.
 * - Mapeamento de mnemônicos para opcodes.
 * - Conversão de velocidades de simulação.
 */
public abstract class Mapper {

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
     * Converte o nome do registrador para seu número correspondente.
     * Exemplo: "A" -> 0, "X" -> 1, "L" -> 2, "B" -> 3, "S" -> 4, "T" -> 5.
     *
     * @param regName Nome do registrador (case insensitive).
     * @return Número do registrador.
     */
    public static int registerNameToNumber(String regName) {
        return switch (regName.toUpperCase()) {
            case "A" -> 0;
            case "X" -> 1;
            case "L" -> 2;
            case "B" -> 3;
            case "S" -> 4;
            case "T" -> 5;
            case "F" -> 6;
            case "PC" -> 8;
            case "SW" -> 9;
            default -> throw new IllegalArgumentException("Registrador inválido: " + regName);
        };
    }

    /**
     * Retorna o objeto Register correspondente ao número fornecido, usando o conjunto de registradores.
     *
     * @param num Número do registrador (0 a 5).
     * @param registers Conjunto de registradores.
     * @return O objeto Register correspondente.
     */
    public static Register getRegisterByNumber(int num, RegisterSet registers) {
        return switch (num) {
            case 0 -> registers.getRegister("A");
            case 1 -> registers.getRegister("X");
            case 2 -> registers.getRegister("L");
            case 3 -> registers.getRegister("B");
            case 4 -> registers.getRegister("S");
            case 5 -> registers.getRegister("T");
            case 6 -> registers.getRegister("F");
            case 8 -> registers.getRegister("PC");
            case 9 -> registers.getRegister("SW");
            default -> throw new IllegalArgumentException("Registrador inválido: " + num);
        };
    }
}
