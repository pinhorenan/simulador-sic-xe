package sicxesimulator.hardware.cpu;

import sicxesimulator.hardware.Memory;
import sicxesimulator.hardware.data.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a unidade de controle do processador SIC/XE.
 *
 * Responsável por coordenar o ciclo de execução das instruções,
 * incluindo a busca, decodificação e delegação da execução.
 *
 * Gerencia os registradores, o estado da CPU (como o bit 'halted'),
 * e mantém um histórico das instruções executadas para fins de depuração.
 */
public class ControlUnit {
    private final List<String> executionHistory = new ArrayList<>(); // Histórico de execução, para depuração

    private final InstructionDecoder decoder;
    private final ExecutionUnit executionUnit;
    private final RegisterSet registerSet;

    private Instruction currentInstruction;
    private boolean halted;
    private String lastExecutionLog;

    /**
     * Constrói uma nova instância da unidade de controle, inicializando
     * o conjunto de registradores, o decodificador e a unidade de execução.
     *
     * @param memory Memória que será acessada pela CPU.
     */
    public ControlUnit(Memory memory) {
        this.registerSet = new RegisterSet();
        this.decoder = new InstructionDecoder(registerSet, memory);
        this.executionUnit = new ExecutionUnit(registerSet, memory);
        this.halted = false;
    }

    /**
     * Retorna o conjunto de registradores gerenciado pela unidade de controle.
     *
     * @return {@link RegisterSet} contendo todos os registradores.
     */
    public RegisterSet getRegisterSet() {
        return this.registerSet;
    }

    /**
     * Retorna o log da última instrução executada.
     *
     * @return String com a descrição da execução mais recente.
     */
    public String getLastExecutionLog() {
        return lastExecutionLog;
    }

    /**
     * Retorna o histórico completo de instruções executadas no ciclo da CPU.
     *
     * @return String com múltiplas linhas, cada uma contendo o log de execução.
     */
    public String getExecutionHistory() {
        return String.join("\n", executionHistory);
    }

    /**
     * Obtém o valor atual do registrador PC (Program Counter).
     *
     * @return Valor inteiro armazenado em PC.
     */
    public int getIntValuePC() {
        return registerSet.getRegister("PC").getIntValue();
    }

    /**
     * Define o valor do registrador PC (Program Counter).
     *
     * @param value Novo valor a ser atribuído a PC.
     */
    public void setIntValuePC(int value) {
        registerSet.getRegister("PC").setValue(value);
    }

    /**
     * Verifica se o processador está parado (halted).
     *
     * @return true se estiver em estado de parada, false caso contrário.
     */
    public boolean isHalted() {
        return halted;
    }

    /**
     * Altera o estado halted do processador.
     *
     * @param halted true para parar a execução; false para continuar.
     */
    public void setHalted(boolean halted) {
        this.halted = halted;
    }

    /**
     * Executa um ciclo completo de CPU:
     * - Busca a próxima instrução a partir do PC.
     * - Decodifica a instrução.
     * - Executa a instrução.
     *
     * O PC é automaticamente incrementado com base no tamanho da instrução.
     */
    public void step() {
        fetch();
        incrementPC(currentInstruction.getSize());
        lastExecutionLog = executeInstruction();
    }

    /**
     * Realiza a busca e decodificação da próxima instrução na memória.
     *
     * A instrução resultante é armazenada em 'currentInstruction'.
     */
    private void fetch() {
        currentInstruction = decoder.decodeInstruction();
    }

    /**
     * Incrementa o valor do registrador PC com base no tamanho da instrução.
     *
     * @param instructionSizeInBytes Tamanho da instrução atual.
     */
    private void incrementPC(int instructionSizeInBytes) {
        setIntValuePC(getIntValuePC() + instructionSizeInBytes);
    }

    /**
     * Zera todos os registradores do processador.
     */
    public void clearAllRegisters() {
        registerSet.clearAll();
    }

    /**
     * Reseta a unidade de controle para o estado inicial:
     * - Zera registradores
     * - Reinicia o decodificador (PC = 0)
     * - Limpa o log de execução
     * - Retira o processador do estado halted
     */
    public void reset() {
        decoder.resetProgramCounter();
        clearAllRegisters();
        setHalted(false);
        currentInstruction = null;
        lastExecutionLog = null;
    }

    /**
     * Executa a instrução atualmente carregada (armazenada em 'currentInstruction'),
     * delegando para a {@link ExecutionUnit} conforme o formato e o opcode.
     *
     * Suporta instruções dos formatos 2 e 3.
     * - Formato 1: Operações que, na verdade, são só STUB (FLOAT, FIX, NORM).
     * - Formato 2: Operações entre registradores (CLEAR, ADDR, etc.).
     * - Formato 3: Operações com acesso à memória (LDA, STA, J, etc.).
     *
     * Instruções como RSUB podem modificar o estado do processador (halt).
     *
     * @return String descrevendo a operação executada.
     * @throws IllegalStateException Se a instrução não for suportada.
     */
    private String executeInstruction() {
        int format = currentInstruction.format();
        int opcode = currentInstruction.opcode();
        int[] operands = currentInstruction.operands();
        boolean indexed = currentInstruction.indexed();
        int effectiveAddress = currentInstruction.effectiveAddress();
        String log;

        switch (format) {
            case 1:
                log = switch (opcode) {
                    case 0xC0 -> executionUnit.executeFLOAT();
                    case 0xC4 -> executionUnit.executeFIX();
                    case 0xC8 -> executionUnit.executeNORM();
                    default -> throw new IllegalStateException("Instrucao de formato 1 nao implementada: " + Integer.toHexString(opcode));
                };
                break;
            case 2:
                // Instruções de formato 2 (apenas registradores)
                log = switch (opcode) {
                    case 0x04 -> {
                        if (operands.length == 1) {
                            yield executionUnit.executeCLEAR(operands[0]);
                        } else if (operands.length == 2) {
                            yield executionUnit.executeLDX(operands, effectiveAddress);
                        } else {
                            throw new IllegalStateException("Número de operandos inválido para CLEAR/LDX");
                        }
                    }
                    case 0x90 -> executionUnit.executeADDR(operands);
                    default ->
                            throw new IllegalStateException("Instrucao de formato 2 nao implementada: " + Integer.toHexString(opcode));
                };
                break;
            case 3:
                // Instruções de formato 3 (com acesso à memória)
                switch (opcode) {
                    case 0x18:
                        log = executionUnit.executeADD(operands, indexed, effectiveAddress);
                        break;
                    case 0x40:
                        log = executionUnit.executeAND(operands, effectiveAddress);
                        break;
                    case 0x28:
                        log = executionUnit.executeCOMP(operands, effectiveAddress);
                        break;
                    case 0xA0:
                        log = executionUnit.executeCOMPR(operands);
                        break;
                    case 0x24:
                        log = executionUnit.executeDIV(operands, effectiveAddress);
                        break;
                    case 0x9C:
                        log = executionUnit.executeDIVR(operands);
                        break;
                    case 0x3C:
                        log = executionUnit.executeJ(effectiveAddress);
                        break;
                    case 0x30:
                        log = executionUnit.executeJEQ(effectiveAddress);
                        break;
                    case 0x34:
                        log = executionUnit.executeJGT(effectiveAddress);
                        break;
                    case 0x38:
                        log = executionUnit.executeJLT(effectiveAddress);
                        break;
                    case 0x48:
                        log = executionUnit.executeJSUB(effectiveAddress);
                        break;
                    case 0x00:
                        log = executionUnit.executeLDA(operands, effectiveAddress);
                        break;
                    case 0x68:
                        log = executionUnit.executeLDB(operands, effectiveAddress);
                        break;
                    case 0x50:
                        log = executionUnit.executeLDCH(operands, effectiveAddress);
                        break;
                    case 0x08:
                        log = executionUnit.executeLDL(operands, effectiveAddress);
                        break;
                    case 0x6C:
                        log = executionUnit.executeLDS(operands, effectiveAddress);
                        break;
                    case 0x74:
                        log = executionUnit.executeLDT(operands, effectiveAddress);
                        break;
                    case 0x20:
                        log = executionUnit.executeMUL(operands, effectiveAddress);
                        break;
                    case 0x98:
                        log = executionUnit.executeMULR(operands);
                        break;
                    case 0x44:
                        log = executionUnit.executeOR(operands, effectiveAddress);
                        break;
                    case 0xAC:
                        log = executionUnit.executeRMO(operands);
                        break;
                    case 0x4C:
                        log = executionUnit.executeRSUB();
                        if (log.contains("HALT")) {
                            setHalted(true);
                        }
                        break;
                    case 0xA4:
                        log = executionUnit.executeSHIFTL(operands);
                        break;
                    case 0xA8:
                        log = executionUnit.executeSHIFTR(operands);
                        break;
                    case 0x0C:
                        log = executionUnit.executeSTA(effectiveAddress);
                        break;
                    case 0x78:
                        log = executionUnit.executeSTB(effectiveAddress);
                        break;
                    case 0x54:
                        log = executionUnit.executeSTCH(effectiveAddress);
                        break;
                    case 0x14:
                        log = executionUnit.executeSTL(effectiveAddress);
                        break;
                    case 0x7C:
                        log = executionUnit.executeSTS(effectiveAddress);
                        break;
                    case 0x84:
                        log = executionUnit.executeSTT(effectiveAddress);
                        break;
                    case 0x10:
                        log = executionUnit.executeSTX(effectiveAddress);
                        break;
                    case 0x1C:
                        log = executionUnit.executeSUB(operands, effectiveAddress);
                        break;
                    case 0x94:
                        log = executionUnit.executeSUBR(operands);
                        break;
                    case 0x2C:
                        log = executionUnit.executeTIX(effectiveAddress);
                        break;
                    case 0xB8:
                        log = executionUnit.executeTIXR(operands);
                        break;
                    case 0x70:
                        log = executionUnit.executeLDF();
                        break;
                    case 0x80:
                        log = executionUnit.executeSTF();
                        break;
                    case 0xD4:
                        log = executionUnit.executeSTI();
                        break;
                    case 0xE8:
                        log = executionUnit.executeSTSW();
                        break;
                    case 0xD0:
                        log = executionUnit.executeLPS();
                        break;
                    case 0xF0:
                        log = executionUnit.executeSIO();
                        break;
                    case 0xEC:
                        log = executionUnit.executeSSK();
                        break;
                    case 0xD8:
                        log = executionUnit.executeRD();
                        break;
                    case 0xF8:
                        log = executionUnit.executeTIO();
                        break;
                    case 0xE0:
                        log = executionUnit.executeTD();
                        break;
                    case 0xB0:
                        log = executionUnit.executeSVC();
                        break;
                    case 0xDC:
                        log = executionUnit.executeWD();
                        break;
                    case 0x5C:
                        log = executionUnit.executeSUBF();
                        break;
                    case 0x58:
                        log = executionUnit.executeADDF();
                        break;
                    case 0x64:
                        log = executionUnit.executeMULF();
                        break;
                    case 0x88:
                        log = executionUnit.executeCOMPF();
                        break;
                    case 0x60:
                        log = executionUnit.executeDIVF();
                        break;
                    default:
                        throw new IllegalStateException("Instrucao de formato 3 nao implementada: " + Integer.toHexString(opcode));
                }
                break;
            default:
                throw new IllegalStateException("Formato de instrucao nao implementado: " + currentInstruction.format());
        }

        executionHistory.add(log);
        return log;
    }
}
