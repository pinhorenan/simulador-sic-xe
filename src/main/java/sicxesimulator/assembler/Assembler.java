package sicxesimulator.assembler;

import java.util.*;

public class Assembler {

    // Tabela de símbolos: mapeia rótulos para endereços
    private Map<String, Integer> symbolTable;
    // Lista de linhas processadas (com informações de label, mnemonic, operando e endereço)
    private List<InstructionLine> instructionLines;
    // Endereço de início do programa e contador de localização.
    private int startAddress;
    private int locationCounter;

    public Assembler() {
        symbolTable = new HashMap<>();
        instructionLines = new ArrayList<>();
        startAddress = 0;
        locationCounter = 0;
    }

    /**
     * Método principal que recebe o código assembly (como lista de linhas e retorna o código objeto (array de bytes).
     */
    public byte[] assemble(List<String> sourceLines) {
        passOne(sourceLines);       // Primeira passagem: montar a tabela de símbolos e definir endereços.
        return passTwo();           // Segunda passagem: gerar o código objeto.
    }

    /**
     * Passagem 1: Processa cada linha do código-fonte para construir a tabela de símbolos e calcular os endereços das instruções.
     */
    private void passOne(List<String> sourceLines) {
        int lineNumber = 0;
        for (String line : sourceLines) {
            lineNumber++;
            line = line.trim();

            // Ignora linhas em branco ou comentários (aqui assumindo que linhas iniciadas por "." são comentários)
            if (line.isEmpty() || line.startsWith(".")) {
                continue;
            }

            // Divide a linha em tokens; aqui supomos o formato: [label] mnemonic [operando]
            String[] parts = line.split("\\s+");
            String label = null;
            String mnemonic = null;
            String operand = null;

            if (parts.length == 3) {
                label = parts[0];
                mnemonic = parts[1];
                operand = parts[2];
            } else if (parts.length == 2) {
                mnemonic = parts[0];
                operand = parts[1];
            } else if (parts.length == 1) {
                mnemonic = parts[0];
            } else {
                // Se a linha não se encaixar no formato esperado, ignore por enquanto.
                System.out.println("DEBUG: Durante a primeira passagem, na linha número: " + line + "não foi encontrado o formato esperado.");
                continue;
            }

            ///  PROCESSAMENTO DE DIRETIVAS

            // Se encontrar a diretiva START, define o endereço de início do programa.
            if (mnemonic.equalsIgnoreCase("START")) {
                startAddress = Integer.parseInt(operand, 16); // TODO: assumindo que o operando esteja em hexadecimal;
                locationCounter = startAddress;
            } else {
                // Se houver rótulo, adiciona-o à tabela de símbolos.
                if (label != null && !label.isEmpty()) {
                    symbolTable.put(label, locationCounter);
                }
                // Calcula o tamanho da instrução ou diretiva e atualiza o contador de localização.
                int size = getInstructionSize(mnemonic, operand);
                InstructionLine instLine = new InstructionLine(label, mnemonic, operand, locationCounter, lineNumber);
                instructionLines.add(instLine);
                locationCounter += size;
            }
        }
    }

    /**
     * Retorna o tamanho (em bytes) da instrução ou diretiva.
     * Para simplificar:
     * - Instruções são assumidas como formato 3 (bytes).
     * - WORD = 3 bytes.
     * - RESW = 3 * valor do operando.
     * - BYTE pode ser tratado de forma especial (aqui simplificado) TODO!
     */
    private int getInstructionSize(String mnemonic, String operand) {
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return 3;
        } else if (mnemonic.equalsIgnoreCase("RESW")) {
            int count = Integer.parseInt(operand);
            return 3 * count;
        } else if (mnemonic.equalsIgnoreCase("BYTE")) {
            // Versão simplificada: se estiver no formato C'...', assume que cada caractere é 1 byte.
            if (operand.startsWith("C'") && operand.endsWith("'")) {
                String str = operand.substring(2, operand.length() - 1);
                return str.length();
            } else if (operand.startsWith("X'") && operand.endsWith("'")) {
                String hex = operand.substring(2, operand.length() - 1);
                return hex.length() / 2;
            }
        }
        // Padrão: instrução de 3 bytes.
        return 3;
    }

    /**
     * Passagem 2: Gera o código objeto (array de bytes) usando a tabela de símbolos e convertendo cada instrução.
     */
    private byte[] passTwo() {
        // Calcula o tamanho total do código objeto.
        int programLength = locationCounter - startAddress;
        byte[] objectCode = new byte[programLength];

        for (InstructionLine inst : instructionLines) {
            int offset = inst.address - startAddress; // Posição relativa no array de saída.
            byte[] code = generateObjectCode(inst);
            System.arraycopy(code, 0, objectCode, offset, code.length);
        }
        return objectCode;
    }

    /**
     * Gera o código objeto para uma linha de instrução ou diretiva.
     * Para diretivas WORD, RESW e BYTE, gera os dados correspondentes.
     * Para instruções, monta uma instrução de 3 bytes de forma simplificada.
     */
    private byte[] generateObjectCode(InstructionLine inst) {
        // Tratamento de diretivas:
        if (inst.mnemonic.equalsIgnoreCase("WORD")) {
            int value = Integer.parseInt(inst.operand);
            return intTo3Bytes(value);
        } else if (inst.mnemonic.equalsIgnoreCase("RESW")) {
            int count = Integer.parseInt(inst.operand);
            return new byte[3 * count]; // Assume que os bytes são zerados.
        } else if (inst.mnemonic.equalsIgnoreCase("BYTE")) {
            if (inst.operand.startsWith("C'") && inst.operand.endsWith("'")) {
                String hex = inst.operand.substring(2, inst.operand.length() - 1);
                return hexStringToByteArray(hex);
            }
        }
        // Tratamento de instruções:
        // Aqui, para simplificar, supomos que as instruções são do formato 3 (3 bytes).
        int opcode = getOpcode(inst.mnemonic);
        // Determina o endereço do operando: se for um rótulo, pega o endereço na tabela de símbolos.
        // Caso contrário, assume que é um número (em hexadecimal).
        int operandAddress = 0;
        if (inst.operand != null && !inst.operand.isEmpty()) {
            if (symbolTable.containsKey(inst.operand)) {
                operandAddress = symbolTable.get(inst.operand);
            } else {
                operandAddress = Integer.parseInt(inst.operand, 16);
            }
        }
        // Construção simplificada da instrução:
        // - Primeiro byte: opcode.
        // - Próximos dois bytes: endereço (deslocamento de 12 bits).
        // Nota: em uma implementação completa, vai ser necessário definir as flags (n, i, x, b, p, e).
        byte byte1 = (byte) opcode;
        int disp = operandAddress & 0xFFF;
        byte byte2 = (byte) ((disp >> 8) & 0xFF);
        byte byte3 = (byte) (disp & 0xFF);
        return new byte[]{byte1, byte2, byte3};
    }

    /**
     * Mapeia mnemônicos para as opcodes correspondentes.
     */
    private int getOpcode(String mnemonic) {
        switch (mnemonic.toUpperCase()) {
            case "ADD":
                return 0x18;
            case "ADDR":
                return 0x90;
            case "AND":
                return 0x40;
            case "CLEAR":
                return 0x04;
            case "COMP":
                return 0x28;
            case "COMPR":
                return 0xA0;
            case "DIV":
                return 0x24;
            case "DIVR":
                return 0x9C;
            case "J":
                return 0x3C;
            case "JEQ":
                return 0x30;
            case "JGT":
                return 0x34;
            case "JLT":
                return 0x38;
            case "JSUB":
                return 0x48;
            case "LDA":
                return 0x00;
            case "LDB":
                return 0x68;
            case "LDCH":
                return 0x50;
            case "LDL":
                return 0x08;
            case "LDS":
                return 0x6C;
            case "LDT":
                return 0x74;
            case "LDX":
                return 0x04;
            case "MUL":
                return 0x20;
            case "MULR":
                return 0x98;
            case "OR":
                return 0x44;
            case "RMO":
                return 0xAC;
            case "RSUB":
                return 0x4C;
            case "SHIFTL":
                return 0xA4;
            case "SHIFTR":
                return 0xA8;
            case "STA":
                return 0x0C;
            case "STB":
                return 0x78;
            case "STCH":
                return 0x54;
            case "STL":
                return 0x14;
            case "STS":
                return 0x7C;
            case "STT":
                return 0x84;
            case "STX":
                return 0x10;
            case "SUB":
                return 0x1C;
            case "SUBR":
                return 0x94;
            case "TIX":
                return 0x2C;
            case "TIXR":
                return 0xB8;

            default:
                throw new IllegalArgumentException("Instrução desconhecida: " + mnemonic);
        }
    }

    /**
     * Converte um valor inteiro em um array de 3 bytes (24 bits) em big-endian.
     */
    private byte[] intTo3Bytes(int value) {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) ((value >> 16) & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) (value & 0xFF);
        return bytes;
    }

    /**
     * Converte uma string hexadecimal em um array de bytes.
     */
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 15));
        }
        return data;
    }

    ///  CLASSES INTERNAS AUXILIARES

    /**
     * Classe interna para representar uma linha de instrução ou diretiva no código assembly.
     */
    private static class InstructionLine {
        String label;
        String mnemonic;
        String operand;
        int address;
        int lineNumber;

        public InstructionLine(String label, String mnemonic, String operand, int address, int lineNumber) {
            this.label = label;
            this.mnemonic = mnemonic;
            this.operand = operand;
            this.address = address;
            this.lineNumber = lineNumber;
        }
    }

    ///  GETTERS

    public int getStartAddress() {
        return startAddress;
    }

    public Map<String, Integer> getSymbolTable() {
        return symbolTable;
    }
}