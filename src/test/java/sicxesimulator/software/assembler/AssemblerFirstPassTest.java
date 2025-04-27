package sicxesimulator.software.assembler;

import org.junit.jupiter.api.Test;
import sicxesimulator.software.assembler.data.AssemblyLine;
import sicxesimulator.software.assembler.data.IntermediateRepresentation;
import sicxesimulator.software.data.SymbolTable;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssemblerFirstPassTest {

    @Test
    void testProcessBasicProgram() {
        // Simula um programa simples com START e END, onde a diretiva START não possui rótulo
        List<String> sourceLines = List.of(
                "START  0x100",
                "FIRST   LDA   0x200",
                "        ADD   0x203",
                "        STA   0x206",
                "END     FIRST"
        );
        // Usamos a mesma lista para originalSource e sourceLines
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        IntermediateRepresentation ir = firstPass.process(sourceLines, sourceLines);

        // Como o START não possui rótulo, o nome do programa deve ser null.
        assertNull(ir.programName(), "O nome do programa deve ser null pois START não possui rótulo");

        // Verifica se o endereço inicial é 0x100
        assertEquals(0x100, ir.startAddress(), "Start address deve ser 0x100");

        // Verifica se foram geradas 3 AssemblyLines (excluindo START e END)
        List<AssemblyLine> lines = ir.assemblyLines();
        assertEquals(3, lines.size(), "Deve haver 3 AssemblyLines (LDA, ADD, STA)");

        // Considerando que as instruções LDA, ADD e STA são do formato 3 (3 bytes cada):
        // Espera-se que os endereços sejam 0x100, 0x103 e 0x106, respectivamente.
        assertEquals(0x100, lines.get(0).address(), "Endereço da 1ª instrução deve ser 0x100");
        assertEquals(0x103, lines.get(1).address(), "Endereço da 2ª instrução deve ser 0x103");
        assertEquals(0x106, lines.get(2).address(), "Endereço da 3ª instrução deve ser 0x106");

        // Verifica se a tabela de símbolos contém o rótulo "FIRST" com o endereço correto.
        SymbolTable symTab = ir.symbolTable();
        assertTrue(symTab.contains("FIRST"), "A tabela de símbolos deve conter 'FIRST'");
        assertEquals(0x100, symTab.getSymbolAddress("FIRST"), "O endereço do símbolo 'FIRST' deve ser 0x100");
    }

    @Test
    void testProcessWithCommentsAndExtraWhitespace() {
        // Programa com comentários e espaçamentos variados
        List<String> sourceLines = List.of(
                "   START    200    ; Início do programa",
                "; Esta linha é um comentário completo",
                "LABEL1   LDA   0x300  ; Carrega valor",
                "         ADD   0x303",
                "LABEL2   STA   0x306   ; Armazena valor",
                "         END   LABEL1  ; Fim do programa"
        );
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        IntermediateRepresentation ir = firstPass.process(sourceLines, sourceLines);

        // Como START não possui rótulo, o nome do programa deve ser null.
        assertNull(ir.programName(), "O nome do programa deve ser null pois START não possui rótulo");

        // Espera-se que sejam geradas 3 AssemblyLines (LDA, ADD, STA)
        List<AssemblyLine> lines = ir.assemblyLines();
        assertEquals(3, lines.size(), "Deve haver 3 AssemblyLines");

        // Verifica se os comentários foram removidos das AssemblyLines
        for (AssemblyLine line : lines) {
            assertFalse(line.toString().contains(";"), "A linha não deve conter comentário");
        }
    }

    @Test
    void testProcessWithExtdefAndExtref() {
        // Programa que utiliza as diretivas EXTDEF e EXTREF
        List<String> sourceLines = List.of(
                "START  300",
                "EXTDEF  ALPHA, BETA",
                "EXTREF  GAMMA, DELTA",
                "        LDA   ALPHA",
                "        STA   GAMMA",
                "END"
        );
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        IntermediateRepresentation ir = firstPass.process(sourceLines, sourceLines);

        // Espera-se que apenas as instruções (LDA e STA) sejam geradas como AssemblyLines.
        List<AssemblyLine> lines = ir.assemblyLines();
        assertEquals(2, lines.size(), "Deve haver 2 AssemblyLines");

        // A tabela de símbolos deve conter os símbolos de EXTDEF com endereço 0 (por definição)
        SymbolTable symTab = ir.symbolTable();
        assertTrue(symTab.contains("ALPHA"), "A tabela de símbolos deve conter 'ALPHA'");
        assertTrue(symTab.contains("BETA"), "A tabela de símbolos deve conter 'BETA'");

        // Os símbolos de EXTREF devem estar presentes no conjunto de importados
        Set<String> imported = ir.importedSymbols();
        assertTrue(imported.contains("GAMMA"), "Os símbolos importados devem conter 'GAMMA'");
        assertTrue(imported.contains("DELTA"), "Os símbolos importados devem conter 'DELTA'");
    }

    @Test
    void testMissingEndDirectiveThrowsException() {
        // Programa sem a diretiva END
        List<String> sourceLines = List.of(
                "START  100",
                "LDA   0x200",
                "STA   0x203"
                // Faltando END
        );
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> firstPass.process(sourceLines, sourceLines));
        String expectedMessage = "Diretiva END nao encontrada";
        assertTrue(exception.getMessage().contains(expectedMessage), "Deve lançar exceção por falta de END");
    }

    @Test
    void testInvalidLineThrowsException() {
        // Linha inválida sem mnemônico reconhecido
        List<String> sourceLines = List.of(
                "START 100",
                "INVALID_LINE",
                "END FIRST"
        );
        AssemblerFirstPass firstPass = new AssemblerFirstPass();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> firstPass.process(sourceLines, sourceLines));
        String expectedMessage = "Linha invalida";
        assertTrue(exception.getMessage().contains(expectedMessage), "Deve lançar exceção por linha inválida");
    }
}
