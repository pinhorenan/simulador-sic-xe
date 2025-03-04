package sicxesimulator.assembler;

import java.util.*;

public class Assembler {

    // Tabela de símbolos: mapeia rótulos para endereços
    @SuppressWarnings("FieldMayBeFinal") // TODO: Verificar se pode ser atributo final.
    private Map<String, Integer> symbolTable;
    // Lista de linhas processadas (com informações de label, mnemonic, operando e endereço)
    @SuppressWarnings("FieldMayBeFinal") // TODO: Verificar se pode ser atributo final.
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
     * Recebe o código assembly (como lista de linhas e retorna o código objeto (array de bytes).
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
            String mnemonic;
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
                assert operand != null;
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
        return switch (mnemonic.toUpperCase()) {
            case "ADD" -> 0x18;
            case "ADDR" -> 0x90;
            case "AND" -> 0x40;
            case "CLEAR", "LDX" -> 0x04;
            case "COMP" -> 0x28;
            case "COMPR" -> 0xA0;
            case "DIV" -> 0x24;
            case "DIVR" -> 0x9C;
            case "J" -> 0x3C;
            case "JEQ" -> 0x30;
            case "JGT" -> 0x34;
            case "JLT" -> 0x38;
            case "JSUB" -> 0x48;
            case "LDA" -> 0x00;
            case "LDB" -> 0x68;
            case "LDCH" -> 0x50;
            case "LDL" -> 0x08;
            case "LDS" -> 0x6C;
            case "LDT" -> 0x74;
            case "MUL" -> 0x20;
            case "MULR" -> 0x98;
            case "OR" -> 0x44;
            case "RMO" -> 0xAC;
            case "RSUB" -> 0x4C;
            case "SHIFTL" -> 0xA4;
            case "SHIFTR" -> 0xA8;
            case "STA" -> 0x0C;
            case "STB" -> 0x78;
            case "STCH" -> 0x54;
            case "STL" -> 0x14;
            case "STS" -> 0x7C;
            case "STT" -> 0x84;
            case "STX" -> 0x10;
            case "SUB" -> 0x1C;
            case "SUBR" -> 0x94;
            case "TIX" -> 0x2C;
            case "TIXR" -> 0xB8;
            default -> throw new IllegalArgumentException("Instrução desconhecida: " + mnemonic);
        };
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

    /**
     * Formata o código objeto em uma string legível
      */
    public String formatObjectCode(byte[] objectCode) {
        StringBuilder formattedCode = new StringBuilder();
        for (byte b : objectCode) {
            // Adiciona cada byte em hexadecimal, com espaço entre eles.
            formattedCode.append(String.format("%02X ", b & 0xFF));
        }
        return formattedCode.toString().trim();
    }

    ///  GETTERS

    public int getStartAddress() {
        return startAddress;
    }

    public Map<String, Integer> getSymbolTable() {
        return symbolTable;
    }

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

    public void reset() {
        symbolTable.clear();
    }

}