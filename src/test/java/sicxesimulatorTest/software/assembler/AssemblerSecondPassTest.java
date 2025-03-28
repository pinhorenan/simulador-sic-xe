package sicxesimulatorTest.software.assembler;

import org.junit.jupiter.api.Test;
import sicxesimulator.software.assembler.AssemblerSecondPass;
import sicxesimulator.software.assembler.data.AssemblyLine;
import sicxesimulator.software.assembler.data.IntermediateRepresentation;
import sicxesimulator.software.data.ObjectFile;
import sicxesimulator.software.data.SymbolTable;
import sicxesimulator.software.assembler.util.Parser;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;


class AssemblerSecondPassTest {

    // Overload para facilitar os testes: gera código objeto usando um HashSet vazio para importados.
    private AssemblerSecondPass getSecondPass() {
        return new AssemblerSecondPass() {
            // Adiciona overload para geração de código com 2 parâmetros.
            public byte[] generateObjectCode(AssemblyLine line, SymbolTable symbolTable) {
                return generateObjectCode(line, symbolTable, new HashSet<>());
            }
        };
    }

    @Test
    void testGenerateObjectFileBasic() {
        SymbolTable symTab = new SymbolTable();
        symTab.addSymbol("ALPHA", 0x100, false);
        symTab.addSymbol("BETA",  0x103, false);

        List<AssemblyLine> lines = List.of(
                new AssemblyLine("ALPHA", "WORD", "5", 0x100),
                new AssemblyLine("BETA",  "RESW", "1", 0x103)
        );

        IntermediateRepresentation ir = new IntermediateRepresentation(
                lines,
                linesAsString(lines),
                symTab,
                Set.of(),    // imported
                "PROG",
                0x100
        );

        AssemblerSecondPass secondPass = new AssemblerSecondPass();
        ObjectFile objFile = secondPass.generateObjectFile(ir);

        assertNotNull(objFile, "ObjectFile não deve ser nulo");
        assertEquals(0x100, objFile.getStartAddress(), "startAddress deve ser 0x100");
        // ALPHA => WORD (3 bytes), BETA => RESW (3 bytes) => total 6 bytes
        assertEquals(6, objFile.getObjectCode().length, "Código objeto deve ter 6 bytes");
        // Verifica o valor de ALPHA: 5 => 0x000005
        byte[] code = objFile.getObjectCode();
        assertEquals(0x00, code[0]);
        assertEquals(0x00, code[1]);
        assertEquals(0x05, code[2]);
        // BETA => RESW: 3 bytes de zeros
        assertEquals(0x00, code[3]);
        assertEquals(0x00, code[4]);
        assertEquals(0x00, code[5]);
    }

    @Test
    void testGenerateObjectCodeFormat3_NonImmediate() {
        SymbolTable st = new SymbolTable();
        // Testa uma instrução LDA com operando numérico (endereço absoluto)
        AssemblyLine line = new AssemblyLine(null, "LDA", "0x200", 0x100);
        AssemblerSecondPass secondPass = getSecondPass();
        byte[] code = secondPass.generateObjectCode(line, st);

        assertEquals(3, code.length, "Formato 3 deve ter 3 bytes");

        // Cálculo esperado:
        // Opcode para LDA: 0x00, com n=1, i=1 => primeiro byte = 0x03.
        // p = 1, disp = operandAddress - (PC + tamanho) = 0x200 - (0x100+3) = 0x200 - 0x103 = 0xFD.
        // Segundo byte: 0x20 | ((disp >> 8) & 0x0F) = 0x20; terceiro byte: 0xFD.
        assertEquals(0x03, code[0] & 0xFF, "Primeiro byte deve ser 0x03");
        assertEquals(0x20, code[1] & 0xFF, "Segundo byte deve ser 0x20");
        assertEquals(0xFD, code[2] & 0xFF, "Terceiro byte deve ser 0xFD");
    }

    @Test
    void testGenerateObjectCodeFormat3_Immediate() {
        SymbolTable st = new SymbolTable();
        // Testa LDA com operando imediato, ex: LDA #10
        AssemblyLine line = new AssemblyLine(null, "LDA", "#10", 0x100);
        AssemblerSecondPass secondPass = getSecondPass();
        byte[] code = secondPass.generateObjectCode(line, st);

        assertEquals(3, code.length, "Formato 3 deve ter 3 bytes");

        // Para imediato, n=0, i=1, e não se aplica PC-relativo (não seta p)
        // Primeiro byte: (opcode LDA = 0x00 & 0xFC) | (0<<1)|1 = 0x01.
        // Displacement é o literal (10 decimal = 0x0A).
        // Segundo byte: não indexado, não seta p, então apenas (disp >>8) = 0x00; terceiro byte: 0x0A.
        assertEquals(0x01, code[0] & 0xFF, "Primeiro byte deve ser 0x01");
        assertEquals(0x00, code[1] & 0xFF, "Segundo byte deve ser 0x00");
        assertEquals(0x0A, code[2] & 0xFF, "Terceiro byte deve ser 0x0A");
    }

    @Test
    void testGenerateObjectCodeFormat2() {
        // Testa instrução de formato 2: por exemplo, ADDR r1,r2
        SymbolTable st = new SymbolTable();
        // Não importa o endereço para formato 2; usamos RESW para gerar 2 bytes
        AssemblyLine line = new AssemblyLine(null, "ADDR", "A,L", 0x100);
        AssemblerSecondPass secondPass = getSecondPass();
        byte[] code = secondPass.generateObjectCode(line, st);

        // Para ADDR, opcode mapeado (0x90), e os dois registradores: A -> 0, L -> 2
        // Segundo byte: (0 <<4) | 2 = 0x02.
        assertEquals(2, code.length, "Formato 2 deve ter 2 bytes");
        assertEquals(0x90, code[0] & 0xFF, "Primeiro byte deve ser 0x90");
        assertEquals(0x02, code[1] & 0xFF, "Segundo byte deve ser 0x02");
    }

    @Test
    void testGenerateObjectCodeFormat4() {
        SymbolTable st = new SymbolTable();
        // Testa instrução de formato 4, ex: +LDA 0x3000
        AssemblyLine line = new AssemblyLine(null, "+LDA", "0x3000", 0x100);
        AssemblerSecondPass secondPass = getSecondPass();
        byte[] code = secondPass.generateObjectCode(line, st);

        // Para formato 4, esperamos 4 bytes.
        assertEquals(4, code.length, "Formato 4 deve ter 4 bytes");
        // Primeiro byte: (opcode LDA 0x00 &0xFC) | 0x03 = 0x03.
        // Segundo byte: deve ter bit e=1 (0x10) e, se indexado, bit x; aqui não indexado.
        // Os três bytes seguintes representam o endereço de 20 bits (0x3000).
        int operandAddress = Parser.resolveOperandAddress("0x3000", st);
        int expectedSecondByte = 0x10 | ((operandAddress >> 16) & 0x0F);
        assertEquals(0x03, code[0] & 0xFF, "Primeiro byte deve ser 0x03");
        assertEquals(expectedSecondByte, code[1] & 0xFF, "Segundo byte incorreto");
        // Verifica os bytes inferiores do endereço
        assertEquals((operandAddress >> 8) & 0xFF, code[2] & 0xFF, "Terceiro byte incorreto");
        assertEquals(operandAddress & 0xFF, code[3] & 0xFF, "Quarto byte incorreto");
    }

    @Test
    void testGenerateObjectCodeByteDirective() {
        SymbolTable st = new SymbolTable();
        // Teste para BYTE com literal de caracteres: C'ABC'
        AssemblyLine lineC = new AssemblyLine(null, "BYTE", "C'ABC'", 0x100);
        AssemblerSecondPass sp = getSecondPass();
        byte[] codeC = sp.generateObjectCode(lineC, st);
        assertArrayEquals(new byte[]{0x41, 0x42, 0x43}, codeC, "BYTE C'ABC' incorreto");

        // Teste para BYTE com literal hexadecimal: X'FF0A'
        AssemblyLine lineX = new AssemblyLine(null, "BYTE", "X'FF0A'", 0x103);
        byte[] codeX = sp.generateObjectCode(lineX, st);
        assertArrayEquals(new byte[]{(byte)0xFF, (byte)0x0A}, codeX, "BYTE X'FF0A' incorreto");
    }

    @Test
    void testDisplacementOutOfRange() {
        // Testa se o cálculo do deslocamento lança exceção quando fora de intervalo
        SymbolTable st = new SymbolTable();
        // Suponha que o operando tenha um endereço que gere deslocamento maior que 2047 ou menor que −2048
        // Por exemplo, se a instrução estiver em 0x100 e o operando for 0x2000, a diferença será muito grande.
        AssemblyLine line = new AssemblyLine(null, "LDA", "0x2000", 0x100);
        AssemblerSecondPass secondPass = getSecondPass();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> secondPass.generateObjectCode(line, st));
        String expectedMessage = "Deslocamento PC-relativo invalido";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Deveria lançar exceção por deslocamento inválido");
    }

    // Função auxiliar para converter AssemblyLine em List<String> (simula raw source)
    private List<String> linesAsString(List<AssemblyLine> lines) {
        return lines.stream().map(AssemblyLine::toString).toList();
    }
}
