package sicxesimulator.simulation.virtualMachine;

/**
 * Essa classe deverá lidar com a operação do simulador, ela é a classe "Máquina" e ela que contém Memória, Conjunto de Registradores, e ela que instanciará e coordenará os componentes atuantes.
 */
public class Machine {
    private Memory memory;
    private Register[] registerSet; // TODO usar uma estrutura que faça algum sentido.

    public Machine() {
        memory = new Memory();
        registerSet = new Register[16]; // TODO Temporário

        // TODO Instanciar todos os registradores com valor zerado e adicionar ao registerSet
    }

    ///  Getters

    public Memory getMemory() {
        return memory;
    }

    public Register[] getRegisterSet() {
        return registerSet;
    }

    /// Setters

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public void setRegisterSet(Register[] register) {
        this.registerSet = register;
        // TODO ainda devemos estruturar o conjunto de registradores de forma adequada.
    }
}
