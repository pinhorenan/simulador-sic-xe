package sicxesimulator.machine.cpu;

import sicxesimulator.machine.Memory;
import sicxesimulator.models.Instruction;

import java.util.logging.Logger;

public class ControlUnit {
    private static final Logger logger = Logger.getLogger(ControlUnit.class.getName());

    private final InstructionDecoder decoder;
    private final ExecutionUnit executionUnit;
    private final RegisterSet registerSet;

    private Instruction currentInstruction;
    private boolean halted;
    private String lastExecutionLog;

    public ControlUnit(Memory memory) {
        this.registerSet = new RegisterSet();
        this.decoder = new InstructionDecoder(registerSet, memory);
        this.executionUnit = new ExecutionUnit(registerSet, memory);
        this.halted = false;
    }

    public RegisterSet getRegisterSet() {
        return this.registerSet;
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
     * Executa um ciclo completo: fetch, decode, incrementa o PC e execução da instrução.
     */
    public void step() {
        // Fetch
        logger.info("Fetching instruction at PC: " + String.format("%06X", getIntValuePC()));
        currentInstruction = decoder.decodeInstruction();

        // Log dos dados decodificados
        logger.info(String.format("Instrução decodificada: Formato %d, Opcode %s, EffectiveAddress %06X, Operandos %s, Indexed: %s",
                currentInstruction.getFormat(),
                Integer.toHexString(currentInstruction.getOpcode()),
                currentInstruction.getEffectiveAddress(),
                java.util.Arrays.toString(currentInstruction.getOperands()),
                currentInstruction.isIndexed()));

        // Incrementa o PC (antes da execução, para manter o PC para cálculos PC-relativos)
        incrementPC(currentInstruction.getSizeInBytes());
        logger.info(String.format("PC incrementado para: %06X", getIntValuePC()));

        // Executa a instrução
        lastExecutionLog = executeInstruction();
        logger.info("Log de execução: " + lastExecutionLog);

        // Log do PC após execução
        logger.info("PC após execução: " + String.format("%06X", getIntValuePC()));
    }

    /**
     * Executa a instrução armazenada em currentInstruction.
     * Inclui um switch-case para instruções de formato 2 e 3.
     */
    private String executeInstruction() {
        int format = currentInstruction.getFormat();
        int opcode = currentInstruction.getOpcode();
        int[] operands = currentInstruction.getOperands();
        boolean indexed = currentInstruction.isIndexed();
        int effectiveAddress = currentInstruction.getEffectiveAddress();
        String log;

        // Log antes da execução
        logger.info(String.format("Executando instrução: Opcode %s, Operandos %s, EffectiveAddress %06X, Indexed: %s",
                Integer.toHexString(opcode), java.util.Arrays.toString(operands), effectiveAddress, indexed));

        switch (format) {
            case 2:
                // Instruções de formato 2 (apenas registradores)
                log = switch (opcode) {
                    case 0x04 -> executionUnit.executeCLEAR_LDX(currentInstruction, operands);
                    case 0x90 -> executionUnit.executeADDR(operands);
                    default ->
                            throw new IllegalStateException("Instrução de formato 2 não implementada: " + Integer.toHexString(opcode));
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
                            setHalted();
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
                        throw new IllegalStateException("Instrução de formato 3 não implementada: " + Integer.toHexString(opcode));
                }
                break;
            default:
                throw new IllegalStateException("Formato de instrução não implementado: " + currentInstruction.getFormat());
        }
        return log;
    }

    private void incrementPC(int instructionSizeInBytes) {
        setIntValuePC(getIntValuePC() + instructionSizeInBytes);
    }

    public void setHalted() {
        halted = true;
    }

    public boolean isHalted() {
        return halted;
    }

    public void reset() {
        registerSet.clearAll();
        halted = false;
    }
}
