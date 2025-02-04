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
     * @param memory       Instância da memória.
     * @param register     Instância dos registradores.
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
     * @param startAddress Endereço inicial do programa
     */

    public void setAddress ( int startAddress){
        this.programCounter = 0; //reinicia a execução do programa.
        this.programCounter = startAddress;
    }


    /**
     * Atualiza as flags de condição baseado no resultado de uma operação
     *
     * @param result Resultado da operação
     */
    private void updateFlags(int result) {
        String flags = "00000000"; //valor padrão

        //verifica se é negativo (bit mais significativo = 1)
        if ((result & 0x800000) != 0) {
            flags = "00001000"; //flag N
        }
        //verifica se é zero
        else if (result == 0) {
            flags = "00000001"; //flag Z
        }
        //se não é negativo nem zero, então é positivo
        else {
            flags = "00000100"; //flag P
        }

        register.setRegister("SW", flags);
    }

    /**
     * Executa a próxima instrução;
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
                rsub();
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

        updateFlags(result);

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

        updateFlags(result);

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

        updateFlags(result);
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

        // DEBUG!!!!!!
        System.out.println("Lendo da memória: Mem[" + address + "] = " + String.format("%06X", value)); // Debug

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

        updateFlags(result);
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

    /**
     * LDX: X ← (m..m+2)
     * Carrega o registrador X com o valor da memória no endereço especificado.
     */
    public void ldx(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDX requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int value = memory.read(address);
        register.setRegister("X", String.format("%06X", value));
        System.out.println("LDX: X = " + String.format("%06X", value));
    }

    /**
     * COMP: CC ← (A) - (m..m+2)
     * Compara o conteúdo do registrador A com o valor da memória no endereço especificado.
     */
    public void comp(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: COMP requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);
        int regA = Integer.parseInt(register.getRegister("A"), 16);
        int result = regA - memValue;

        // Ajuste dos bits de condição (CC) conforme o resultado
        if (result == 0) {
            // CC = 0 (igual)
            register.setRegister("SW", "00000001");  // Representando CC = 0 (igual)
            System.out.println("COMP: CC ajustado para igual (SW = 00000001).");
        } else if (result < 0) {
            // CC = 1 (menor)
            register.setRegister("SW", "00000010");  // Representando CC = 1 (menor)
            System.out.println("COMP: CC ajustado para menor (SW = 00000010).");
        } else {
            // CC = 2 (maior)
            register.setRegister("SW", "00000011");  // Representando CC = 2 (maior)
            System.out.println("COMP: CC ajustado para maior (SW = 00000011).");
        }
    }

    /**
     * COMPR: CC ← (r1) - (r2)
     * Compara o conteúdo de dois registradores e ajusta os bits de condição.
     */
    public void compr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: COMPR requer dois argumentos.");
            return;
        }
        String r1 = args[0];
        String r2 = args[1];
        int value1 = Integer.parseInt(register.getRegister(r1), 16);
        int value2 = Integer.parseInt(register.getRegister(r2), 16);
        int result = value1 - value2;

        // Ajuste dos bits de condição (CC) conforme o resultado
        if (result == 0) {
            // CC = 0 (igual)
            register.setRegister("SW", "00000001");  // Representando CC = 0 (igual)
            System.out.println("COMPR: CC ajustado para igual (SW = 00000001).");
        } else if (result < 0) {
            // CC = 1 (menor)
            register.setRegister("SW", "00000010");  // Representando CC = 1 (menor)
            System.out.println("COMPR: CC ajustado para menor (SW = 00000010).");
        } else {
            // CC = 2 (maior)
            register.setRegister("SW", "00000011");  // Representando CC = 2 (maior)
            System.out.println("COMPR: CC ajustado para maior (SW = 00000011).");
        }
    }


    /**
     * DIV: A ← (A) / (m..m+2)
     * Divide o conteúdo do registrador A pelo valor da memória no endereço especificado.
     */
    public void div(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: DIV requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);
        int regA = Integer.parseInt(register.getRegister("A"), 16);
        if (memValue == 0) {
            System.out.println("Erro: Divisão por zero.");
            return;
        }
        int result = regA / memValue;

        updateFlags(result);
        register.setRegister("A", String.format("%06X", result));
        System.out.println("DIV: A = " + String.format("%06X", result));
    }

    /**
     * DIVR: r2 ← (r2) / (r1)
     * Divide o conteúdo de dois registradores e armazena o resultado no segundo registrador.
     */
    public void divr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: DIVR requer dois argumentos.");
            return;
        }
        String r1 = args[0];
        String r2 = args[1];
        int value1 = Integer.parseInt(register.getRegister(r1), 16);
        int value2 = Integer.parseInt(register.getRegister(r2), 16);
        if (value1 == 0) {
            System.out.println("Erro: Divisão por zero.");
            return;
        }
        int result = value2 / value1;

        updateFlags(result);
        register.setRegister(r2, String.format("%06X", result));
        System.out.println("DIVR: " + r2 + " = " + String.format("%06X", result));
    }

    /**
     * JEQ: Se A == 0, PC ← m
     * Realiza um salto condicional para o endereço especificado se o conteúdo do registrador A for zero.
     */
    public void jeq(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: JEQ requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);

        // Verifica a flag Z (Zero) no SW
        int swValue = Integer.parseInt(register.getRegister("SW"), 16);
        boolean zeroFlag = (swValue & 0b0001) != 0;  // Flag Z (Zero) é o bit 0

        if (zeroFlag) {
            this.programCounter = address - 1;  // Salta para o endereço especificado
            System.out.println("JEQ: Salto para " + address);
        } else {
            System.out.println("JEQ: Não ocorreu salto, SW = " + Integer.toBinaryString(swValue));
        }
    }


    /**
     * JGT: Se o resultado da comparação for 'maior' (CC == 2), PC ← m
     * Realiza um salto condicional para o endereço especificado se o resultado da última comparação indicar que o valor comparado é maior.
     */
    public void jgt(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: JGT requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);

        // Obtém o valor do registrador SW (Condition Code)
        int swValue = Integer.parseInt(register.getRegister("SW"), 16);

        // Verifica se a condição de maior foi atendida
        // CC = 2 é quando a flag P está setada e N e Z estão zerados
        boolean greaterThan = ((swValue & 0b0100) != 0) &&  // Flag P (positivo) deve estar 1
                ((swValue & 0b1000) == 0) &&  // Flag N (negativo) deve estar 0
                ((swValue & 0b0001) == 0);    // Flag Z (zero) deve estar 0

        if (greaterThan) {
            this.programCounter = address - 1;  // Ajusta o program counter para o endereço de salto
            System.out.println("JGT: Condição satisfeita (maior). Salto para " + address);
        } else {
            System.out.println("JGT: Condição não satisfeita. Sem salto.");
        }
    }


    /**
     * JLT: Se o resultado da comparação for 'menor' (CC == 1), PC ← m
     * Realiza um salto condicional para o endereço especificado se o resultado da última comparação indicar que o valor comparado é menor.
     */
    public void jlt(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: JLT requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);

        // Obtém o valor do registrador SW (Condition Code)
        int swValue = Integer.parseInt(register.getRegister("SW"), 16);

        // Verifica se a condição de menor foi atendida
        // CC = 1 é quando a flag N está setada e P e Z estão zerados
        boolean lessThan = ((swValue & 0b1000) != 0) &&  // Flag N (negativo) deve estar 1
                ((swValue & 0b0100) == 0) &&  // Flag P (positivo) deve estar 0
                ((swValue & 0b0001) == 0);    // Flag Z (zero) deve estar 0

        if (lessThan) {
            this.programCounter = address - 1;  // Ajusta o program counter para o endereço de salto
            System.out.println("JLT: Condição satisfeita (menor). Salto para " + address);
        } else {
            System.out.println("JLT: Condição não satisfeita. Sem salto.");
        }
    }


    /**
     * JSUB: L ← PC+1; PC ← m
     * Salta para o endereço especificado, salvando o endereço de retorno (a instrução seguinte) no registrador L.
     */
    public void jsub(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: JSUB requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        // Armazena o endereço de retorno no registrador L
        register.setRegister("L", String.format("%06X", this.programCounter + 1));
        this.programCounter = address - 1;  // Salta para o endereço especificado
        System.out.println("JSUB: Salto para " + address + " com retorno armazenado em L = " + register.getRegister("L"));
    }


    /**
     * LDB: B ← (m..m+2)
     * Carrega o registrador B com o valor da memória no endereço especificado.
     */
    public void ldb(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDB requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int value = memory.read(address);
        register.setRegister("B", String.format("%06X", value));
        System.out.println("LDB: B = " + String.format("%06X", value));
    }


    /**
     * LDCH: Carrega um byte de memória no registrador A.
     * O byte lido substitui os 8 bits menos significativos de A, preservando os 16 bits mais significativos.
     */
    public void ldch(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDCH requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);  // Lê um valor de 24 bits da memória
        int memByte = memValue & 0xFF;         // Extrai os 8 bits menos significativos (1 byte)
        int regA = Integer.parseInt(register.getRegister("A"), 16);
        int newValue = (regA & 0xFFFF00) | memByte;  // Preserva os 16 bits mais significativos
        register.setRegister("A", String.format("%06X", newValue));
        System.out.println("LDCH: A = " + String.format("%06X", newValue));
    }


    /**
     * LDL: L ← (m..m+2)
     * Carrega o registrador L com o valor da memória no endereço especificado.
     */
    public void ldl(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDL requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int value = memory.read(address);
        register.setRegister("L", String.format("%06X", value));
        System.out.println("LDL: L = " + String.format("%06X", value));
    }


    /**
     * LDS: S ← (m..m+2)
     * Carrega o registrador S com o valor da memória no endereço especificado.
     */
    public void lds(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDS requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int value = memory.read(address);
        register.setRegister("S", String.format("%06X", value));
        System.out.println("LDS: S = " + String.format("%06X", value));
    }


    /**
     * LDT: T ← (m..m+2)
     * Carrega o registrador T com o valor da memória no endereço especificado.
     */
    public void ldt(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: LDT requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int value = memory.read(address);
        register.setRegister("T", String.format("%06X", value));
        System.out.println("LDT: T = " + String.format("%06X", value));
    }

    /**
     * MUL: A ← (A) * (m..m+2)
     * Multiplica o conteúdo do registrador A pelo valor da memória no endereço especificado.
     */
    public void mul(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: MUL requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);
        int regA = Integer.parseInt(register.getRegister("A"), 16);
        int result = (regA * memValue) & 0xFFFFFF;

        updateFlags(result);
        register.setRegister("A", String.format("%06X", result));
        System.out.println("MUL: A = " + String.format("%06X", result));
    }

    /**
     * MULR: r2 ← (r2) * (r1)
     * Multiplica o conteúdo de dois registradores e armazena o resultado no segundo registrador.
     */
    public void mulr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: MULR requer dois argumentos.");
            return;
        }
        String r1 = args[0];
        String r2 = args[1];
        int value1 = Integer.parseInt(register.getRegister(r1), 16);
        int value2 = Integer.parseInt(register.getRegister(r2), 16);
        int result = (value2 * value1) & 0xFFFFFF;

        updateFlags(result);
        register.setRegister(r2, String.format("%06X", result));
        System.out.println("MULR: " + r2 + " = " + String.format("%06X", result));
    }

    /**
     * OR: A ← (A) | (m..m+2)
     * Realiza a operação OR bit a bit entre o conteúdo do registrador A e o valor da memória.
     */
    public void or(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: OR requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);
        int regA = Integer.parseInt(register.getRegister("A"), 16);
        int result = regA | memValue;

        updateFlags(result);
        register.setRegister("A", String.format("%06X", result));
        System.out.println("OR: A = " + String.format("%06X", result));
    }

    /**
     * RMO: r2 ← (r1)
     * Move (cópia) o conteúdo do registrador r1 para o registrador r2.
     */
    public void rmo(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: RMO requer dois argumentos.");
            return;
        }
        String source = args[0];
        String dest = args[1];
        String value = register.getRegister(source);
        register.setRegister(dest, value);
        System.out.println("RMO: " + dest + " = " + value + " (copiado de " + source + ")");
    }

    /**
     * RSUB: PC ← L
     * Retorna da sub-rotina, ajustando o contador de programa para o endereço armazenado em L.
     */
    public void rsub() {
        String lValue = register.getRegister("L");
        int returnAddress = Integer.parseInt(lValue, 16);
        this.programCounter = returnAddress - 1; // Ajusta para que, após o incremento, PC seja o endereço de retorno
        System.out.println("RSUB: Retorno para " + returnAddress);
    }

    /**
     * SHIFTL: r ← (r) << n
     * Realiza um deslocamento lógico à esquerda no registrador especificado por n bits.
     */
    public void shiftl(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: SHIFTL requer dois argumentos.");
            return;
        }
        String reg = args[0];
        int shiftCount = Integer.parseInt(args[1]);

        if (reg.equals("F")) {
            long value = Long.parseLong(register.getRegister(reg), 16);
            long result = (value << shiftCount) & 0xFFFFFFFFFFFFL;
            register.setRegister(reg, String.format("%012X", result));
            updateFlags((int) (result & 0xFFFFFF)); //atualiza flags usando os 24 bits menos significativos
            System.out.println("SHIFTL: " + reg + " = " + String.format("%012X", result));
        } else {
            int value = Integer.parseInt(register.getRegister(reg), 16);
            int result = (value << shiftCount) & 0xFFFFFF;
            updateFlags(result);
            register.setRegister(reg, String.format("%06X", result));
            System.out.println("SHIFTL: " + reg + " = " + String.format("%06X", result));
        }
    }

    /**
     * SHIFTR: r ← (r) >> n
     * Realiza um deslocamento lógico à direita no registrador especificado por n bits.
     */
    public void shiftr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: SHIFTR requer dois argumentos.");
            return;
        }
        String reg = args[0];
        int shiftCount = Integer.parseInt(args[1]);

        if (reg.equals("F")) {
            long value = Long.parseLong(register.getRegister(reg), 16);
            long result = (value >> shiftCount) & 0xFFFFFFFFFFFFL;
            register.setRegister(reg, String.format("%012X", result));
            updateFlags((int) (result & 0xFFFFFF)); //atualiza flags usando os 24 bits menos significativos
            System.out.println("SHIFTR: " + reg + " = " + String.format("%012X", result));
        } else {
            int value = Integer.parseInt(register.getRegister(reg), 16);
            int result = (value >> shiftCount) & 0xFFFFFF;
            updateFlags(result);
            register.setRegister(reg, String.format("%06X", result));
            System.out.println("SHIFTR: " + reg + " = " + String.format("%06X", result));
        }
    }

    /**
     * STB: m..m+2 ← (B)
     * Armazena o conteúdo do registrador B na memória no endereço especificado.
     */
    public void stb(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STB requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        String value = register.getRegister("B");
        memory.setMemory(address, new Word(value));
        System.out.println("STB: Mem[" + address + "] = " + value);
    }

    /**
     * STCH: m ← (byte menos significativo de A)
     * Armazena o byte (8 bits) menos significativo do registrador A na memória no endereço especificado.
     */
    public void stch(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STCH requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        String regA = register.getRegister("A");
        // Extrai os 2 últimos dígitos hexadecimais (1 byte)
        String byteValue = regA.substring(regA.length() - 2);

        // TODO adicionar método setByte em memory
        memory.setByte(address, byteValue);

        System.out.println("STCH: Mem[" + address + "] = " + byteValue);
    }

    /**
     * STL: m..m+2 ← (L)
     * Armazena o conteúdo do registrador L na memória no endereço especificado.
     */
    public void stl(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STL requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        String value = register.getRegister("L");
        memory.setMemory(address, new Word(value));
        System.out.println("STL: Mem[" + address + "] = " + value);
    }

    /**
     * STS: m..m+2 ← (S)
     * Armazena o conteúdo do registrador S na memória no endereço especificado.
     */
    public void sts(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STS requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        String value = register.getRegister("S");
        memory.setMemory(address, new Word(value));
        System.out.println("STS: Mem[" + address + "] = " + value);
    }

    /**
     * STT: m..m+2 ← (T)
     * Armazena o conteúdo do registrador T na memória no endereço especificado.
     */
    public void stt(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STT requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        String value = register.getRegister("T");
        memory.setMemory(address, new Word(value));
        System.out.println("STT: Mem[" + address + "] = " + value);
    }

    /**
     * STX: m..m+2 ← (X)
     * Armazena o conteúdo do registrador X na memória no endereço especificado.
     */
    public void stx(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: STX requer um argumento.");
            return;
        }
        int address = Integer.parseInt(args[0]);
        String value = register.getRegister("X");
        memory.setMemory(address, new Word(value));
        System.out.println("STX: Mem[" + address + "] = " + value);
    }

    /**
     * SUBR: r2 ← (r2) - (r1)
     * Subtrai o conteúdo de um registrador (r1) do conteúdo de outro registrador (r2).
     */
    public void subr(String[] args) {
        if (args.length < 2) {
            System.out.println("Erro: SUBR requer dois argumentos.");
            return;
        }
        String r1 = args[0];
        String r2 = args[1];
        int value1 = Integer.parseInt(register.getRegister(r1), 16);
        int value2 = Integer.parseInt(register.getRegister(r2), 16);
        int result = (value2 - value1) & 0xFFFFFF; // Mantém o valor dentro de 24 bits.

        updateFlags(result);
        register.setRegister(r2, String.format("%06X", result));
        System.out.println("SUBR: " + r2 + " = " + String.format("%06X", result));
    }

    /**
     * TIX: X ← (X) + 1
     * Se o valor de X não for zero, incrementa o registrador X e compara o resultado com o conteúdo da memória.
     */
    public void tix(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: TIX requer um argumento.");
            return;
        }

        int address = Integer.parseInt(args[0]);
        int memValue = memory.read(address);

        //incrementa X
        int regX = Integer.parseInt(register.getRegister("X"), 16);
        regX = (regX + 1) & 0xFFFFFF;
        register.setRegister("X", String.format("%06X", regX));

        //compara com valor da memória
        int result = regX - memValue;

        //ajusta flags de condição
        if (result == 0) {
            register.setRegister("SW", "00000001");  // igual
        } else if (result < 0) {
            register.setRegister("SW", "00000010");  // menor
        } else {
            register.setRegister("SW", "00000011");  // maior
        }

        System.out.println("TIX: X = " + String.format("%06X", regX));
    }

    /**
     * TIXR: X ← (X) + 1
     * Se X não for zero, faz o salto condicional, comparando X com outro registrador (r1).
     */
    public void tixr(String[] args) {
        if (args.length < 1) {
            System.out.println("Erro: TIXR requer um argumento.");
            return;
        }

        String r1 = args[0];

        //incrementa X
        int regX = Integer.parseInt(register.getRegister("X"), 16);
        regX = (regX + 1) & 0xFFFFFF;
        register.setRegister("X", String.format("%06X", regX));

        //compara com o registrador r1
        int r1Value = Integer.parseInt(register.getRegister(r1), 16);
        int result = regX - r1Value;

        //ajusta flags de condição
        if (result == 0) {
            register.setRegister("SW", "00000001"); //igual
        } else if (result < 0) {
            register.setRegister("SW", "00000010"); //menor
        } else {
            register.setRegister("SW", "00000011"); //maior
        }

        System.out.println("TIXR: X = " + String.format("%06X", regX));
    }
}
