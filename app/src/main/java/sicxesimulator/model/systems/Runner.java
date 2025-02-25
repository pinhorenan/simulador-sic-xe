package sicxesimulator.model.systems;

import sicxesimulator.model.components.Machine;
import sicxesimulator.model.components.Register;
import sicxesimulator.model.components.operations.Instruction;
import sicxesimulator.model.components.operations.Operation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Runner {
    private final Machine machine;
    private final Map<String, Consumer<String[]>> operationMap = new HashMap<>();
    private final Operation operations;

    public Runner(Machine machine) {
        this.machine = machine;
        this.operations = new Operation(machine);
        initializeOperations();
    }

    private void initializeOperations() {
        operationMap.put("ADD", operations::add);
        operationMap.put("ADDR", operations::addr);
        operationMap.put("AND", operations::and);
        operationMap.put("CLEAR", operations::clear);
        operationMap.put("COMP", operations::comp);
        operationMap.put("DIV", operations::div);
        operationMap.put("J", operations::j);
        operationMap.put("JEQ", operations::jeq);
        operationMap.put("JGT", operations::jgt);
        operationMap.put("JLT", operations::jlt);
        operationMap.put("LDA", operations::lda);
        operationMap.put("LDCH", operations::ldch);
        operationMap.put("LDL", operations::ldl);
        operationMap.put("LDX", operations::ldx);
        operationMap.put("RSUB", args -> operations.rsub());
        operationMap.put("STA", operations::sta);
        operationMap.put("STCH", operations::stch);
        operationMap.put("STX", operations::stx);
        operationMap.put("SUB", operations::sub);
        operationMap.put("TIX", operations::tix);
        operationMap.put("MUL", operations::mul);
        operationMap.put("JSUB", operations::jsub);
    }

    public void setStartAddress(int startAddress) {
        machine.setPC(startAddress);
    }

    public boolean isFinished() {
        int pcValue = machine.getPC().getIntValue();
        return pcValue >= machine.getMemory().getSize() ||
                machine.getMemory().read(pcValue).equals("00");
    }

    public void runNextInstruction() {
        if (isFinished()) {
            System.out.println("Execução concluída.");
            return;
        }

        Register pc = machine.getPC();
        int currentAddress = pc.getIntValue();

        try {
            Instruction instruction = decodeInstruction(currentAddress);
            System.out.println("Executando: " + instruction);
            executeInstruction(instruction);
            updatePC(instruction);
        } catch (Exception e) {
            System.err.println("Erro na execução: " + e.getMessage());
            machine.getPC().setValue(machine.getMemory().getSize());
        }
    }

    private Instruction decodeInstruction(int address) {
        String opCode = machine.getMemory().read(address);
        int format = getInstructionFormat(opCode);
        String mnemonic = resolveMnemonic(opCode, format);
        String[] operands = decodeOperands(address, format);

        return new Instruction("", mnemonic, operands, address, getInstructionSize(format));
    }

    private int getInstructionFormat(String opCode) {
        int opValue = Integer.parseInt(opCode, 16);
        if ((opValue & 0x01) == 0x01) return 4; // Verifica bit E
        if (opCode.matches("9.|A.|B.")) return 2;
        return 3;
    }

    private String resolveMnemonic(String opCode, int format) {
        Map<String, String> opcodeMap = new HashMap<>();
        opcodeMap.put("18", "ADD");
        opcodeMap.put("90", "ADDR");
        opcodeMap.put("3C", "J");
        opcodeMap.put("4C", "RSUB");
        // Adicione outros mnemônicos necessários

        String baseOp = format == 4 ? opCode.substring(0, 2) : opCode;
        return opcodeMap.getOrDefault(baseOp, "UNKNOWN");
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    private String[] decodeOperands(int baseAddress, int format) {
        switch (format) {
            case 2: return decodeFormat2Operands(baseAddress);
            case 4: return decodeFormat4Operands(baseAddress);
            default: return decodeFormat3Operands(baseAddress);
        }
    }

    // Métodos de decodificação corrigidos
    private String[] decodeFormat2Operands(int address) {
        String byte2 = machine.getMemory().read(address + 1);
        return new String[]{
                String.valueOf(Integer.parseInt(byte2.substring(0, 1), 16)),
                String.valueOf(Integer.parseInt(byte2.substring(1, 2), 16))
        };
    }

    private String[] decodeFormat3Operands(int address) {
        String byte2 = machine.getMemory().read(address + 1);
        String byte3 = machine.getMemory().read(address + 2);
        return new String[]{String.format("%04X", Integer.parseInt(byte2 + byte3, 16))};
    }

    private String[] decodeFormat4Operands(int address) {
        String byte2 = machine.getMemory().read(address + 1);
        String byte3 = machine.getMemory().read(address + 2);
        String byte4 = machine.getMemory().read(address + 3);
        return new String[]{String.format("%06X", Integer.parseInt(byte2 + byte3 + byte4, 16))};
    }

    private int getInstructionSize(int format) {
        return switch (format) {
            case 2 -> 2;
            case 4 -> 4;
            default -> 3;
        };
    }

    private void executeInstruction(Instruction instruction) {
        Consumer<String[]> operation = operationMap.get(instruction.getName().toUpperCase());
        if (operation != null) {
            operation.accept(instruction.getArgs());
        } else {
            throw new UnsupportedOperationException("Instrução não implementada: " + instruction.getName());
        }
    }

    private void updatePC(Instruction instruction) {
        machine.getPC().setValue(instruction.getAddress() + instruction.getSize());
    }
}