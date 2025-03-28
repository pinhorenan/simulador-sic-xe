package sicxesimulator.hardware.cpu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa o conjunto de registradores da arquitetura SIC/XE.
 *
 * Contém os registradores padrão ("A", "X", "L", "B", "S", "T", "F", "PC", "SW"),
 * com métodos de acesso individual ou em grupo, além de operação de reset.
 */
public class RegisterSet {
    protected final Map<String, Register> registers = new HashMap<>();

    public RegisterSet() {
        registers.put("A", new Register("A"));
        registers.put("X", new Register("X"));
        registers.put("L", new Register("L"));
        registers.put("B", new Register("B"));
        registers.put("S", new Register("S"));
        registers.put("T", new Register("T"));
        registers.put("F", new Register("F"));
        registers.put("PC", new Register("PC"));
        registers.put("SW", new Register("SW"));
    }

    /**
     * Retorna o registrador associado ao nome especificado.
     *
     * @param name Nome do registrador (ex: "A", "X", "PC", etc).
     * @return Objeto {@link Register} correspondente, ou null se não existir.
     */
    public Register getRegister(String name) {
        return registers.get(name);
    }

    /**
     * Retorna uma coleção com todos os registradores.
     *
     * @return Coleção de todos os {@link Register}'s do conjunto.
     */
    public Collection<Register> getAllRegisters() {
        return this.registers.values();
    }

    /**
     * Zera o valor de todos os registradores do conjunto.
     */
    public void clearAll() {
        registers.values().forEach(Register::clearRegister);
    }
}
