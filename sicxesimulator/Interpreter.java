package sicxesimulator;

import java.util.List;

/**
 * Classe placeholder do Interpretador.
 * Responsável por processar e executar as instruções do arquivo de montagem.
 */
public class Interpreter {

    private List<Instruction> instructions;
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
    public Interpreter(List<Instruction> instructions, Memory memory, Register register) {
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
        if (programCounter >= instructions.size()) {
            System.out.println("Fim da execução.");
            return "done";
        } else {
            Instruction instruction = instructions.get(programCounter);
            System.out.println("Executando: " + instruction.getName());
            executeInstruction(instruction);
            programCounter++;
            return null;
        }
    }



    /**
     * Decodifica e executa uma instrução específica.
     *
     * @param instruction A instrução a ser processada.
     */
    private void executeInstruction(Instruction instruction) {
        String name = instruction.getName(); // Nome da operação.
        String[] args = instruction.getArgs(); // Argumentos da operação.

        switch (name) {
            case "ADD":
                add(args);
                break;

            case "ADDR":
                addr(args);
                break;

            case "AND":
                and(args);
                break;

            case "CLEAR":
                clear(args);
                break;

            case "LDX":
                ldx(args);
                break;

            case "COMP":
                comp(args);
                break;

            case "COMPR":
                compr(args);
                break;

            case "DIV":
                div(args);
                break;

            case "DIVR":
                divr(args);
                break;

            case "J":
                j(args);
                break;

            case "JEQ":
                jeq(args);
                break;

            case "JGT":
                jgt(args);
                break;

            case "JLT":
                jlt(args);
                break;

            case "JSUB":
                jsub(args);
                break;

            case "LDA":
                lda(args);
                break;

            case "LDB":
                ldb(args);
                break;

            case "LDCH":
                ldch(args);
                break;

            case "LDL":
                ldl(args);
                break;

            case "LDS":
                lds(args);
                break;

            case "LDT":
                ldt(args);
                break;

            case "MUL":
                mul(args);
                break;

            case "MULR":
                mulr(args);
                break;

            case "OR":
                or(args);
                break;

            case "RMO":
                rmo(args);
                break;

            case "RSUB":
                rsub(args);
                break;

            case "SHIFTL":
                shiftl(args);
                break;

            case "SHIFTR":
                shiftr(args);
                break;

            case "STA":
                sta(args);
                break;

            case "STB":
                stb(args);
                break;

            case "STCH":
                stch(args);
                break;

            case "STL":
                stl(args);
                break;

            case "STS":
                sts(args);
                break;

            case "STT":
                stt(args);
                break;

            case "STX":
                stx(args);
                break;

            case "SUB":
                sub(args);
                break;

            case "SUBR":
                subr(args);
                break;

            case "TIX":
                tix(args);
                break;

            case "TIXR":
                tixr(args);
                break;

            default:
                System.out.println("Instrução desconhecida: " + name);
                break;
        }
    }

    /**
     * ADD: A ← (A) + (m..m+2)
     * Soma o conteúdo da memória (no endereço especificado em args[0]) com o conteúdo do registrador A.
     */
    public void add(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: ADD requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);
        int regA = Integer.parseInt(register.getRegister("A"), 16);
        int result = (regA + memValue) & 0xFFFFFF;
        register.setRegister("A", String.format("%06X", result));
        System.out.println("ADD: A = " + String.format("%06X", result));
    }

    /**
     * ADDR: r2 ← (r2) + (r1)
     * Soma o conteúdo de dois registradores. Os nomes dos registradores devem estar em args[0] e args[1].
     */
    public void addr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: ADDR requer dois argumentos.");
            return;
        }
        String r1 = args[0];
        String r2 = args[1];
        int value1 = Integer.parseInt(register.getRegister(r1), 16);
        int value2 = Integer.parseInt(register.getRegister(r2), 16);
        int result = (value2 + value1) & 0xFFFFFF;
        register.setRegister(r2, String.format("%06X", result));
        System.out.println("ADDR: " + r2 + " = " + String.format("%06X", result));
    }

    /**
     * AND: A ← (A) & (m..m+2)
     * Realiza a operação AND bit a bit entre o conteúdo do registrador A e o conteúdo da memória.
     */
    public void and(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: AND requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);
        int regA = Integer.parseInt(register.getRegister("A"), 16);
        int result = regA & memValue;
        register.setRegister("A", String.format("%06X", result));
        System.out.println("AND: A = " + String.format("%06X", result));
    }

    /**
     * CLEAR: r1 ← 0
     * Zera o registrador especificado em args[0].
     */
    public void clear(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: CLEAR requer um argumento.");
            return;
        }
        String reg = args[0];
        // Se o registrador for F (48 bits), usamos 12 zeros; caso contrário, 6 zeros.
        String zeroValue = reg.equals("F") ? "000000000000" : "000000";
        register.setRegister(reg, zeroValue);
        System.out.println("CLEAR: " + reg + " set to " + zeroValue);
    }

    /**
     * LDA: A ← (m..m+2)
     * Carrega o registrador A com o valor da memória no endereço especificado.
     */
    public void lda(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDA requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int value = memory.read(address);
        register.setRegister("A", String.format("%06X", value));
        System.out.println("LDA: A = " + String.format("%06X", value));
    }

    /**
     * STA: m..m+2 ← (A)
     * Armazena o conteúdo do registrador A na memória no endereço especificado.
     */
    public void sta(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STA requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        String value = register.getRegister("A");
        memory.setMemory(address, new Word(value));
        System.out.println("STA: Mem[" + address + "] = " + value);
    }

    /**
     * SUB: A ← (A) - (m..m+2)
     * Subtrai o valor da memória (no endereço especificado) do valor do registrador A.
     */
    public void sub(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: SUB requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);
        int regA = Integer.parseInt(register.getRegister("A"), 16);
        int result = (regA - memValue) & 0xFFFFFF;
        register.setRegister("A", String.format("%06X", result));
        System.out.println("SUB: A = " + String.format("%06X", result));
    }

    /**
     * J: PC ← m
     * Salto incondicional para o endereço especificado.
     * Ajusta o contador de programa (programCounter) para o endereço (menos 1, pois será incrementado posteriormente).
     */
    public void j(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: J requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        this.programCounter = address - 1; // Ajusta para que, após o incremento, o PC seja 'address'
        System.out.println("J: Jump para " + address);
    }


    // TODO PUTA MERDA QUANTA COISA
    public void ldx(String[] args) { System.out.println("LDX: Não implementado ainda."); }
    public void comp(String[] args) { System.out.println("COMP: Não implementado ainda."); }
    public void compr(String[] args) { System.out.println("COMPR: Não implementado ainda."); }
    public void div(String[] args) { System.out.println("DIV: Não implementado ainda."); }
    public void divr(String[] args) { System.out.println("DIVR: Não implementado ainda."); }
    public void jeq(String[] args) { System.out.println("JEQ: Não implementado ainda."); }
    public void jgt(String[] args) { System.out.println("JGT: Não implementado ainda."); }
    public void jlt(String[] args) { System.out.println("JLT: Não implementado ainda."); }
    public void jsub(String[] args) { System.out.println("JSUB: Não implementado ainda."); }
    public void ldb(String[] args) { System.out.println("LDB: Não implementado ainda."); }
    public void ldch(String[] args) { System.out.println("LDCH: Não implementado ainda."); }
    public void ldl(String[] args) { System.out.println("LDL: Não implementado ainda."); }
    public void lds(String[] args) { System.out.println("LDS: Não implementado ainda."); }
    public void ldt(String[] args) { System.out.println("LDT: Não implementado ainda."); }
    public void mul(String[] args) { System.out.println("MUL: Não implementado ainda."); }
    public void mulr(String[] args) { System.out.println("MULR: Não implementado ainda."); }
    public void or(String[] args) { System.out.println("OR: Não implementado ainda."); }
    public void rmo(String[] args) { System.out.println("RMO: Não implementado ainda."); }
    public void rsub(String[] args) { System.out.println("RSUB: Não implementado ainda."); }
    public void shiftl(String[] args) { System.out.println("SHIFTL: Não implementado ainda."); }
    public void shiftr(String[] args) { System.out.println("SHIFTR: Não implementado ainda."); }
    public void stb(String[] args) { System.out.println("STB: Não implementado ainda."); }
    public void stch(String[] args) { System.out.println("STCH: Não implementado ainda."); }
    public void stl(String[] args) { System.out.println("STL: Não implementado ainda."); }
    public void sts(String[] args) { System.out.println("STS: Não implementado ainda."); }
    public void stt(String[] args) { System.out.println("STT: Não implementado ainda."); }
    public void stx(String[] args) { System.out.println("STX: Não implementado ainda."); }
    public void subr(String[] args) { System.out.println("SUBR: Não implementado ainda."); }
    public void tix(String[] args) { System.out.println("TIX: Não implementado ainda."); }
    public void tixr(String[] args) { System.out.println("TIXR: Não implementado ainda."); }
}