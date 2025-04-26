package sicxesimulator.hardware.cpu.decoder;

import sicxesimulator.hardware.Memory;
import sicxesimulator.hardware.cpu.core.RegisterSet;
import sicxesimulator.hardware.data.Instruction;
import sicxesimulator.utils.Convert;

/**
 * Decodifica instruções na memória a partir do PC, identificando formato,
 * campos de registradores, flags de modo e calculando endereço efetivo.
 *
 * @author Renan
 * @since 1.0.0
 */
public class InstructionDecoder {
    private final Memory memory;
    private final RegisterSet registers;
    private int programCounter;

    public InstructionDecoder(RegisterSet registers, Memory memory) {
        this.memory = memory;
        this.registers = registers;
    }

    /**
     * Decodifica a instrução atual no PC.
     * @return instrução decodificada
     */
    public Instruction decodeInstruction() {
        programCounter = registers.getRegister("PC").getIntValue();
        int firstByte = memory.readByte(programCounter) & 0xFF;
        int n = (firstByte >> 1) & 1, i = firstByte & 1;
        int format = determineInstructionFormat(firstByte);
        int opcode, effectiveAddress = 0;
        int[] operands;
        boolean indexed = false;

        if (format == 1) {
            opcode = firstByte;
            operands = new int[0];
        } else if (format == 2) {
            opcode = firstByte;
            operands = decodeFormat2();
        } else {
            opcode = firstByte & 0xFC;
            operands = decodeFormat3Or4(n, i, format);
            indexed = operands[1] == 1;
            effectiveAddress = calculateEffectiveAddress(
                    operands[0], operands[1], operands[2],
                    operands[3], operands[4], operands[5], operands[6]
            );
        }

        return new Instruction(opcode, operands, format, indexed, effectiveAddress);
    }

    /**
     * @return a
     */
    public Memory getMemory() {
        return memory;
    }

    /**
     * Reseta o PC para zero.
     */
    public void resetProgramCounter() {
        registers.getRegister("PC").setValue(0);
        programCounter = 0;
    }

    // private

    private int determineInstructionFormat(int fullByte) {
        if (fullByte==0xC4||fullByte==0xC0||fullByte==0xC8||fullByte==0xF4||fullByte==0xF0||fullByte==0xF8) return 1;
        if (fullByte==0x04||fullByte==0x90) return 2;
        int second = memory.readByte(programCounter+1)&0xFF;
        return ((second&0x10)>>4)==1 ? 4 : 3;
    }

    private int[] decodeFormat2() {
        int second = memory.readByte(programCounter+1)&0xFF;
        return new int[]{ (second>>4)&0xF, second&0xF };
    }

    private int[] decodeFormat3Or4(int n, int i, int format) {
        int second = memory.readByte(programCounter+1)&0xFF;
        int x=(second&0x80)>>7, b=(second&0x40)>>6, p=(second&0x20)>>5, e=(second&0x10)>>4;
        int high4 = second&0x0F;
        if (format==3) {
            int third = memory.readByte(programCounter+2)&0xFF;
            int disp12 = (high4<<8)|third;
            return new int[]{ disp12, x, b, p, e, n, i };
        } else {
            int third = memory.readByte(programCounter+2)&0xFF;
            int fourth= memory.readByte(programCounter+3)&0xFF;
            int addr20 = (high4<<16)|(third<<8)|fourth;
            return new int[]{ addr20, x, b, p, e, n, i };
        }
    }

    private int calculateEffectiveAddress(int dispOrAddr, int x, int b, int p, int e, int n, int i) {
        if (n==0&&i==1) {
            int imm = dispOrAddr;
            if (e==0 && (imm&0x800)!=0) imm -= 0x1000;
            return imm;
        }
        int addr = dispOrAddr;
        if (e==0 && (addr&0x800)!=0) addr -= 0x1000;
        if (p==1) addr += programCounter + (e==1?4:3);
        else if (b==1) addr += registers.getRegister("B").getIntValue();
        if (x==1) addr += registers.getRegister("X").getIntValue();
        if (n==1 && i==0) {
            if (addr%3!=0) throw new IllegalArgumentException("Indireto nao alinhado: "+addr);
            return Convert.bytesToInt(memory.readWord(addr/3));
        }
        return addr;
    }

}
