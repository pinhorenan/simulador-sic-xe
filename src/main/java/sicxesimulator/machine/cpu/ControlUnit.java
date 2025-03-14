package sicxesimulator.machine.cpu;

import sicxesimulator.machine.Memory;
import sicxesimulator.models.Instruction;

import java.util.logging.Logger;

/**
 * Classe responsável pelo controle do ciclo de execução do processador do simulador SIC/XE.
 * Gerencia a decodificação e execução das instruções.
 */
public class ControlUnit {
    private static final Logger logger = Logger.getLogger(ControlUnit.class.getName());

    private final InstructionDecoder decoder;
    private final ExecutionUnit executionUnit;
    private final RegisterSet registerSet;

    private Instruction currentInstruction;
    private boolean halted;
    private String lastExecutionLog;

    /**
     * Constrói uma nova instância da unidade de controle, inicializando os componentes necessários.
     *
     * @param memory Memória utilizada pela CPU para acessar instruções e dados.
     */
    public ControlUnit(Memory memory) {
        this.registerSet = new RegisterSet();
        this.decoder = new InstructionDecoder(registerSet, memory);
        this.executionUnit = new ExecutionUnit(registerSet, memory);
        this.halted = false;
    }

    /// ===== Métodos de Acesso e Modificação =====

    /**
     * Retorna o conjunto de registradores do processador.
     * @return o RegisterSet do processador.
     */
    public RegisterSet getRegisterSet() {
        return this.registerSet;
    }

    /**
     * Retorna o log da última execução de instrução.
     * @return String com o log da última execução.
     */
    public String getLastExecutionLog() {
        return lastExecutionLog;
    }

    /**
     * Retorna o valor inteiro do registrador PC.
     * @return o valor atual do PC.
     */
    public int getIntValuePC() {
        return registerSet.getRegister("PC").getIntValue();
    }

    /**
     * Define o valor do registrador PC.
     * @param value valor a ser atribuído ao PC.
     */
    public void setIntValuePC(int value) {
        registerSet.getRegister("PC").setValue(value);
    }

    /**
     * Define o status halted do processador.
     * @param halted true se o processador deve ser interrompido.
     */
    public void setHalted(boolean halted) {
        this.halted = halted;
    }

    /**
     * Verifica se o processador está em estado halted.
     * @return true se estiver halted, false caso contrário.
     */
    public boolean isProcessorHalted() {
        return halted;
    }

    /// ===== Métodos de Controle do Ciclo de Execução =====

    /**
     * Realiza um passo completo do ciclo de execução: busca, decodificação e execução da instrução.
     */
    public void step() {
        fetch();
        incrementPC(currentInstruction.getSize());
        lastExecutionLog = executeInstruction();
    }

    /**
     * Realiza a busca e decodificação da próxima instrução a partir do PC.
     */
    private void fetch() {
        currentInstruction = decoder.decodeInstruction();
    }

    /**
     * Incrementa o valor do PC com base no tamanho da instrução atual.
     * @param instructionSizeInBytes o tamanho da instrução atual em bytes.
     */
    private void incrementPC(int instructionSizeInBytes) {
        setIntValuePC(getIntValuePC() + instructionSizeInBytes);
    }

    /// ===== Métodos Utilitários =====

    /**
     * Limpa o conteúdo de todos os registradores.
     */
    public void clearAllRegisters() {
        registerSet.clearAll();
    }

    /**
     * Reseta a unidade de controle e todos os seus componentes associados para o estado inicial.
     */
    public void reset() {
        decoder.resetProgramCounter();
        clearAllRegisters();
        setHalted(false);
        currentInstruction = null;
        lastExecutionLog = null;
    }

    /// ===== Métodos Internos Privados =====

    /**
     * Executa a instrução atual, determinando a operação correta com base no formato e opcode.
     * @return uma string com o log da operação executada.
     */
    private String executeInstruction() {
        int format = currentInstruction.format();
        int opcode = currentInstruction.opcode();
        int[] operands = currentInstruction.operands();
        boolean indexed = currentInstruction.indexed();
        int effectiveAddress = currentInstruction.effectiveAddress();
        String log;

        // Log antes da execução
        logger.info(String.format("Executando instrucao: Opcode %s, Operandos %s, EffectiveAddress %06X, Indexed: %s",
                Integer.toHexString(opcode), java.util.Arrays.toString(operands), effectiveAddress, indexed));

        switch (format) {
            case 2:
                // Instruções de formato 2 (apenas registradores)
                log = switch (opcode) {
                    case 0x04 -> executionUnit.executeCLEAR_LDX(currentInstruction, operands);
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
                        log = executionUnit.executeAND(operands, indexed, effectiveAddress);
                        break;
                    case 0x28:
                        log = executionUnit.executeCOMP(operands, indexed, effectiveAddress);
                        break;
                    case 0xA0:
                        log = executionUnit.executeCOMPR(operands);
                        break;
                    case 0x24:
                        log = executionUnit.executeDIV(operands, indexed, effectiveAddress);
                        break;
                    case 0x9C:
                        log = executionUnit.executeDIVR(operands);
                        break;
                    case 0x3C:
                        log = executionUnit.executeJ(operands, indexed, effectiveAddress);
                        break;
                    case 0x30:
                        log = executionUnit.executeJEQ(operands, indexed, effectiveAddress);
                        break;
                    case 0x34:
                        log = executionUnit.executeJGT(operands, indexed, effectiveAddress);
                        break;
                    case 0x38:
                        log = executionUnit.executeJLT(operands, indexed, effectiveAddress);
                        break;
                    case 0x48:
                        log = executionUnit.executeJSUB(operands, indexed, effectiveAddress);
                        break;
                    case 0x00:
                        log = executionUnit.executeLDA(operands, indexed, effectiveAddress);
                        break;
                    case 0x68:
                        log = executionUnit.executeLDB(operands, indexed, effectiveAddress);
                        break;
                    case 0x50:
                        log = executionUnit.executeLDCH(operands, indexed, effectiveAddress);
                        break;
                    case 0x08:
                        log = executionUnit.executeLDL(operands, indexed, effectiveAddress);
                        break;
                    case 0x6C:
                        log = executionUnit.executeLDS(operands, indexed, effectiveAddress);
                        break;
                    case 0x74:
                        log = executionUnit.executeLDT(operands, indexed, effectiveAddress);
                        break;
                    case 0x20:
                        log = executionUnit.executeMUL(operands, indexed, effectiveAddress);
                        break;
                    case 0x98:
                        log = executionUnit.executeMULR(operands);
                        break;
                    case 0x44:
                        log = executionUnit.executeOR(operands, indexed, effectiveAddress);
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
                        log = executionUnit.executeSTA(operands, indexed, effectiveAddress);
                        break;
                    case 0x78:
                        log = executionUnit.executeSTB(operands, indexed, effectiveAddress);
                        break;
                    case 0x54:
                        log = executionUnit.executeSTCH(operands, indexed, effectiveAddress);
                        break;
                    case 0x14:
                        log = executionUnit.executeSTL(operands, indexed, effectiveAddress);
                        break;
                    case 0x7C:
                        log = executionUnit.executeSTS(operands, indexed, effectiveAddress);
                        break;
                    case 0x84:
                        log = executionUnit.executeSTT(operands, indexed, effectiveAddress);
                        break;
                    case 0x10:
                        log = executionUnit.executeSTX(operands, indexed, effectiveAddress);
                        break;
                    case 0x1C:
                        log = executionUnit.executeSUB(operands, indexed, effectiveAddress);
                        break;
                    case 0x94:
                        log = executionUnit.executeSUBR(operands);
                        break;
                    case 0x2C:
                        log = executionUnit.executeTIX(operands, indexed, effectiveAddress);
                        break;
                    case 0xB8:
                        log = executionUnit.executeTIXR(operands);
                        break;
                    default:
                        throw new IllegalStateException("Instrucao de formato 3 nao implementada: " + Integer.toHexString(opcode));
                }
                break;
            default:
                throw new IllegalStateException("Formato de instrucao nao implementado: " + currentInstruction.format());
        }
        return log;
    }
}
