package sicxesimulator.software.data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Registro de relocação:
 * <ul>
 *   <li>{@code offset} – posição no array de bytes que requer ajuste;</li>
 *   <li>{@code symbol} – nome do símbolo cujo endereço será somado;</li>
 *   <li>{@code length} – tamanho do campo em bytes (1–4);</li>
 *   <li>{@code pcRelative} – se true, loader subtrai o deslocamento PC (-3) após somar símbolo.</li>
 * </ul>
 */
public record RelocationRecord(int offset, String symbol, int length, boolean pcRelative)
        implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @Override public String toString() {
        return String.format("Reloc{off=%04X, sym=%s, len=%d, pcRel=%b}",
                offset, symbol, length, pcRelative);
    }
}
