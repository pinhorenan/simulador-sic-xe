package sicxesimulator.machine.cpu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RegisterSet {
    protected final Map<String, Register> registers = new HashMap<>();

    /**
     * Cria um novo conjunto de registradores.
     */
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

    /// ===== Métodos Getters =====
    public Register getRegister(String name) {
        return registers.get(name);
    }

    public Collection<Register> getAllRegisters() {
        return this.registers.values();
    }

    /// ===== Métodos Auxiliares =====
    public void clearAll() {
        registers.values().forEach(Register::clearRegister);
    }

}
