package sicxesimulator.hardware.cpu.register;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Agrupa todos os registradores da CPU SIC/XE.
 *
 * <p>Fornece acesso individual por nome, visualização imutável da coleção e
 * operação de limpeza em lote.</p>
 *
 * @author Renan
 * @since 1.0.0
 */
public final class RegisterSet {

    private static final String[] NAMES =
            { "A", "X", "L", "B", "S", "T", "F", "PC", "SW" };

    private final Map<String, Register> registers = new HashMap<>();

    public RegisterSet() {
        for (String n : NAMES) registers.put(n, new Register(n));
    }

    /**
     * @param name identificador do registrador
     * @return instância correspondente ou {@code null} se inexistente
     */
    public Register getRegister(String name) {
        return registers.get(name);
    }

    /** @return coleção somente-leitura dos registradores */
    public Collection<Register> getAllRegisters() {
        return Collections.unmodifiableCollection(registers.values());
    }

    /** Define todos os registradores para zero. */
    public void clearAll() {
        registers.values().forEach(Register::clearRegister);
    }
}
