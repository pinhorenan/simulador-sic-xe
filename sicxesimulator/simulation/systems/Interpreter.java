package sicxesimulator.simulation.systems;

import sicxesimulator.simulation.virtualMachine.Machine;
import sicxesimulator.simulation.virtualMachine.operations.Operation;
import sicxesimulator.simulation.virtualMachine.operations.Instruction;

import java.util.List;

public class Interpreter {
    private Machine machine;
    private List<Instruction> instructions;
    private int programCounter = 0;
    private Operation op;

    public Interpreter(Machine machine) {
        this.machine = machine;
        this.op = new Operation(machine);
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
        this.programCounter = 0;
    }

    public void setStartAddress(int startAddress) {
        programCounter = startAddress;
    }

    public boolean isFinished() {
        return instructions == null || programCounter >= instructions.size();
    }

    public void runNextInstruction() {
        if (isFinished()) {
            System.out.println("Fim da execução.");
            return;
        }
        Instruction instruction = instructions.get(programCounter);
        System.out.println("Executando: " + instruction.getName() + " " + String.join(" ", instruction.getArgs()));
        executeInstruction(instruction);
        programCounter++;
    }

    private void executeInstruction(Instruction instruction) {
        String name = instruction.getName().toUpperCase();
        String[] args = instruction.getArgs();

        switch (name) {
            case "ADD":
                op.add(args);
                break;
            case "ADDR":
                op.addr(args);
                break;
            case "AND":
                op.and(args);
                break;
            case "CLEAR":
                op.clear(args);
                break;
            case "LDA":
                op.LDA(args);
                break;
            case "STA":
                op.sta(args);
                break;
            case "SUB":
                op.sub(args);
                break;
            case "J":
                op.j(args);
                break;
            case "LDX":
                op.ldx(args);
                break;
            case "COMP":
                op.comp(args);
                break;
            case "COMPR":
                op.compr(args);
                break;
            case "DIV":
                op.div(args);
                break;
            case "DIVR":
                op.divr(args);
                break;
            case "JEQ":
                op.jeq(args);
                break;
            case "JGT":
                op.jgt(args);
                break;
            case "JLT":
                op.jlt(args);
                break;
            case "JSUB":
                op.jsub(args);
                break;
            case "LDB":
                op.ldb(args);
                break;
            case "LDCH":
                op.ldch(args);
                break;
            case "LDL":
                op.ldl(args);
                break;
            case "LDS":
                op.lds(args);
                break;
            case "LDT":
                op.ldt(args);
                break;
            case "MUL":
                op.mul(args);
                break;
            case "MULR":
                op.mulr(args);
                break;
            case "OR":
                op.or(args);
                break;
            case "RMO":
                op.rmo(args);
                break;
            case "RSUB":
                op.rsub(); // RSUB sem argumentos
                break;
            case "SHIFTL":
                op.shiftl(args);
                break;
            case "SHIFTR":
                op.shiftr(args);
                break;
            case "STB":
                op.stb(args);
                break;
            case "STCH":
                op.stch(args);
                break;
            case "STL":
                op.stl(args);
                break;
            case "STS":
                op.sts(args);
                break;
            case "STT":
                op.stt(args);
                break;
            case "STX":
                op.stx(args);
                break;
            case "SUBR":
                op.subr(args);
                break;
            case "TIX":
                op.tix(args);
                break;
            case "TIXR":
                op.tixr(args);
                break;
            default:
                System.out.println("Instrução desconhecida: " + name);
                break;
        }
    }
}
