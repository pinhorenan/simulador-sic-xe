package sicxesimulator.hardware.cpu;

import sicxesimulator.hardware.Memory;
import sicxesimulator.hardware.data.Instruction;
import sicxesimulator.utils.Convert;

/**
 * Responsável por decodificar instruções da memória a partir do endereço apontado pelo registrador PC.
 *
 * Implementa a lógica de identificação de formato (1, 2, 3 ou 4), extração dos campos relevantes
 * da instrução e cálculo do endereço efetivo (EA) para execução.
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
     * Decodifica a instrução na posição atual do PC, lendo diretamente da memória.
     *
     * Atualiza internamente o programCounter e, quando necessário, avança o PC.
     * Identifica o formato da instrução e extrai campos como opcode, registradores,
     * bits de modo (n, i, x, b, p, e) e o endereço efetivo.
     *
     * @return {@link Instruction} completamente decodificada.
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
     * Determina o formato da instrução a partir do primeiro byte.
     *
     * Formato 1 e 2 são identificados diretamente por opcode.
     * Para formatos 3 e 4, o segundo byte da instrução é analisado para obter o bit 'e'.
     *
     * @param fullByte Primeiro byte da instrução.
     * @return 1, 2, 3 ou 4, dependendo do formato identificado.
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
     * Decodifica uma instrução de formato 2 (2 bytes).
     *
     * O segundo byte contém dois registradores codificados em 4 bits cada.
     *
     * @return Array [r1, r2] com os códigos dos registradores envolvidos.
     */
    private int[] decodeFormat2() {
        // Lê o segundo byte da instrução (offset 1 a partir do PC)
        int secondByte = memory.readByte(programCounter + 1) & 0xFF;
        int r1 = (secondByte >> 4) & 0xF;
        int r2 = secondByte & 0xF;
        return new int[]{ r1, r2 };
    }

    /**
     * Decodifica instruções nos formatos 3 ou 4, extraindo bits de controle e deslocamento/endereço.
     *
     * @param n bit n (modo de endereçamento)
     * @param i bit i (modo de endereçamento)
     * @param format Formato da instrução (3 ou 4)
     * @return Array de inteiros:
     *         [0] = disp (12 bits) ou addr (20 bits),
     *         [1..6] = bits x, b, p, e, n, i (nessa ordem)
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
     * Calcula o endereço efetivo (EA) da instrução com base no deslocamento/endereço
     * e nos bits de modo (x, b, p, e, n, i).
     *
     * Aplica os modos PC-relative, base-relative, indexado, imediato e indireto conforme necessário.
     *
     * @param dispOrAddr Campo de endereço (12 bits para formato 3 ou 20 bits para formato 4).
     * @param x Bit de indexação.
     * @param b Bit de base-relative.
     * @param p Bit de PC-relative.
     * @param e Bit que indica o formato (0 = Form3, 1 = Form4).
     * @param n Bit 'n' de endereçamento.
     * @param i Bit 'i' de endereçamento.
     * @return Endereço efetivo resultante da decodificação.
     */
    private int calculateEffectiveAddress(int dispOrAddr, int x, int b, int p, int e, int n, int i) {
        // Se for modo imediato (n==0, i==1), devolve o valor literal (aplicando extensão de sinal para formato 3, se necessário)
        if (n == 0 && i == 1) {
            int immediate = dispOrAddr;
            if (e == 0) { // Formato 3: 12 bits com sinal
                if ((immediate & 0x800) != 0) { // bit 11 setado
                    immediate = immediate - 0x1000;
                }
            }
            return immediate;
        }

        // Caso contrário, trata os modos direto e indireto
        int addressBase = dispOrAddr;
        if (e == 0) { // Formato 3: realiza extensão de sinal para 12 bits
            if ((addressBase & 0x800) != 0) { // se bit 11 estiver setado
                addressBase = addressBase - 0x1000;
            }
        }

        // Aplica endereçamento relativo ao PC se p==1
        if (p == 1) {
            addressBase += programCounter + ((e == 1) ? 4 : 3);
        } else if (b == 1) { // ou endereçamento base se b==1
            addressBase += registers.getRegister("B").getIntValue();
        }

        // Aplica indexação se x==1
        if (x == 1) {
            addressBase += registers.getRegister("X").getIntValue();
        }

        // Se for endereçamento indireto (n==1, i==0), obtém o endereço final lendo a memória
        if (n == 1 && i == 0) {
            if (addressBase % 3 != 0) {
                throw new IllegalArgumentException("Endereço indireto não alinhado: " + addressBase);
            }
            byte[] wordBytes = memory.readWord(addressBase / 3);
            return Convert.bytesToInt(wordBytes);
        }

        // Caso seja endereçamento direto (n==1, i==1), retorna o endereço calculado
        return addressBase;
    }

    /**
     * Reinicia o contador de programa (PC) para zero, tanto no registrador quanto internamente.
     */
    public void resetProgramCounter() {
        // Reseta o registrador PC para 0 e atualiza o programCounter interno
        registers.getRegister("PC").setValue(0);
        programCounter = 0;
    }
}
