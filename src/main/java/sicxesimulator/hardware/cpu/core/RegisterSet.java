package sicxesimulator.hardware.cpu.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Conjunto de registradores SIC/XE com acesso individual e reset.
 *
 * @author Renan
 * @since 1.0.0
 */
public class RegisterSet {
    private final Map<String, Register> registers = new HashMap<>();

    public RegisterSet() {
        for (String name : new String[]{"A","X","L","B","S","T","F","PC","SW"}) {
            registers.put(name, new Register(name));
        }
    }

    /**
     * Obtém registrador por nome.
     * @param name nome do registrador
     * @return instância ou null
     */
    public Register getRegister(String name) {
        return registers.get(name);
    }

    /**
     * @return coleção de todos os registradores
     */
    public Collection<Register> getAllRegisters() {
        return registers.values();
    }

    /** Limpa todos os registradores (setValue(0)). */
    public void clearAll() {
        registers.values().forEach(Register::clearRegister);
    }
}
