package sicxesimulator;

/**
 * Classe placeholder do Interpretador.
 * Responsável por processar e executar as instruções do arquivo de montagem.
 */
public class Interpreter {

    private String[] instructions;
    private Memory memory;
    private Register register;

    /**
     * Construtor do interpretador.
     *
     * @param instructions Array de instruções.
     * @param memory Instância da memória.
     * @param register Instância dos registradores.
     */
    public Interpreter(String[] instructions, Memory memory, Register register) {
        this.instructions = instructions;
        this.memory = memory;
        this.register = register;
    }

    /**
     * Configura o endereço inicial para execução das instruções.
     * (Implementação futura)
     */
    public void setAddress() {
        // TODO
    }

    /**
     * Executa a próxima instrução;
     * (Implementação futura)
     *
     * @return Uma string que indica se a execução foi concluída ou null caso contrário.
     */
    public String runNextInstruction() {
        // TODO
        return null;
    }
}
