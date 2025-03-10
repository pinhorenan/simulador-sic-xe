package sicxesimulator.linker;

import sicxesimulator.models.ObjectFile;

import java.util.List;

/**
 * A classe Linker operacionaliza o processo de ligação em duas passagens:
 * 1) A primeira passagem (FirstPassLinker) que calcula offsets e constrói a tabela de símbolos global.
 * 2) A segunda passagem (SecondPassLinker) que gera o ObjectFile final linkado, aplicando a relocação completa se requerido.
 * Observação: O loadAddress é informado em bytes.
 */
public class Linker {

    private final LinkerFirstPass firstPass;
    private final LinkerSecondPass secondPass;

    public Linker() {
        firstPass = new LinkerFirstPass();
        secondPass = new LinkerSecondPass();
    }

    /**
     * Realiza a ligação de múltiplos ObjectFile.
     *
     * @param objectFiles    Lista de módulos (ObjectFile) a serem linkados.
     * @param loadAddress    Endereço de carga inicial (em bytes) para o programa linkado.
     * @param fullRelocation Se true, o linker realiza a relocação completa do código objeto;
     *                       caso contrário, a relocação final fica a cargo do Loader.
     * @return               O ObjectFile final resultante da ligação.
     */
    public ObjectFile link(List<ObjectFile> objectFiles, int loadAddress, boolean fullRelocation) {
        // Primeira passagem: calcular offsets, tabela global e identificar o programa.
        firstPass.process(objectFiles, loadAddress);

        // Segunda passagem: gerar o código objeto final utilizando as informações da primeira passagem.

        return secondPass.process(
                objectFiles,
                firstPass.getModuleRelocationOffsets(),
                loadAddress,
                fullRelocation,
                firstPass.getGlobalSymbolTable(),
                firstPass.getProgramName()
        );
    }
}
