package sicxesimulatorTest.software.loader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sicxesimulator.data.ObjectFile;
import sicxesimulator.data.SymbolTable;
import sicxesimulator.data.records.RelocationRecord;
import sicxesimulator.hardware.Memory;
import sicxesimulator.software.loader.Loader;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoaderTest {

    private Memory memory;
    private Loader loader;

    @BeforeEach
    void setup() {
        // Cria memória de 256 bytes (por exemplo)
        memory = new Memory(256);
        loader = new Loader();
    }

    @Test
    void loadFullyRelocatedObject() {
        // Objeto que já está totalmente realocado
        byte[] code = {0x10, 0x20, 0x30, 0x40};
        SymbolTable symTab = new SymbolTable();
        symTab.addSymbol("START", 0, true);

        // Marcamos isFullyRelocated = true e não fornecemos RelocationRecords
        ObjectFile fullyRelocated = new ObjectFile(
                0,
                code,
                symTab,
                "FULL",
                Collections.emptyList(),
                Collections.emptySet(),
                Collections.emptyList()
        );
        fullyRelocated.setFullyRelocated(true); // Importante

        // Carrega na base 10, mas não deve realocar nada
        loader.loadObjectFile(fullyRelocated, memory, 10);

        // Verifica se a memória bate com o code, a partir de address=10
        for (int i = 0; i < code.length; i++) {
            assertEquals(Byte.toUnsignedInt(code[i]), memory.readByte(10 + i));
        }

        // Verifica se SymbolTable não mudou (porque já estava relocada)
        assertEquals(0, symTab.getSymbolAddress("START"));
    }

    @Test
    void loadObjectThatDoesNotFitMemory() {
        // code com tamanho maior que a memória
        byte[] code = new byte[300]; // mais do que 256
        ObjectFile bigObject = new ObjectFile(
                0,
                code,
                new SymbolTable(),
                "TOO_BIG",
                Collections.emptyList(),
                Collections.emptySet(),
                Collections.emptyList()
        );

        // Tentar carregar deve gerar exceção, pois base + codeLength > memory.getSize()
        assertThrows(
                IllegalArgumentException.class,
                () -> loader.loadObjectFile(bigObject, memory, 0)
        );
    }

    @Test
    void loadPartialRelocationObject() {
        // Código de 4 bytes
        // Suponha que no offset 2..3 (2 bytes) haja um endereço que precisa ser somado ao base + symbol
        byte[] code = {0x00, 0x00, 0x12, (byte) 0x34}; // valor 0x1234 a ser realocado
        SymbolTable symTab = new SymbolTable();
        symTab.addSymbol("MYLABEL", 10, true); // endereço 10, mas não atualizado ainda

        // Precisamos de um relocation record indicando que, no offset=2, length=2,
        // devamos somar end. do símbolo MYLABEL
        // pcRelative=false => soma normal
        RelocationRecord rec = new RelocationRecord(2, "MYLABEL", 2, false);

        ObjectFile partialObj = new ObjectFile(
                0,
                code,
                symTab,
                "PART",
                Collections.emptyList(),
                Collections.emptySet(),
                List.of(rec)
        );
        partialObj.setFullyRelocated(false); // Avisa que ainda não realocado

        // Carrega na base 50
        loader.loadObjectFile(partialObj, memory, 50);

        // Esperamos que a SymbolTable seja atualizada: MYLABEL=10 + base=50 => 60
        assertEquals(60, symTab.getSymbolAddress("MYLABEL"));

        // O valor original (0x1234) deve ter sido somado a 60 => 0x1234 + 0x003C = 0x1270
        // Em decimal: 0x1234 = 4660,  0x3C = 60  => 4720 = 0x1270
        int lo = memory.readByte(50 + 3) & 0xFF; // byte[3]
        int hi = memory.readByte(50 + 2) & 0xFF; // byte[2]
        int relocatedVal = (hi << 8) | lo;
        assertEquals(0x1270, relocatedVal);
    }

    @Test
    void loadPartialRelocationObjectPcRelative() {
        // Código 2 bytes, valor inicial 0x0005, por exemplo
        byte[] code = {0x00, 0x05};
        SymbolTable symTab = new SymbolTable();
        symTab.addSymbol("PCSYM", 100, true); // endereço 100

        // Precisamos realocar offset=0, length=2
        // pcRelative=true => soma symAddr e subtrai 3
        RelocationRecord rec = new RelocationRecord(0, "PCSYM", 2, true);

        ObjectFile obj = new ObjectFile(
                0,
                code,
                symTab,
                "PCR",
                Collections.emptyList(),
                Collections.emptySet(),
                List.of(rec)
        );
        obj.setFullyRelocated(false);

        // Carrega na base 20
        loader.loadObjectFile(obj, memory, 20);

        // SymbolTable deve somar base => PCSYM = 100 + 20 = 120
        assertEquals(120, symTab.getSymbolAddress("PCSYM"));

        // Valor realocado: oldVal=0x0005 + sym(120) - 3 = 0x0005 + 0x0078 - 0x0003
        // = 0x007A => decimal 122
        int hi = memory.readByte(20) & 0xFF;
        int lo = memory.readByte(21) & 0xFF;
        int relocatedVal = (hi << 8) | lo;
        assertEquals(0x007A, relocatedVal);
    }

    @Test
    void loadObjectMissingSymbolInRelocation() {
        // code com 2 bytes
        byte[] code = {0x00, 0x10};
        SymbolTable symTab = new SymbolTable();
        // Não adicionamos o símbolo que será precisado
        // simTab.addSymbol("MISSING", 0, true);

        RelocationRecord rec = new RelocationRecord(0, "MISSING", 2, false);

        ObjectFile obj = new ObjectFile(
                0,
                code,
                symTab,
                "MISSYM",
                Collections.emptyList(),
                Collections.emptySet(),
                List.of(rec)
        );
        obj.setFullyRelocated(false);

        // Esperamos exceção no applyRelocationInMemory (symAddr == null)
        assertThrows(RuntimeException.class, () -> loader.loadObjectFile(obj, memory, 0));
    }
}
