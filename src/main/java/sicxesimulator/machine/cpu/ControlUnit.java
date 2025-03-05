package sicxesimulator.machine.cpu;

import sicxesimulator.machine.memory.Memory;

public class ControlUnit {
    private final InstructionDecoder decoder;
    private final ExecutionUnit executionUnit;
    private final RegisterSet registerSet;
    private final Memory memory;
    private Instruction next;
    private boolean halted;
    private String lastExecutionLog;


    public ControlUnit(Memory memory) {
        this.memory = memory;
        this.registerSet = new RegisterSet();
        this.decoder = new InstructionDecoder(registerSet, memory);
        this.executionUnit = new ExecutionUnit(registerSet, memory);
        this.halted = false;
    }

    public RegisterSet getRegisterSet() {
        return this.registerSet;
    }

    /**
     * Fetch: Lê a instrução da memória com base no valor do PC.
     */
    public void fetch() {
        int byteAddress = registerSet.getRegister("PC").getIntValue(); // PC já em bytes
        int wordIndex = byteAddress / 3;

        if (wordIndex < 0 || wordIndex >= memory.getAddressRange()) {
            throw new IllegalArgumentException("Endereço de memória inválido: " + byteAddress);
        }

        // Lógica de leitura da memória
        byte[] instructionBytes = memory.readWord(wordIndex);
        // ... processamento da instrução

        // TODO:
    }

    /**
     * Decode: Decodifica a instrução baseada no valor do PC e no opcode.
     */
    public void decode() {
        next = decoder.decodeInstruction();

        // Calcula o tamanho da instrução
        int instructionSize = next.getSizeInBytes();
        incrementPC(instructionSize);
    }

    /**
     * Execute: Executa a instrução utilizando a unidade de execução.
     */
    public void execute() {
        // Cria a ExecutionUnit e executa a instrução
        lastExecutionLog = executionUnit.execute(next);
    }

    public String getLastExecutionLog() {
        return lastExecutionLog;
    }

    public int getIntValuePC() {
        return registerSet.getRegister("PC").getIntValue();
    }

    public void setIntValuePC(int value) {
        registerSet.getRegister("PC").setValue(value);
    }

    /**
     * Incrementa o valor do PC conforme o tamanho da instrução.
     * O PC é incrementado em palavras, mas o endereço de memória é em bytes.
     */
    private void incrementPC(int instructionSizeInWords) {
        setIntValuePC(getIntValuePC() + (instructionSizeInWords * 3));
    }

    /**
     * Verifica se a máquina está em estado de "halt".
     */
    public boolean isHalted() {
        return halted;
    }

    public void reset() {
        registerSet.clearAll();
        halted = false;
    }
}
