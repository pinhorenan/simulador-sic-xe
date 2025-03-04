package sicxesimulator.linker;

import java.util.HashMap;
import java.util.Map;

public class Linker {
    private Map<String, Integer> symbolTablesToLink = new HashMap<>();

    public Linker() {

    }

    public byte[] link(byte[] objectCode1, byte[] objectCode2) {
        // TODO: Implementar
        byte[] linkedCode = new byte[objectCode1.length + objectCode2.length];
        return linkedCode;
    }
}
