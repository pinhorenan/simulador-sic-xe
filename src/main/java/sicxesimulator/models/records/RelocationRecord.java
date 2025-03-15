package sicxesimulator.models.records;

import java.io.Serializable;

/**
 * Representa um registro de relocação para corrigir endereços no machineCode.
 * Offset: posição em bytes no machineCode onde há um valor a corrigir
 * Symbol: nome do símbolo (local ou importado)
 * Length: tamanho em bytes do campo que representa o endereço
 * PcRelative: se true, pode haver ajustes específicos (ex: subtrair 3 bytes, etc. no caso do SIC/XE)
 */
public record RelocationRecord(int offset, String symbol, int length, boolean pcRelative) implements Serializable {

    @Override
    public String toString() {
        return String.format("RelocRecord{offset=%04X, sym=%s, len=%d, pcRel=%b}", offset, symbol, length, pcRelative);
    }
}
