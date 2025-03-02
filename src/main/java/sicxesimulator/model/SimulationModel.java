package sicxesimulator.model;

import sicxesimulator.machine.Machine;
import sicxesimulator.assembler.Assembler;
import sicxesimulator.loader.Loader;

import java.io.IOException;
import java.util.List;

public class SimulationModel {
    private final Machine machine;
    private final Assembler assembler;
    private final Loader loader;

    // Armazena o último código objeto montado e informações relacionadas
    private byte[] objectCode;
    private int startAddress;
    private int programLength;

    public SimulationModel(Machine machine, Assembler assembler, Loader loader) {
        this.machine = machine;
        this.assembler = assembler;
        this.loader = loader;
    }

    /**
     * Monta e carrega um programa na máquina.
     * Gera o código objeto (como array de bytes) a partir do código fonte e carrega na memória.
     *
     * @param sourceLines Lista contendo o código assembly fonte.
     * @throws IOException Se ocorrer erro durante a montagem ou carregamento.
     */
    public void assembleAndLoadProgram(List<String> sourceLines) throws IOException {
        // Gera o código objeto diretamente a partir do código fonte.
        objectCode = assembler.assemble(sourceLines);
        // Obtém o endereço de início (definido via diretiva START no código assembly)
        startAddress = assembler.getStartAddress();
        // Armazena o tamanho do programa para um eventual mecanismo de término
        programLength = objectCode.length;
        // Carrega o programa na memória a partir do endereço de início
        machine.loadProgram(startAddress, objectCode);
    }

    /**
     * Executa um ciclo de instrução (fetch-decode-execute).
     */
    public void runNextInstruction() {
        machine.runCycle();
    }

    /**
     * Indica se a execução foi concluída.
     * Nesta versão, como não há um mecanismo de término automático, retorna sempre false.
     * Para uma implementação futura, você pode verificar se o PC ultrapassou o endereço final do programa.
     *
     * Exemplo:
     * return machine.getControlUnit().getProgramCounter() >= (startAddress + programLength);
     *
     * @return true se a execução estiver concluída, false caso contrário.
     */
    public boolean isFinished() {
        // Stub – ajuste conforme a lógica de término do seu simulador.
        return false;
    }

    /**
     * Reseta o estado da simulação.
     * Limpa o estado da máquina, do assembler e do loader, além de limpar o código objeto carregado.
     */
    public void reset() {
        machine.reset();
        assembler.reset();
        objectCode = null;
        startAddress = 0;
        programLength = 0;
    }

    public Machine getMachine() {
        return machine;
    }

    /**
     * Retorna o código objeto gerado.
     * @return Array de bytes com o código objeto.
     */
    public byte[] getObjectCode() {
        return objectCode;
    }

    /**
     * Retorna o Assembler utilizado.
     * @return Instância do Assembler.
     */
    public Assembler getAssembler() {
        return assembler;
    }

    /**
     * Retorna o endereço de início do programa.
     * @return Endereço inicial.
     */
    public int getStartAddress() {
        return startAddress;
    }

    /**
     * Retorna o tamanho do código objeto carregado.
     * @return Tamanho do programa.
     */
    public int getProgramLength() {
        return programLength;
    }
}
