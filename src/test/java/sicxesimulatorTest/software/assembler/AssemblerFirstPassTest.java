package sicxesimulatorTest.software.assembler;

import org.junit.jupiter.api.Test;
import sicxesimulator.software.assembler.AssemblerFirstPass;
import sicxesimulator.software.assembler.data.AssemblyLine;
import sicxesimulator.software.assembler.data.IntermediateRepresentation;
import sicxesimulator.data.Symbol;
import sicxesimulator.data.SymbolTable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssemblerFirstPassTest {

    @Test
    void testProcessBasicStart() {
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        List<String> code = List.of(
                "PROG    START  0x100",
                "ALPHA   WORD   5",
                "BETA    RESW   1",
                "GAMMA   RESB   2",
                "        END    PROG"
        );

        IntermediateRepresentation ir = firstPass.process(code, code);
        assertNotNull(ir, "IR não deve ser nulo");
        assertEquals(0x100, ir.startAddress(), "START address deve ser 0x100");
        assertEquals("PROG", ir.programName(), "Nome do programa deve ser PROG");

        SymbolTable symTab = ir.symbolTable();
        // Verifica endereços atribuídos
        Symbol alpha = symTab.getSymbolInfo("ALPHA");
        assertEquals(0x100, alpha.address, "ALPHA deve estar no endereço 0x100");
        Symbol beta = symTab.getSymbolInfo("BETA");
        // WORD => 3 bytes; logo, BETA deve vir depois de 3 bytes
        assertEquals(0x103, beta.address, "BETA deve estar em 0x103 (após ALPHA)");
        Symbol gamma = symTab.getSymbolInfo("GAMMA");
        // RESW 1 => reserva 3 bytes; GAMA deve estar depois de 3 bytes de BETA
        assertEquals(0x106, gamma.address, "GAMMA deve estar em 0x106 (após BETA)");

        // RESB 2 => avança 2 bytes, total final = 0x108
        List<AssemblyLine> lines = ir.assemblyLines();
        // Checamos se a última instrução (END) não gera AssemblyLine
        // Então devemos ter 3 AssemblyLine gerados (uma para ALPHA, BETA, GAMMA)
        assertEquals(3, lines.size(), "Deve haver 3 AssemblyLine (sem contar START e END)");
    }

    @Test
    void testProcessWithoutEnd() {
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        List<String> code = List.of(
                "PROG    START   100",
                "ALPHA   WORD    5"
                // sem END
        );

        assertThrows(IllegalArgumentException.class, () ->
                        firstPass.process(code, code),
                "Deve lançar exceção pois não encontrou END"
        );
    }

    @Test
    void testReswAndResbMultiple() {
        // Verifica se múltiplas diretivas de reserva avançam corretamente o LC
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        List<String> code = List.of(
                "PROG START 0",
                "ONE  RESW 2",    // reserva 2*3=6 bytes
                "TWO  RESB 5",    // reserva +5 bytes
                "        END PROG"
        );
        IntermediateRepresentation ir = firstPass.process(code, code);
        SymbolTable st = ir.symbolTable();
        assertEquals(0, st.getSymbolAddress("PROG"), "PROG label deve ser 0 se START 0");
        assertEquals(0, st.getSymbolAddress("ONE"),  "ONE começa em 0");
        // ONE => 6 bytes => TWO=6
        assertEquals(6, st.getSymbolAddress("TWO"),  "TWO deve ser 6");
        // Tamanho final (6 + 5=11)
        // AssemblyLine => 2 lines (ONE, TWO)
        assertEquals(2, ir.assemblyLines().size());
    }

    @Test
    void testExtdefExtref() {
        // Verifica se EXTDEF e EXTREF são processados corretamente
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        List<String> code = List.of(
                "MYPROG  START  0x200",
                "        EXTDEF ALFA, BETA",
                "        EXTREF OMEGA",
                "ALFA    WORD   123",
                "        END    MYPROG"
        );
        IntermediateRepresentation ir = firstPass.process(code, code);
        SymbolTable st = ir.symbolTable();
        // ALFA e BETA devem estar marcados como símbolos públicos
        assertTrue(st.contains("ALFA"), "ALFA deve existir");
        assertTrue(st.getSymbolInfo("ALFA").isPublic, "ALFA deve ser public");
        // BETA declarado via EXTDEF, mas sem rótulo real => deve ser address=0
        assertTrue(st.contains("BETA"), "BETA deve existir");
        assertTrue(st.getSymbolInfo("BETA").isPublic, "BETA deve ser public");
        // OMEGA => importado
        assertEquals(1, ir.importedSymbols().size(), "Deve ter 1 símbolo importado");
        assertTrue(ir.importedSymbols().contains("OMEGA"), "OMEGA deve constar em importedSymbols");
        // ALFA com address=0x200
        assertEquals(0x200, st.getSymbolAddress("ALFA"));
    }
}
