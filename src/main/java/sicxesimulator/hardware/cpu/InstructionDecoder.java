package sicxesimulator.hardware.cpu;

import sicxesimulator.hardware.Memory;
import sicxesimulator.hardware.data.Instruction;
import sicxesimulator.utils.Converter;

@SuppressWarnings("JavadocBlankLines")
public class InstructionDecoder {
    private final Memory memory;
    private final RegisterSet registers;
    private int programCounter;

    public InstructionDecoder(RegisterSet registers, Memory memory) {
        this.memory = memory;
        this.registers = registers;
    }

    /**
     * Decodifica a instrução a partir do valor atual de PC, lendo os bytes diretamente da memória.
     */
    public Instruction decodeInstruction() {
        // Atualiza o PC a partir dos registradores.
        programCounter = registers.getRegister("PC").getIntValue();

        // Lê o primeiro byte da instrução da memória (offset 0)
        int firstByte = memory.readByte(programCounter) & 0xFF;

        // Extrai os bits n e i (endereçamento)
        int n = (firstByte >> 1) & 1; // bit 'n'
        int i = firstByte & 1; // bit 'i'

        // Determina o formato da instrução
        int format = determineInstructionFormat(firstByte);

        int opcode;
        int[] operands;
        boolean indexed = false;
        int effectiveAddress = 0;

        if (format == 1) {
            // Formato 1: 1 byte, sem operandos; EA deve ser 0
            opcode = firstByte;
            operands = new int[0];
            // Avança o PC em 1 byte para evitar reprocessar este opcode
            registers.getRegister("PC").setValue(programCounter + 1);
        } else if (format == 2) {
            // Para formato 2, o opcode é lido integralmente (não há bits n e i)
            opcode = firstByte;
            operands = decodeFormat2();
        } else {
            // Para formato 3 ou 4, extrai-se os 6 bits superiores para o opcode
            opcode = firstByte & 0xFC;

            // Faz a decodificação unificada para formato 3/4, já incluindo n e i no resultado
            operands = decodeFormat3Or4(n, i, format);

            // O campo "indexed" é baseado no bit 'x' (operands[1])
            indexed = (operands[1] == 1);

            // Agora calculamos o endereço efetivo (EA) da instrução
            effectiveAddress = calculateEffectiveAddress(
                    operands[0],  // disp ou addr
                    operands[1],  // x
                    operands[2],  // b
                    operands[3],  // p
                    operands[4],  // e
                    operands[5],  // n
                    operands[6]   // i
            );
        }

        return new Instruction(opcode, operands, format, indexed,  effectiveAddress);
    }

    /**
     * Determina o formato da instrução.
     * Para instruções em formato 2, utiliza o byte completo; para formato 3/4, analisamos o bit 'e'.
     */
    private int determineInstructionFormat(int fullByte) {
        // Verifica se o opcode corresponde a instruções de formato 1
        if (fullByte == 0xC4 || fullByte == 0xC0 || fullByte == 0xC8 || fullByte == 0xF4 || fullByte == 0xF0 || fullByte == 0xF8) {
            return 1;
        }

        // Verifica se o opcode corresponde a instruções de formato 2
        if (fullByte == 0x04 || fullByte == 0x90) {
            return 2;
        }

        // Para instruções de formato 3/4, lemos o segundo byte para checar bit flag 'e'
        int secondByte = memory.readByte(programCounter + 1) & 0xFF;
        int e = (secondByte & 0x10) >> 4;

        // Se 'e' == 1, é formato 4; caso contrário, formato 3.
        return (e == 1) ? 4 : 3;
    }

    /**
     * Decodifica instruções em formato 2 (2 bytes).
     * O segundo byte contém dois registradores (4 bits cada).
     */
    private int[] decodeFormat2() {
        // Lê o segundo byte da instrução (offset 1 a partir do PC)
        int secondByte = memory.readByte(programCounter + 1) & 0xFF;
        int r1 = (secondByte >> 4) & 0xF;
        int r2 = secondByte & 0xF;
        return new int[]{ r1, r2 };
    }

    /**
     * Decodifica instruções em formato 3/4.
     *
     * Estrutura (formato 3):
     *  - Byte 1: bits 7..2 = opcode; bits 1..0 = n e i
     *  - Byte 2: bit 7 = x; bit 6 = b; bit 5 = p; bit 4 = e; bits 3..0 = 4 bits altos do deslocamento
     *  - Byte 3: 8 bits baixos do deslocamento
     *
     * Estrutura (formato 4, e=1):
     *  - Byte 1: bits 7..2 = opcode; bits 1..0 = n e i
     *  - Byte 2: bit 7 = x; bit 6 = b; bit 5 = p; bit 4 = e; bits 3..0 = 4 bits altos do addr20
     *  - Byte 3: 8 bits médios do addr20
     *  - Byte 4: 8 bits baixos do addr20
     *
     *  Retorna um array com:
     *  [0]=disp/addr (12 ou 20 bits), [1]=x, [2]=b, [3]=p, [4]=e, [5]=n, [6]=i
     *
     * @param n         bit n
     * @param i         bit i
     * @param format    formato da instrução (3 ou 4)
     * @return          Array de inteiros: [0]=disp/addr (12 ou 20 bits), [1]=x, [2]=b, [3]=p, [4]=e, [5]=n, [6]=i
     */
    private int[] decodeFormat3Or4(int n, int i, int format) {
        // Lê o segundo byte
        int secondByte = memory.readByte(programCounter + 1) & 0xFF;

        int x = (secondByte & 0x80) >> 7;
        int b = (secondByte & 0x40) >> 6;
        int p = (secondByte & 0x20) >> 5;
        int e = (secondByte & 0x10) >> 4;

        // Parte alta do deslocamento ou do addr
        int high4 = secondByte & 0x0F;

        if (format == 3) {
            // Lê o terceiro byte (deslocamento baixo)
            int thirdByte = memory.readByte(programCounter + 2) & 0xFF;
            int disp12 = (high4 << 8) | thirdByte;
            return new int[]{ disp12, x, b, p, e, n, i };
        } else {
            // format == 4 => precisamos ler 3º e 4º bytes
            int thirdByte = memory.readByte(programCounter + 2) & 0xFF;
            int fourthByte = memory.readByte(programCounter + 3) & 0xFF;

            // addr20 = high4(4 bits) + thirdByte(8 bits) + fourthByte(8 bits)
            int addr20 = (high4 << 16) | (thirdByte << 8) | fourthByte;
            return new int[]{ addr20, x, b, p, e, n, i };
        }
    }

    /**
     * Calcula o endereço efetivo (EA) a partir do deslocamento e dos bits de modo.
     *
     * - Se p == 1 (PC-relativo): EA = (PC_original + tamanhoInstrução) + disp (com sinal)
     * - Se b == 1 (Base-relativo): EA = (valor do registrador B) + disp
     * - Caso contrário: EA = disp (endereço absoluto)
     * - Se x == 1: EA += (valor do registrador X)
     *
     * Para endereçamento indireto (n=1, i=0):
     *  - Ler a memória nesse EA para obter o endereço final.
     *
     *  Para endereçamento imediato (n=0, i=1):
     *  - O EA pode representar diretamente o operando, mas no caso de dados esse valor ainda é interpretado como literal. (Aqui devolvemos EA mesmo.)
     *
     * @param dispOrAddr    deslocamento (formato3, 12 bits) ou addr (formato4, 20 bits)
     * @param x             bit x
     * @param b             bit b
     * @param p             bit p
     * @param e             bit e (0=Form3, 1=Form4)
     * @param n             bit n
     * @param i             bit i
     * @return              O endereço efetivo da instrução
     */
    private int calculateEffectiveAddress(int dispOrAddr, int x, int b, int p, int e, int n, int i) {
        // Converte o deslocamento/addr para valor com sinal se for formato 3 (12 bits):
        // Formato 4 (20 bits) normalmente não se interpreta como valor negativo (PC-relativo), mas sim absoluto.
        // Segue a convenção do SIC/XE: p=1 não faz sentido se e=1, mas deixamos a checagem se surgir necessidade.
        int addressBase = dispOrAddr;

        if (e == 0) {
            // Formato 3 => disp12 com sinal
            if ((addressBase & 0x800) != 0) { // bit 11 setado
                addressBase = addressBase - 0x1000;
            }
        }
        // Se p==1, soma PC+(tamanho da instrução);
        //  tamanho 3 bytes se e=0; 4 bytes se e=1
        if(p == 1) {
            addressBase += programCounter + ((e == 1) ? 4 : 3);
        } else if (b == 1) {
            addressBase += registers.getRegister("B").getIntValue();
        }
        // Indexado
        if (x == 1) {
            addressBase += registers.getRegister("X").getIntValue();
        }

        // Modo de endereçamento indireto (n=1, i=0):
        if (n == 1 && i == 0) {
            // Verifica se o endereço calculado está alinhado em 3 bytes
            if (addressBase % 3 != 0) {
                throw new IllegalArgumentException("Endereço indireto não alinhado: " + addressBase);
            }
            // Lê a memória para obter endereço final (24 bits).
            byte[] wordBytes = memory.readWord(addressBase / 3);
            return Converter.bytesToInt(wordBytes);
        }

        // Modo de endereçamento imediato (n=0, i=1): O EA é devolvido como está (é valor literal).

        // Modo de endereçamento direto (n=1, i=1): O EA está em addressBase.
        return addressBase;
    }


    /**
     * Limpa ou zera o programCounter interno
     */
    public void resetProgramCounter() {
        // Reseta o registrador PC para 0 e atualiza o programCounter interno
        registers.getRegister("PC").setValue(0);
        programCounter = 0;
    }
}
