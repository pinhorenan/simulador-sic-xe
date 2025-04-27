package sicxesimulator.hardware.cpu.control;

import sicxesimulator.hardware.memory.Memory;
import sicxesimulator.hardware.cpu.model.ExecutionContext;
import sicxesimulator.hardware.cpu.decoder.InstructionDecoder;
import sicxesimulator.hardware.cpu.register.RegisterSet;
import sicxesimulator.hardware.cpu.model.Instruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordena o ciclo de instruções da CPU SIC/XE: fetch → decode → execute.
 * <p>
 * Gerencia o Program Counter, o estado de parada (halted), histórico de execução
 * e expõe logs para depuração.
 *
 * @author Renan
 * @since 1.0.0
 */
public class ControlUnit {

    private final RegisterSet registerSet;
    private final InstructionDecoder decoder;
    private final ExecutionDispatcher dispatcher;
    private final List<String> executionHistory = new ArrayList<>();

    private Instruction currentInstruction;
    private String lastExecutionLog;
    private boolean halted;

    /**
     * Cria uma ControlUnit ligada à memória fornecida.
     *
     * @param memory memória que será acessada para buscar instruções e dados
     */
    public ControlUnit(Memory memory) {
        this.registerSet   = new RegisterSet();
        this.decoder       = new InstructionDecoder(registerSet, memory);
        this.dispatcher    = new ExecutionDispatcher();
        this.halted        = false;
    }

    /** @return conjunto de registradores da CPU */
    public RegisterSet getRegisterSet() {
        return registerSet;
    }

    /** @return verdadeiro se a CPU estiver em estado de HALT */
    public boolean isHalted() {
        return halted;
    }

    /**
     * Define estado de parada da CPU.
     *
     * @param halted true para parar, false para reativar
     */
    public void setHalted(boolean halted) {
        this.halted = halted;
    }

    /** @return valor inteiro atual do PC (Program Counter) */
    public int getIntValuePC() {
        return registerSet.getRegister("PC").getIntValue();
    }

    /**
     * Atualiza o PC com novo valor.
     *
     * @param value novo valor para o Program Counter
     */
    public void setIntValuePC(int value) {
        registerSet.getRegister("PC").setValue(value);
    }

    /** @return descrição da última instrução executada */
    public String getLastExecutionLog() {
        return lastExecutionLog;
    }

    /** @return histórico completo de instruções executadas */
    public List<String> getExecutionHistory() {
        return List.copyOf(executionHistory);
    }

    /**
     * Executa um ciclo de CPU:
     * 1. Busca e decodifica próxima instrução,
     * 2. Incrementa o PC,
     * 3. Executa e grava log.
     *
     * @throws IllegalStateException se a instrução não for suportada
     */
    public void step() {
        fetch();
        incrementPC(currentInstruction.getSize());
        lastExecutionLog = executeInstruction();
        executionHistory.add(lastExecutionLog);
    }

    /** Zera todos os registradores (inclusive SW e PC). */
    public void clearAllRegisters() {
        registerSet.clearAll();
    }

    /**
     * Retorna ao estado inicial:
     * - PC = 0
     * - Registradores zerados
     * - Histórico e halted limpos
     */
    public void reset() {
        decoder.resetProgramCounter();
        clearAllRegisters();
        halted = false;
        currentInstruction = null;
        lastExecutionLog    = null;
        executionHistory.clear();
    }

    // private

    private void fetch() {
        currentInstruction = decoder.decodeInstruction();
    }

    private void incrementPC(int instructionSizeInBytes) {
        setIntValuePC(getIntValuePC() + instructionSizeInBytes);
    }

    private String executeInstruction() {
        int opcode           = currentInstruction.opcode();
        int[] operands       = currentInstruction.operands();
        boolean indexed      = currentInstruction.indexed();
        int effectiveAddress = currentInstruction.effectiveAddress();

        ExecutionContext ctx = new ExecutionContext(operands, indexed, effectiveAddress, registerSet, decoder.getMemory());
        String log = dispatcher.dispatch(opcode, ctx);

        if (log.contains("HALT")) halted = true;
        return log;
    }
}
