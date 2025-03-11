package sicxesimulator.linker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import sicxesimulator.models.ObjectFile;
import sicxesimulator.models.SymbolTable;

import java.util.Arrays;
import java.util.List;

public class LinkerTest {

    /**
     * Testa a ligação sem relocação completa.
     * Nesse cenário, o código objeto final é apenas a concatenação dos códigos originais.
     */
    @Test
    public void testLinkerWithoutFullRelocation() {
        // Configura o primeiro módulo:
        // - startAddress em palavras: 0
        // - Código objeto: {0x00, 0x00, 0x05} (valor 5)
        // - Símbolo "SYM1" em 0 (endereço em palavras)
        SymbolTable st1 = new SymbolTable();
        st1.addSymbol("SYM1", 0);
        byte[] machineCode1 = new byte[]{0x00, 0x00, 0x05};
        ObjectFile module1 = new ObjectFile(0, machineCode1, st1, "MODULE1");

        // Configura o segundo módulo:
        // - startAddress em palavras: 10
        // - Código objeto: {0x00, 0x00, 0x0A} (valor 10)
        // - Símbolo "SYM2" em 2 (endereço em palavras)
        SymbolTable st2 = new SymbolTable();
        st2.addSymbol("SYM2", 2);
        byte[] machineCode2 = new byte[]{0x00, 0x00, 0x0A};
        ObjectFile module2 = new ObjectFile(10, machineCode2, st2, "MODULE2");

        List<ObjectFile> modules = Arrays.asList(module1, module2);

        int loadAddress = 300; // endereço de carga em bytes
        boolean fullRelocation = false; // não realizar relocação completa

        Linker linker = new Linker();
        ObjectFile finalObj = linker.link(modules, loadAddress, fullRelocation);

        // O startAddress final é loadAddress/3 (pois startAddress é em palavras)
        assertEquals(100, finalObj.getStartAddress());

        // Como não há relocação completa, o código objeto final é a concatenação dos códigos originais.
        byte[] expectedMachineCode = new byte[machineCode1.length + machineCode2.length];
        System.arraycopy(machineCode1, 0, expectedMachineCode, 0, machineCode1.length);
        System.arraycopy(machineCode2, 0, expectedMachineCode, machineCode1.length, machineCode2.length);
        assertArrayEquals(expectedMachineCode, finalObj.getMachineCode());

        // O nome do programa final é a concatenação dos nomes dos módulos com "_" (conforme implementação do FirstPassLinker).
        assertEquals("MODULE1_MODULE2_", finalObj.getFilename());

        // Verifica se a tabela de símbolos global foi atualizada corretamente:
        // Para module1:
        //   relocationOffset = loadAddress - (0 * 3) = 300
        //   Novo endereço de "SYM1" = (0*3 + 300) / 3 = 100
        assertEquals(100, finalObj.getSymbolTable().getAddress("SYM1"));

        // Para module2:
        //   cumulativeOffset após module1 = machineCode1.length = 3
        //   moduleOriginalStartBytes = 10 * 3 = 30
        //   moduleLoadAddress = (loadAddress + 3) = 303
        //   relocationOffset = 303 - 30 = 273
        //   Novo endereço de "SYM2" = ((2*3) + 273) / 3 = (6 + 273)/3 = 279/3 = 93
        assertEquals(93, finalObj.getSymbolTable().getAddress("SYM2"));
    }

    /**
     * Testa a ligação com relocação completa.
     * Nesse cenário, o código objeto de cada módulo é ajustado (relocado) antes de ser concatenado.
     */
    @Test
    public void testLinkerWithFullRelocation() {
        // Configura o primeiro módulo:
        SymbolTable st1 = new SymbolTable();
        st1.addSymbol("SYM1", 0);
        byte[] machineCode1 = new byte[]{0x00, 0x00, 0x05}; // representa 5
        ObjectFile module1 = new ObjectFile(0, machineCode1, st1, "MODULE1");

        // Configura o segundo módulo:
        SymbolTable st2 = new SymbolTable();
        st2.addSymbol("SYM2", 2);
        byte[] machineCode2 = new byte[]{0x00, 0x00, 0x0A}; // representa 10
        ObjectFile module2 = new ObjectFile(10, machineCode2, st2, "MODULE2");

        List<ObjectFile> modules = Arrays.asList(module1, module2);

        int loadAddress = 300; // em bytes
        boolean fullRelocation = true;

        Linker linker = new Linker();
        ObjectFile finalObj = linker.link(modules, loadAddress, fullRelocation);

        // Verifica startAddress final (em palavras)
        assertEquals(100, finalObj.getStartAddress());

        // Para o module1:
        //   relocationOffset = 300 - (0 * 3) = 300.
        //   Valor original = 5, valor relocacionado = 5 + 300 = 305.
        //   305 em 3 bytes: 0x00, 0x01, 0x31.
        byte[] relocatedModule1 = new byte[]{0x00, 0x01, 0x31};

        // Para o module2:
        //   cumulativeOffset após module1 = machineCode1.length = 3.
        //   moduleOriginalStartBytes = 10*3 = 30.
        //   moduleLoadAddress = 300 + 3 = 303.
        //   relocationOffset = 303 - 30 = 273.
        //   Valor original = 10, valor relocacionado = 10 + 273 = 283.
        //   283 em 3 bytes: 0x00, 0x01, 0x1B.
        byte[] relocatedModule2 = new byte[]{0x00, 0x01, 0x1B};

        byte[] expectedMachineCode = new byte[6];
        System.arraycopy(relocatedModule1, 0, expectedMachineCode, 0, 3);
        System.arraycopy(relocatedModule2, 0, expectedMachineCode, 3, 3);
        assertArrayEquals(expectedMachineCode, finalObj.getMachineCode());

        // Verifica o nome concatenado dos módulos.
        assertEquals("MODULE1_MODULE2_", finalObj.getFilename());

        // Verifica a tabela de símbolos global:
        // Os valores relocacionados devem ser os mesmos que no teste anterior.
        assertEquals(100, finalObj.getSymbolTable().getAddress("SYM1"));
        assertEquals(93, finalObj.getSymbolTable().getAddress("SYM2"));
    }
}
