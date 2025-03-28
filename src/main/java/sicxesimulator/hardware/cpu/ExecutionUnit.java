package sicxesimulator.hardware.cpu;

import sicxesimulator.hardware.Memory;
import sicxesimulator.utils.Convert;
import sicxesimulator.utils.Mapper;

import java.util.logging.Logger;

/**
 * Representa a Unidade de Execução (Execution Unit) da arquitetura SIC/XE.
 *
 * <p>Responsável por interpretar e executar as instruções do programa,
 * manipulando os registradores, acessando a memória e atualizando a palavra de condição (SW).</p>
 *
 * <p>Esta classe implementa o núcleo da lógica operacional da CPU simulada,
 * incluindo suporte a modos de endereçamento, instruções de carga/armazenamento,
 * aritmética, lógica, controle de fluxo, entre outras.</p>
 */
public class ExecutionUnit {
    private final RegisterSet registers;
    private final Memory memory;

    private static final Logger logger = Logger.getLogger(ExecutionUnit.class.getName());

    public ExecutionUnit(RegisterSet registers, Memory memory) {
        this.registers = registers;
        this.memory = memory;
    }

    /**
     * Converte um endereço de memória em bytes para um endereço de palavra (3 bytes).
     *
     * <p>Valida se o endereço efetivo está alinhado com o tamanho da palavra do SIC/XE,
     * lançando uma exceção se não estiver.</p>
     *
     * @param effectiveAddress Endereço efetivo em bytes.
     * @return Endereço correspondente em palavras (índice de palavra).
     * @throws IllegalArgumentException Se o endereço não for múltiplo de 3 (não alinhado).
     */
    private int toWordAddress(int effectiveAddress) {
        if (effectiveAddress % 3 != 0) {
            throw new IllegalArgumentException("Endereço efetivo não alinhado: " + effectiveAddress);
        }
        return effectiveAddress / 3;
    }

    /**
     * Retorna o valor do operando com base no modo de endereçamento (imediato, indireto ou direto).
     *
     * <p>Interpreta os bits n e i dos operandos para decidir como acessar o valor:</p>
     * <ul>
     *   <li><b>Imediato</b> (n=0, i=1): retorna o próprio endereço efetivo.</li>
     *   <li><b>Indireto</b> (n=1, i=0): acessa duas vezes a memória para resolver o valor final.</li>
     *   <li><b>Direto</b> (n=1, i=1): acessa diretamente a memória.</li>
     * </ul>
     *
     * @param operands Vetor contendo os bits n e i nas posições 5 e 6, respectivamente.
     * @param effectiveAddress Endereço efetivo calculado para a instrução.
     * @return Valor resolvido do operando.
     */
    private int getValueOrImmediate(int[] operands, int effectiveAddress) {
        int n = operands[5], i = operands[6];

        if (n == 0 && i == 1) {  // imediato
            return effectiveAddress;
        } else if (n == 1 && i == 0) {  // indireto
            int addrFromMem = Convert.bytesToInt(memory.readWord(toWordAddress(effectiveAddress)));
            return Convert.bytesToInt(memory.readWord(toWordAddress(addrFromMem)));
        } else {  // direto padrão SIC/XE (n=1,i=1) ou SIC/nãoXE (n=0,i=0)
            return Convert.bytesToInt(memory.readWord(toWordAddress(effectiveAddress)));
        }
    }

    /**
     * Atualiza o registrador SW (Status Word) com base no resultado de uma operação de comparação.
     *
     * <p>Define os seguintes códigos de condição:</p>
     * <ul>
     *   <li>0 → Igual</li>
     *   <li>1 → Menor</li>
     *   <li>2 → Maior</li>
     * </ul>
     *
     * @param value Resultado da operação de comparação.
     */
    private void updateConditionCode(int value) {
        int cc = (value == 0) ? 0 : (value < 0 ? 1 : 2);
        registers.getRegister("SW").setValue(cc);
    }

    /**
     * Retorna uma descrição textual do código de condição armazenado em SW.
     *
     * <p>Mapeia os valores numéricos de SW para texto interpretável:</p>
     * <ul>
     *   <li>0 → "Igual"</li>
     *   <li>1 → "Menor"</li>
     *   <li>2 → "Maior"</li>
     *   <li>Outro → "Desconhecido"</li>
     * </ul>
     *
     * @return Descrição legível do código de condição.
     */
    private String getConditionCodeDescription() {
        return switch (registers.getRegister("SW").getIntValue()) {
            case 0 -> "Igual";
            case 1 -> "Menor";
            case 2 -> "Maior";
            default -> "Desconhecido";
        };
    }

    /**
     * Executa a instrução ADD (adição inteira).
     *
     * <p>Soma o valor especificado (imediato, direto ou indireto) ao acumulador (registrador A).</p>
     * <p>Atualiza a palavra de condição (SW) com base no resultado.</p>
     *
     * @param operands Operandos da instrução.
     * @param indexed Indica se o endereço é indexado (não afeta diretamente aqui).
     * @param effectiveAddress Endereço efetivo ou valor imediato.
     * @return Log formatado com os valores originais e o resultado.
     */
    public String executeADD(int[] operands, boolean indexed, int effectiveAddress) {
        Register A = registers.getRegister("A");
        int aBefore = A.getIntValue();
        int operandValue = getValueOrImmediate(operands, effectiveAddress);
        int result = aBefore + operandValue;

        A.setValue(result);
        updateConditionCode(result);

        String log = String.format("ADD: A=%06X + %06X => %06X", aBefore, operandValue, result);
        logger.info(log);
        return log;
    }


    /**
     * Executa a instrução ADDR (adição entre registradores).
     *
     * <p>Soma o conteúdo do primeiro registrador ao segundo registrador.</p>
     * <p>Atualiza a palavra de condição (SW).</p>
     *
     * @param operands Operandos indicando os registradores.
     * @return Log formatado exibindo os registradores envolvidos e o resultado da soma.
     */
    public String executeADDR(int[] operands) {
        Register reg1 = Mapper.getRegisterByNumber(operands[0], registers);
        Register reg2 = Mapper.getRegisterByNumber(operands[1], registers);
        int result = reg1.getIntValue() + reg2.getIntValue();
        reg2.setValue(result);
        updateConditionCode(result);
        String log = String.format("ADDR: %s + %s => %06X",
                reg1.getName(), reg2.getName(), result);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução SUB (subtração inteira).
     *
     * <p>Subtrai o valor especificado (imediato, direto ou indireto) do acumulador (registrador A).</p>
     * <p>Atualiza a palavra de condição (SW) com base no resultado.</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Endereço efetivo ou valor imediato.
     * @return Log detalhado com os valores originais e o resultado.
     */
    public String executeSUB(int[] operands, int effectiveAddress) {
        Register A = registers.getRegister("A");
        int aBefore = A.getIntValue();
        int operandValue = getValueOrImmediate(operands, effectiveAddress);
        int result = aBefore - operandValue;

        A.setValue(result);
        updateConditionCode(result);

        String log = String.format("SUB: A=%06X - %06X => %06X", aBefore, operandValue, result);
        logger.info(log);
        return log;
    }


    /**
     * Executa a instrução SUBR (subtração entre registradores).
     *
     * <p>Subtrai o valor do primeiro registrador fornecido do segundo registrador.</p>
     * <p>Atualiza a palavra de condição (SW).</p>
     *
     * @param operands Registradores envolvidos na subtração.
     * @return Log detalhado com os registradores e resultado.
     */
    public String executeSUBR(int[] operands) {
        Register r1 = Mapper.getRegisterByNumber(operands[0], registers);
        Register r2 = Mapper.getRegisterByNumber(operands[1], registers);
        int result = r2.getIntValue() - r1.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);
        String log = String.format("SUBR: %s - %s => %06X",
                r2.getName(), r1.getName(), result);

        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução MUL (multiplicação inteira).
     *
     * <p>Multiplica o acumulador (registrador A) pelo valor especificado (imediato, direto ou indireto).</p>
     * <p>Atualiza a palavra de condição (SW).</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Endereço efetivo ou valor imediato.
     * @return Log detalhado com valores antes e depois da multiplicação.
     */
    public String executeMUL(int[] operands, int effectiveAddress) {
        Register A = registers.getRegister("A");
        int aBefore = A.getIntValue();
        int operandValue = getValueOrImmediate(operands, effectiveAddress);
        int result = aBefore * operandValue;

        A.setValue(result);
        updateConditionCode(result);

        String log = String.format("MUL: A=%06X * %06X => %06X", aBefore, operandValue, result);
        logger.info(log);
        return log;
    }


    /**
     * Executa a instrução MULR (multiplicação entre registradores).
     *
     * <p>Multiplica o conteúdo do primeiro registrador com o segundo registrador.</p>
     * <p>Atualiza a palavra de condição (SW).</p>
     *
     * @param operands Registradores envolvidos na multiplicação.
     * @return Log detalhado da multiplicação e resultado.
     */
    public String executeMULR(int[] operands) {
        Register r1 = Mapper.getRegisterByNumber(operands[0], registers);
        Register r2 = Mapper.getRegisterByNumber(operands[1], registers);
        int result = r1.getIntValue() * r2.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);
        String log = String.format("MULR: %s * %s => %06X",
                r1.getName(), r2.getName(), result);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução DIV (divisão inteira).
     *
     * <p>Divide o acumulador pelo valor especificado (imediato, direto ou indireto).</p>
     * <p>Atualiza a palavra de condição (SW).</p>
     *
     * @param operands Operandos da instrução (usados para detectar modo de endereçamento).
     * @param effectiveAddress Valor imediato ou endereço efetivo, conforme o modo de endereçamento.
     * @return Log detalhado da operação.
     * @throws ArithmeticException Se tentar dividir por zero.
     */
    public String executeDIV(int[] operands, int effectiveAddress) {
        Register A = registers.getRegister("A");
        int divisor = getValueOrImmediate(operands, effectiveAddress);
        if (divisor == 0) {
            throw new ArithmeticException("Divisão por zero");
        }
        int result = A.getIntValue() / divisor;
        A.setValue(result);
        updateConditionCode(result);
        String log = String.format("DIV: A=%06X / %06X => %06X",
                (result * divisor), divisor, result);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução DIVR (divisão entre registradores).
     *
     * <p>Divide o segundo registrador pelo primeiro.</p>
     * <p>Atualiza a palavra de condição (SW).</p>
     *
     * @throws ArithmeticException Divisão por zero.
     */
    public String executeDIVR(int[] operands) {
        Register r1 = Mapper.getRegisterByNumber(operands[0], registers);
        Register r2 = Mapper.getRegisterByNumber(operands[1], registers);
        if (r1.getIntValue() == 0) {
            throw new ArithmeticException("Divisão por zero");
        }
        int result = r2.getIntValue() / r1.getIntValue();
        r2.setValue(result);
        updateConditionCode(result);
        String log = String.format("DIVR: %s / %s => %06X",
                r2.getName(), r1.getName(), result);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução AND (operação lógica bit a bit).
     *
     * <p>Realiza uma operação AND bit a bit entre o acumulador e o valor especificado (imediato ou memória). </p>
     * <p>O resultado é armazenado no acumulador. Atualiza a palavra de condição (SW) com base no resultado.</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Endereço efetivo ou valor imediato.
     * @return Log detalhado mostrando valores antes e depois da operação.
     */
    public String executeAND(int[] operands, int effectiveAddress) {
        Register A = registers.getRegister("A");
        int originalA = A.getIntValue();
        int operandValue = getValueOrImmediate(operands, effectiveAddress);
        int result = originalA & operandValue;
        A.setValue(result);
        updateConditionCode(result);
        String log = String.format("AND: A=%06X & %06X => %06X", originalA, operandValue, result);
        logger.info(log);
        return log;

    }

    /**
     * Executa a instrução OR (operação lógica bit a bit).
     *
     * <p>Realiza uma operação OR bit a bit entre o acumulador (registrador A) e o valor especificado
     * (imediato, direto ou indireto).</p>
     * <p>O resultado é armazenado no acumulador. Atualiza a palavra de condição (SW) com base no resultado.</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Endereço efetivo ou valor imediato.
     * @return Log detalhado mostrando os valores antes e depois da operação.
     */
    public String executeOR(int[] operands, int effectiveAddress) {
        Register A = registers.getRegister("A");
        int aBefore = A.getIntValue();
        int operandValue = getValueOrImmediate(operands, effectiveAddress);
        int result = aBefore | operandValue;

        A.setValue(result);
        updateConditionCode(result);

        String log = String.format("OR: A=%06X | %06X => %06X", aBefore, operandValue, result);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução LDA (Load Accumulator).
     *
     * <p>Carrega o acumulador (registrador A) com o valor especificado,
     * seja imediato, direto ou indireto.</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log descrevendo o valor carregado no acumulador.
     */
    public String executeLDA(int[] operands, int effectiveAddress) {
        int value = getValueOrImmediate(operands, effectiveAddress);
        registers.getRegister("A").setValue(value);

        String log = String.format("LDA: A ← %06X", value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução LDB (Load Register B).
     *
     * <p>Carrega o registrador B com o valor especificado
     * (imediato, direto ou indireto).</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log descrevendo a operação.
     */
    public String executeLDB(int[] operands, int effectiveAddress) {
        int value = getValueOrImmediate(operands, effectiveAddress);
        registers.getRegister("B").setValue(value);

        String log = String.format("LDB: B ← %06X", value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução LDCH (Load Character).
     *
     * <p>Carrega o byte mais à direita do registrador A com o valor especificado (imediato, direto ou indireto).</p>
     * <p>Nos modos indireto e direto, o byte é lido da memória. No modo imediato, é carregado diretamente do operando.</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log descrevendo o byte carregado.
     */
    public String executeLDCH(int[] operands, int effectiveAddress) {
        int n = operands[5], i = operands[6];
        int byteValue;

        if (n == 0 && i == 1) { // Imediato
            byteValue = effectiveAddress & 0xFF;
        } else { // Direto ou indireto
            byteValue = memory.readByte(effectiveAddress);
        }

        Register A = registers.getRegister("A");
        int originalA = A.getIntValue();
        int newA = (originalA & 0xFFFF00) | (byteValue & 0xFF);
        A.setValue(newA);

        String log = String.format("LDCH: A[byte] ← %02X", byteValue);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução LDL (Load Register L).
     *
     * <p>Carrega o registrador L com valor especificado (imediato, direto ou indireto).</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log descrevendo a operação realizada.
     */
    public String executeLDL(int[] operands, int effectiveAddress) {
        int value = getValueOrImmediate(operands, effectiveAddress);
        registers.getRegister("L").setValue(value);

        String log = String.format("LDL: L ← %06X", value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução LDS (Load Register S).
     *
     * <p>Carrega o registrador S com valor especificado (imediato, direto ou indireto).</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log descrevendo a operação.
     */
    public String executeLDS(int[] operands, int effectiveAddress) {
        int value = getValueOrImmediate(operands, effectiveAddress);
        registers.getRegister("S").setValue(value);

        String log = String.format("LDS: S ← %06X", value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução LDT (Load Register T).
     *
     * <p>Carrega o registrador T com valor especificado (imediato, direto ou indireto).</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log descrevendo a operação.
     */
    public String executeLDT(int[] operands, int effectiveAddress) {
        int value = getValueOrImmediate(operands, effectiveAddress);
        registers.getRegister("T").setValue(value);

        String log = String.format("LDT: T ← %06X", value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução LDX (Load Index).
     *
     * <p>Carrega o registrador X com o valor especificado (imediato, direto ou indireto).</p>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log indicando o valor carregado no registrador X.
     */
    public String executeLDX(int[] operands, int effectiveAddress) {
        int value = getValueOrImmediate(operands, effectiveAddress);
        registers.getRegister("X").setValue(value);

        String log = String.format("LDX: X ← %06X", value);
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (LDF - Load Floating Point).
     * <p>Operações envolvendo ponto flutuante não são suportadas conforme especificações do projeto.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeLDF() {
        String log = "LDF: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução STA (Store Accumulator).
     *
     * <p>Armazena o conteúdo do registrador A no endereço efetivo especificado na memória.</p>
     *
     * @param effectiveAddress Endereço efetivo de destino na memória.
     * @return Log detalhado da operação realizada.
     */
    public String executeSTA(int effectiveAddress) {
        int value = registers.getRegister("A").getIntValue();
        memory.writeWord(toWordAddress(effectiveAddress), Convert.intTo3Bytes(value));
        String log = String.format("STA: Mem[%06X] ← %06X", effectiveAddress, value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução STB (Store Register B).
     *
     * <p>Armazena o conteúdo do registrador B no endereço efetivo especificado na memória.</p>
     *
     * @param effectiveAddress Endereço efetivo de destino na memória.
     * @return Log detalhado da operação realizada.
     */
    public String executeSTB(int effectiveAddress) {
        int value = registers.getRegister("B").getIntValue();
        memory.writeWord(toWordAddress(effectiveAddress), Convert.intTo3Bytes(value));
        String log = String.format("STB: Mem[%06X] ← %06X", effectiveAddress, value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução STCH (Store Character).
     *
     * <p>Armazena o byte menos significativo (mais à direita) do registrador A diretamente no endereço especificado na memória.</p>
     *
     * @param effectiveAddress Endereço efetivo de destino na memória (em bytes).
     * @return Log detalhado da operação realizada.
     */
    public String executeSTCH(int effectiveAddress) {
        int byteValue = registers.getRegister("A").getIntValue() & 0xFF;
        // Para acesso de byte, escrevemos diretamente no endereço em bytes.
        memory.writeByte(effectiveAddress, byteValue);
        String log = String.format("STCH: Mem[%06X] ← %02X", effectiveAddress, byteValue);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução STL (Store Register L).
     *
     * <p>Armazena o conteúdo do registrador L no endereço efetivo especificado na memória.</p>
     *
     * @param effectiveAddress Endereço efetivo de destino na memória.
     * @return Log detalhado da operação realizada.
     */
    public String executeSTL(int effectiveAddress) {
        int value = registers.getRegister("L").getIntValue();
        memory.writeWord(toWordAddress(effectiveAddress), Convert.intTo3Bytes(value));
        String log = String.format("STL: Mem[%06X] ← %06X", effectiveAddress, value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução STS (Store Register S).
     *
     * <p>Armazena o conteúdo do registrador S no endereço efetivo especificado na memória.</p>
     *
     * @param effectiveAddress Endereço efetivo de destino na memória.
     * @return Log detalhado da operação realizada.
     */
    public String executeSTS(int effectiveAddress) {
        int value = registers.getRegister("S").getIntValue();
        memory.writeWord(toWordAddress(effectiveAddress), Convert.intTo3Bytes(value));
        String log = String.format("STS: Mem[%06X] ← %06X", effectiveAddress, value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução STT (Store Register T).
     *
     * <p>Armazena o conteúdo do registrador T no endereço efetivo especificado na memória.</p>
     *
     * @param effectiveAddress Endereço efetivo de destino na memória.
     * @return Log detalhado da operação realizada.
     */
    public String executeSTT(int effectiveAddress) {
        int value = registers.getRegister("T").getIntValue();
        memory.writeWord(toWordAddress(effectiveAddress), Convert.intTo3Bytes(value));
        String log = String.format("STT: Mem[%06X] ← %06X", effectiveAddress, value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução STX (Store Register X).
     *
     * <p>Armazena o conteúdo do registrador X no endereço efetivo especificado na memória.</p>
     *
     * @param effectiveAddress Endereço efetivo de destino na memória.
     * @return Log detalhado da operação realizada.
     */
    public String executeSTX(int effectiveAddress) {
        int value = registers.getRegister("X").getIntValue();
        memory.writeWord(toWordAddress(effectiveAddress), Convert.intTo3Bytes(value));
        String log = String.format("STX: Mem[%06X] ← %06X", effectiveAddress, value);
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (STF - Store Floating Point).
     *
     * <p>Operações envolvendo ponto flutuante não são suportadas conforme especificações do projeto.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeSTF() {
        String log = "STF: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (STI - Store to Device).
     *
     * <p>Operações envolvendo dispositivos de entrada/saída não são suportadas conforme especificações do projeto.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeSTI() {
        String log = "STI: Operação de armazenamento para dispositivo não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (STSW - Store Status Word).
     *
     * <p>A operação de armazenamento da palavra de condição não é suportada conforme especificações do projeto.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeSTSW() {
        String log = "STSW: Operação não implementada conforme especificação.";
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução J (Jump).
     *
     * <p>Altera o fluxo do programa, transferindo o controle diretamente para o endereço efetivo fornecido.</p>
     *
     * @param effectiveAddress Endereço efetivo para o salto.
     * @return Log descrevendo o endereço para onde o controle foi transferido.
     */
    public String executeJ(int effectiveAddress) {
        registers.getRegister("PC").setValue(effectiveAddress);
        String log = String.format("J: PC ← %06X", effectiveAddress);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução JEQ (Jump if Equal).
     *
     * <p>Realiza um salto para o endereço efetivo caso o último resultado armazenado em SW indique igualdade (SW=0).</p>
     *
     * @param effectiveAddress Endereço efetivo para o salto condicional.
     * @return Log descrevendo se o salto foi realizado ou não.
     */
    public String executeJEQ(int effectiveAddress) {
        if (registers.getRegister("SW").getIntValue() == 0) {
            registers.getRegister("PC").setValue(effectiveAddress);
            String log = String.format("JEQ: PC ← %06X (Jump realizado)", effectiveAddress);
            logger.info(log);
            return log;
        } else {
            String log = "JEQ: Condição não satisfeita (sem jump)";
            logger.info(log);
            return log;
        }
    }

    /**
     * Executa a instrução JGT (Jump if Greater Than).
     *
     * <p>Realiza um salto para o endereço efetivo caso o último resultado armazenado em SW indique que foi maior que zero (SW>0).</p>
     *
     * @param effectiveAddress Endereço efetivo para o salto condicional.
     * @return Log descrevendo se o salto foi realizado ou não.
     */
    public String executeJGT(int effectiveAddress) {
        if (registers.getRegister("SW").getIntValue() == 2) {
            registers.getRegister("PC").setValue(effectiveAddress);
            String log = String.format("JGT: PC ← %06X (Jump realizado)", effectiveAddress);
            logger.info(log);
            return log;
        } else {
            String log = "JGT: Condição não satisfeita (sem jump)";
            logger.info(log);
            return log;
        }
    }

    /**
     * Executa a instrução JLT (Jump if Less Than).
     *
     * <p>Realiza um salto para o endereço efetivo caso o último resultado armazenado em SW indique que foi menor que zero (SW<0).</p>
     *
     * @param effectiveAddress Endereço efetivo para o salto condicional.
     * @return Log descrevendo se o salto foi realizado ou não.
     */
    public String executeJLT(int effectiveAddress) {
        if (registers.getRegister("SW").getIntValue() == 1) {
            registers.getRegister("PC").setValue(effectiveAddress);
            String log = String.format("JLT: PC ← %06X (Jump realizado)", effectiveAddress);
            logger.info(log);
            return log;
        } else {
            String log = "JLT: Condição não satisfeita (sem jump)";
            logger.info(log);
            return log;
        }
    }

    /**
     * Executa a instrução JSUB (Jump to Subroutine).
     *
     * <p>Armazena o endereço atual (PC) no registrador L e realiza um salto para o endereço efetivo indicado.</p>
     *
     * @param effectiveAddress Endereço efetivo da subrotina.
     * @return Log indicando o endereço armazenado em L e o novo PC.
     */
    public String executeJSUB(int effectiveAddress) {
        int returnAddress = registers.getRegister("PC").getIntValue();
        registers.getRegister("L").setValue(returnAddress);
        registers.getRegister("PC").setValue(effectiveAddress);
        String log = String.format("JSUB: L ← %06X, PC ← %06X", effectiveAddress, returnAddress);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução RSUB (Return from Subroutine).
     *
     * <p>Retorna o fluxo do programa para o endereço armazenado no registrador L.</p>
     *
     * @return Log descrevendo o retorno ao endereço contido em L.
     */
    public String executeRSUB() {
        int returnAddress = registers.getRegister("L").getIntValue();
        if (returnAddress == 0) {
            registers.getRegister("PC").setValue(0);
            String log = "RSUB: Encerrando execução (HALT).";
            logger.info(log);
            return log;
        }
        registers.getRegister("PC").setValue(returnAddress);
        String log = String.format("RSUB: PC ← L (%06X)", returnAddress);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução COMP (Compare Accumulator).
     *
     * <p>Compara o conteúdo do acumulador (registrador A) com o valor especificado (imediato, direto ou indireto).</p>
     * <p>Atualiza o registrador SW com:</p>
     * <ul>
     *   <li>SW = 0 se A = valor</li>
     *   <li>SW = 1 se A &lt; valor</li>
     *   <li>SW = 2 se A &gt; valor</li>
     * </ul>
     *
     * @param operands Operandos da instrução.
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log descrevendo a comparação e o resultado no SW.
     */
    public String executeCOMP(int[] operands, int effectiveAddress) {
        Register A = registers.getRegister("A");
        int aBefore = A.getIntValue();
        int operandValue = getValueOrImmediate(operands, effectiveAddress);
        int comparison = aBefore - operandValue;

        updateConditionCode(comparison);

        String log = String.format("COMP: A (%06X) comparado com %06X (SW=%s)",
                aBefore, operandValue, getConditionCodeDescription());
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução COMPR (Compare Registers).
     *
     * <p>Compara os valores armazenados em dois registradores.</p>
     * <p>Atualiza o registrador SW com:
     * <ul>
     *   <li>SW = -1 se reg1 < reg2</li>
     *   <li>SW =  0 se reg1 = reg2</li>
     *   <li>SW =  1 se reg1 > reg2</li>
     * </ul></p>
     *
     * @param operands Operandos indicando os registradores envolvidos.
     * @return Log descrevendo a comparação e o resultado em SW.
     */
    public String executeCOMPR(int[] operands) {
        Register r1 = Mapper.getRegisterByNumber(operands[0], registers);
        Register r2 = Mapper.getRegisterByNumber(operands[1], registers);
        int comparison = r1.getIntValue() - r2.getIntValue();
        updateConditionCode(comparison);
        String log = String.format("COMPR: %s (%06X) comparado com %s (%06X) (SW=%s)",
                r1.getName(), r1.getIntValue(), r2.getName(), r2.getIntValue(), getConditionCodeDescription());

        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução TIX (Test and Increment Index).
     *
     * <p>Incrementa o registrador X em 1, depois compara com o valor especificado.</p>
     * <p>Atualiza SW com base na comparação:</p>
     * <ul>
     *   <li>SW = -1 se X < valor</li>
     *   <li>SW =  0 se X = valor</li>
     *   <li>SW =  1 se X > valor</li>
     * </ul>
     *
     * @param effectiveAddress Valor imediato ou endereço efetivo.
     * @return Log descrevendo o novo valor de X e o resultado no SW.
     */
    public String executeTIX(int effectiveAddress) {
        Register X = registers.getRegister("X");
        X.setValue(X.getIntValue() + 1);
        byte[] wordBytes = memory.readWord(toWordAddress(effectiveAddress));
        int memValue = Convert.bytesToInt(wordBytes);
        int comparison = X.getIntValue() - memValue;
        updateConditionCode(comparison);
        String log = String.format("TIX: X incrementado para %06X e comparado com %06X (SW=%s)",
                X.getIntValue(), memValue, getConditionCodeDescription());
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução TIXR (Test and Increment Index by Register).
     *
     * <p>Incrementa o registrador X em 1, depois compara com o valor do registrador especificado.</p>
     * <p>Atualiza SW com base na comparação:</p>
     * <ul>
     *   <li>SW = -1 se X < reg</li>
     *   <li>SW =  0 se X = reg</li>
     *   <li>SW =  1 se X > reg</li>
     * </ul>
     *
     * @param operands Operandos indicando o registrador envolvido.
     * @return Log descrevendo o novo valor de X, registrador comparado e resultado no SW.
     */
    public String executeTIXR(int[] operands) {
        Register X = registers.getRegister("X");
        X.setValue(X.getIntValue() + 1);
        Register r = Mapper.getRegisterByNumber(operands[0], registers);
        int comparison = X.getIntValue() - r.getIntValue();
        updateConditionCode(comparison);
        String log = String.format("TIXR: X incrementado para %06X e comparado com %s (%06X) (SW=%s)",
                X.getIntValue(), r.getName(), r.getIntValue(), getConditionCodeDescription());
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução CLEAR (limpa um registrador).
     *
     * <p>Zera o conteúdo do registrador especificado. Este métodO é invocado
     * quando a instrução possui apenas 1 operando (indicando CLEAR).</p>
     *
     * @param operand Número que identifica o registrador a ser zerado.
     * @return Log indicando o registrador zerado.
     */
    public String executeCLEAR(int operand) {
        Register reg = Mapper.getRegisterByNumber(operand, registers);
        reg.setValue(0);
        String log = String.format("CLEAR: %s zerado", reg.getName());
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução SHIFTL (Shift Left).
     *
     * <p>Realiza o deslocamento lógico à esquerda do valor do registrador especificado pelo número de posições indicadas.</p>
     * <p>Atualiza o registrador SW conforme o resultado.</p>
     *
     * @param operands Vetor contendo [registrador, número de posições].
     * @return Log detalhando registrador, deslocamento e resultado.
     */
    public String executeSHIFTL(int[] operands) {
        Register reg = Mapper.getRegisterByNumber(operands[0], registers);
        int count = operands[1];
        int value = reg.getIntValue() << count;
        reg.setValue(value);
        updateConditionCode(value);
        String log = String.format("SHIFTL: R%d << %d = %06X", operands[0], count, value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução SHIFTR (Shift Right).
     *
     * <p>Realiza o deslocamento lógico à direita do valor do registrador especificado pelo número de posições indicadas.</p>
     * <p>Atualiza o registrador SW conforme o resultado.</p>
     *
     * @param operands Vetor contendo [registrador, número de posições].
     * @return Log detalhando registrador, deslocamento e resultado.
     */
    public String executeSHIFTR(int[] operands) {
        Register reg = Mapper.getRegisterByNumber(operands[0], registers);
        int count = operands[1];
        int value = reg.getIntValue() >>> count; // Deslocamento lógico
        reg.setValue(value);
        updateConditionCode(value);
        String log = String.format("SHIFTR: R%d >> %d = %06X", operands[0], count, value);
        logger.info(log);
        return log;
    }

    /**
     * Executa a instrução RMO (Register Move).
     *
     * <p>Transfere o conteúdo do primeiro registrador especificado para o segundo.</p>
     *
     * @param operands Vetor contendo [registrador Origem, registrador Destino].
     * @return Log descrevendo a transferência entre os registradores.
     */
    public String executeRMO(int[] operands) {
        Register source = Mapper.getRegisterByNumber(operands[0], registers);
        Register dest = Mapper.getRegisterByNumber(operands[1], registers);
        dest.setValue(source.getIntValue());
        String log = String.format("RMO: R%d → R%d | Valor = %06X", operands[0], operands[1], source.getIntValue());
        logger.info(log);
        return log;
    }

    ///  Instruções Não Implementadas (STUBS I/O e especiais)

    /**
     * <b>[STUB]</b> Não implementado (LPS - Load Processor Status).
     *
     * <p>A instrução LPS deveria carregar o status do processador a partir de um endereço de memória
     * ou registrador para configurar estados internos como modo de execução ou permissões.</p>
     *
     * <p>Este tipo de operação de controle interno da CPU não é suportado pelo simulador,
     * conforme definido nas especificações do projeto.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeLPS() {
        String log = "LPS: Operação não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (SIO - Start Input/Output).
     *
     * <p>A instrução SIO deveria iniciar uma operação de entrada/saída assíncrona
     * com um dispositivo periférico previamente configurado.</p>
     *
     * <p>Este simulador não implementa controle ou comunicação com dispositivos de I/O,
     * e, portanto, esta operação está desabilitada.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeSIO() {
        String log = "SIO: Início de I/O não implementado.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (SSK - Set Index Register Mask / Protection Key).
     *
     * <p>A instrução SSK deveria configurar uma máscara de proteção na memória
     * usando um registrador índice como referência, limitando o acesso a certas áreas.</p>
     *
     * <p>Como o simulador não implementa gerenciamento de proteção de memória,
     * esta funcionalidade foi omitida.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeSSK() {
        String log = "SSK: Operação de proteção não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (RD - Read from Device).
     *
     * <p>A instrução RD deveria ler um byte de um dispositivo de entrada
     * e armazená-lo em um registrador ou posição de memória.</p>
     *
     * <p>Operações diretas com dispositivos não fazem parte do escopo deste simulador.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeRD() {
        String log = "RD: Operação de leitura de dispositivo não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (TIO - Test Input/Output).
     *
     * <p>A instrução TIO deveria testar se um dispositivo de entrada/saída está pronto
     * para leitura ou escrita, retornando uma flag de status.</p>
     *
     * <p>Este simulador não implementa interface com dispositivos físicos ou virtuais.</p>
     *
     * @return Log indicando instrução não implementada.
     */

    public String executeTIO() {
        String log = "TIO: Teste de I/O não implementado.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (TD - Test Device).
     *
     * <p>A instrução TD deveria consultar o status de um dispositivo específico
     * para verificar se ele está pronto para uma operação de I/O.</p>
     *
     * <p>Como o simulador não possui suporte a dispositivos, essa funcionalidade está indisponível.</p>
     *
     * @return Log indicando instrução não implementada.
     */

    public String executeTD() {
        String log = "TD: Teste de dispositivo não implementado.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (SVC - Supervisor Call).
     *
     * <p>A instrução SVC deveria invocar rotinas de serviço do sistema operacional,
     * como chamadas de sistema, interrupções ou gerenciamento de recursos.</p>
     *
     * <p>Como o simulador não implementa um ambiente operacional completo,
     * essa funcionalidade não está disponível.</p>
     *
     * @return Log indicando instrução não implementada.
     */

    public String executeSVC() {
        String log = "SVC: Chamada de sistema não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (WD - Write to Device).
     *
     * <p>A instrução WD deveria enviar um byte de dados para um dispositivo periférico
     * de saída, como impressora, terminal ou arquivo virtual.</p>
     *
     * <p>Este simulador não possui suporte a dispositivos de saída,
     * portanto essa operação é ignorada.</p>
     *
     * @return Log indicando instrução não implementada.
     */

    public String executeWD() {
        String log = "WD: Escrita para dispositivo não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (SUBF - Floating Point Subtraction).
     *
     * <p>A instrução SUBF deveria subtrair um valor de ponto flutuante armazenado na memória
     * do conteúdo atual do registrador F (Floating Point Accumulator), armazenando o resultado de volta em F.</p>
     *
     * <p>Essa operação envolve manipulação precisa de números em ponto flutuante de 48 bits,
     * o que está fora do escopo deste simulador conforme definido nas especificações do projeto.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeSUBF() {
        String log = "SUBF: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (ADDF - Floating Point Addition).
     *
     * <p>A instrução ADDF deveria somar um valor de ponto flutuante (armazenado na memória)
     * ao conteúdo do registrador F (Floating Point Accumulator), atualizando o registrador com o resultado.</p>
     *
     * <p>Como o simulador atual não possui suporte a ponto flutuante,
     * essa instrução foi omitida por decisão de projeto.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeADDF() {
        String log = "ADDF: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (DIVF - Floating Point Division).
     *
     * <p>A instrução DIVF deveria dividir o conteúdo do registrador F por um valor de ponto flutuante
     * localizado na memória e armazenar o resultado em F.</p>
     *
     * <p>A falta de suporte interno a representação e aritmética de ponto flutuante
     * torna essa operação indisponível neste simulador.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeDIVF() {
        String log = "DIVF: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (MULF - Floating Point Multiplication).
     *
     * <p>A instrução MULF deveria multiplicar o conteúdo do registrador F por um valor
     * de ponto flutuante presente na memória, armazenando o resultado em F.</p>
     *
     * <p>Operações de ponto flutuante não são suportadas neste projeto, e por isso essa instrução
     * está marcada como não implementada.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeMULF() {
        String log = "MULF: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (COMPF - Compare Floating Point).
     *
     * <p>A instrução COMPF deveria comparar o conteúdo do registrador F com um valor
     * de ponto flutuante na memória, atualizando o registrador SW de acordo:</p>
     *
     * <ul>
     *   <li>SW = -1 se F &lt; valor</li>
     *   <li>SW =  0 se F = valor</li>
     *   <li>SW =  1 se F &gt; valor</li>
     * </ul>
     *
     * <p>Como o suporte a ponto flutuante não está incluído neste simulador, esta operação não é implementada.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeCOMPF() {
        String log = "COMPF: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (FIX - Convert Floating to Fixed).
     *
     * <p>A instrução FIX deveria converter o valor em ponto flutuante do registrador F
     * para um valor inteiro e armazená-lo no registrador A.</p>
     *
     * <p>Apesar da implementação atual usar conversão direta ('(int) long'),
     * operações de ponto flutuante não são oficialmente suportadas neste projeto
     * e devem ser tratadas como não implementadas.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeFIX() {
        String log = "FIX: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (FLOAT - Convert Fixed to Floating).
     *
     * <p>A instrução FLOAT deveria converter o valor inteiro do registrador A
     * para formato de ponto flutuante e armazená-lo no registrador F.</p>
     *
     * <p>Apesar da implementação atual usar uma conversão direta (inteiro para long),
     * operações de ponto flutuante não são oficialmente suportadas neste projeto
     * e devem ser tratadas como não implementadas.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeFLOAT() {
        String log = "FLOAT: Operação ponto flutuante não implementada.";
        logger.info(log);
        return log;
    }

    /**
     * <b>[STUB]</b> Não implementado (NORM - Normalize Floating Point).
     *
     * <p>A instrução NORM ajustaria o valor do registrador F para um formato
     * normalizado de ponto flutuante.</p>
     *
     * <p>Esta funcionalidade não é suportada no simulador, conforme especificações do projeto.</p>
     *
     * @return Log indicando instrução não implementada.
     */
    public String executeNORM() {
        String log = "NORM: Operação de normalização não implementada.";
        logger.info(log);
        return log;
    }
}
