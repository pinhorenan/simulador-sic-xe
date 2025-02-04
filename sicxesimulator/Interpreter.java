package sicxesimulator;

import java.util.List;

/**
 * Classe placeholder do Interpretador.
 * Responsável por processar e executar as instruções do arquivo de montagem.
 */
public class Interpreter {

    private List<Instructions> instructions;
    private Memory memory;
    private Register register;
    private int programCounter;

    /**
     * Construtor do interpretador.
     *
     * @param instructions Array de instruções.
     * @param memory Instância da memória.
     * @param register Instância dos registradores.
     */
    public Interpreter(List<Instructions> instructions, Memory memory, Register register) {
        this.instructions = instructions;
        this.memory = memory;
        this.register = register;
        this.programCounter = 0;
    }

    /**
     * Configura o endereço inicial para execução das instruções.
     * O interpretador pode configurar o program counter no registrador correspondente.
     */
    public void setAddress() {
        this.programCounter = 0; // Reinicia a execução do programa.
    }

    /**
     * Executa a próxima instrução;
     * (Implementação futura)
     *
     * @return Uma string que indica se a execução foi concluída ou null caso contrário.
     */
    public String runNextInstruction() {
        if (programCounter < instructions.size()) {
            System.out.println("Fim da execução.");
            return "done";
        }

        Instructions instruction = instructions.get(programCounter);
        System.out.println("Executando: instrução número " + programCounter);

        // Decodifica e executa a instrução.
        executeInstruction(instruction);

        // Avança para a próxima instrução.
        programCounter++;
        return null;
    }

    /**
     * Decodifica e executa uma instrução específica.
     *
     * @param instruction A instrução a ser processada.
     */
    private void executeInstruction(Instructions instruction) {
        String opcode = instruction.getName(); // Nome da operação.
        String[] args = instruction.getArgs(); // Argumentos da operação.

        switch (opcode) {
            case "18": // ADD

                break;

            case "90": // ADDR
                break;

            case "40": // AND
                break;

            case "4": // CLEAR
                break;

            case "28": // COMP
                break;

            case "A0": // COMPR
                break;

            case "24": // DIV
                break;

            case "9C": // DIVR
                break;

            case "3C": // J
                break;

            case "30": // JEQ
                break;

            case "34": // JGT
                break;

            case "38": // JLT
                break;

            case "48": // JSUB
                break;

            case "0": // LDA
                break;

            case "68": // LDB
                break;

            case "50": // LDCH
                break;

            case "8": // LDL
                break;

            case "6C": // LDS
                break;

            case "74": // LDT
                break;

            case "4": // LDX é o mesmo OPCODE que CLEAR, precisamos fazer uma distinção conforme o tipo de instrução.
                break;

            case "20": // MUL
                break;

            case "98": // MULR
                break;

            case "44": // OR
                break;

            case "AC": // RMO
                break;

            case "4C": // RSUB
                break;

            case "A4": // SHIFTL
                break;

            case "A8": // SHIFTR
                break;

            case "0C": // STA
                break;

            case "78": // STB
                break;

            case "54": // STCH
                break;

            case "14": // STL
                break;

            case "7C": // STS
                break;

            case "84": // STT
                break;

            case "10": // STX
                break;

            case "1C": // SUB
                break;

            case "94": // SUBR
                break;

            case "2C": // TIX
                break;

            case "B8": // TIXR
                break;;

            default:
                System.out.println("Instrução desconhecida: " + opcode);
                break;
        }
    }

    /**
     * Carrega um valor da memória para o registrador A.
     *
     * @param args Argumentos da instrução.
     */
    private void loadAccumulator(String[] args) {
        if (args.length == 0) {
            System.out.println("Erro: LDA requer um argumento.");
            return;
        }

        int address = Integer.parseInt(args[0]);
        String value = memory.getMemory().get(address).toString(); // Lendo a memória.
        register.setRegister("A", value); // Armazena no registrador A.
        System.out.println("LDA: Carregado " + value + " de " + address);
    }


}
