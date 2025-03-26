package sicxesimulatorTest.software.assembler;

import org.junit.jupiter.api.Test;
import sicxesimulator.software.assembler.AssemblerSecondPass;
import sicxesimulator.software.assembler.data.AssemblyLine;
import sicxesimulator.software.assembler.data.IntermediateRepresentation;
import sicxesimulator.data.ObjectFile;
import sicxesimulator.data.SymbolTable;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssemblerSecondPassTest {

    @Test
    void testGenerateObjectFileBasic() {
        // Preparando IR simulando a primeira passagem
        SymbolTable symTab = new SymbolTable();
        symTab.addSymbol("ALPHA", 0x100, false);
        symTab.addSymbol("BETA",  0x103, false);

        List<AssemblyLine> lines = List.of(
                // Removeu-se a linha START, que não gera código objeto
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
        // ALPHA => WORD(3 bytes), BETA => RESW(3 bytes) => total 6
        assertEquals(6, objFile.getObjectCode().length, "Código objeto deve ter 6 bytes");
        // O 1º WORD (ALPHA) deve ter valor 5 => 0x000005
        assertEquals(0x00, objFile.getObjectCode()[0]);
        assertEquals(0x00, objFile.getObjectCode()[1]);
        assertEquals(0x05, objFile.getObjectCode()[2]);
        // BETA => RESW(1) => 3 bytes de zeros
        assertEquals(0x00, objFile.getObjectCode()[3]);
        assertEquals(0x00, objFile.getObjectCode()[4]);
        assertEquals(0x00, objFile.getObjectCode()[5]);
    }


    @Test
    void testGenerateObjectCodeFormat3() {
        SymbolTable st = new SymbolTable();
        AssemblyLine line = new AssemblyLine(null, "LDA", "0x200", 0x100);

        AssemblerSecondPass secondPass = new AssemblerSecondPass();
        byte[] code = secondPass.generateObjectCode(line, st);

        assertEquals(3, code.length, "Formato 3 => 3 bytes");

        // Ajuste feito conforme sua implementação (opcode=0x00, n=1, i=1, p=1)
        assertEquals(0x03, code[0] & 0xFF); // opcode LDA + flags n e i
        assertEquals(0x20, code[1] & 0xFF); // bit p=1 e disp hi bits
        assertEquals(0xFD, code[2] & 0xFF); // disp lo bits (0xFD)
    }


    @Test
    void testGenerateObjectCodeByteDirective() {
        SymbolTable st = new SymbolTable();
        // Exemplo: BYTE C'ABC' => [0x41, 0x42, 0x43]
        AssemblyLine lineC = new AssemblyLine(null,"BYTE","C'ABC'", 0x100);
        AssemblerSecondPass sp = new AssemblerSecondPass();
        byte[] codeC = sp.generateObjectCode(lineC, st);
        assertArrayEquals(new byte[]{0x41, 0x42, 0x43}, codeC);

        // Exemplo: BYTE X'FF0A' => [0xFF, 0x0A]
        AssemblyLine lineX = new AssemblyLine(null,"BYTE","X'FF0A'", 0x103);
        byte[] codeX = sp.generateObjectCode(lineX, st);
        assertArrayEquals(new byte[]{(byte)0xFF, (byte)0x0A}, codeX);
    }

    // Função auxiliar para converter AssemblyLine -> List<String> (simulando rawSource)
    private List<String> linesAsString(List<AssemblyLine> lines) {
        return lines.stream().map(AssemblyLine::toString).toList();
    }
}
