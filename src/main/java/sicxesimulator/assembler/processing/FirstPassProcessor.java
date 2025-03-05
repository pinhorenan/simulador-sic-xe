package sicxesimulator.assembler.processing;

import sicxesimulator.assembler.models.AssemblyLine;
import sicxesimulator.assembler.models.IntermediateRepresentation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class FirstPassProcessor {
    private static final Logger logger = Logger.getLogger(FirstPassProcessor.class.getName());

    // Conjunto de mnemônicos válidos, incluindo diretivas básicas.
    private static final Set<String> VALID_MNEMONICS = new HashSet<>(Arrays.asList(
            "START", "END", "BYTE", "WORD", "RESB", "RESW", "BASE", "NOBASE", "EQU", "LTORG",
            "ADD", "ADDR", "AND", "CLEAR", "COMP", "COMPR", "DIV", "DIVR",
            "J", "JEQ", "JGT", "JLT", "JSUB", "LDA", "LDB", "LDCH", "LDL", "LDS",
            "LDT", "LDX", "MUL", "MULR", "OR", "RMO", "RSUB", "SHIFTL", "SHIFTR",
            "STA", "STB", "STCH", "STL", "STS", "STT", "STX", "SUB", "SUBR", "TIX",
            "TIXR"
    ));

    // Contador de localização e endereço inicial (em palavras)
    private int locationCounter;
    private int startAddress;

    public IntermediateRepresentation process(List<String> sourceLines) {
        // Inicializa os contadores e a representação intermediária
        locationCounter = 0;
        startAddress = 0;
        boolean endFound = false;
        IntermediateRepresentation ir = new IntermediateRepresentation();
        int lineNumber = 0;

        for (String line : sourceLines) {
            lineNumber++;
            line = line.trim();

            // Ignora linhas vazias e comentários (iniciados por ".")
            if (line.isEmpty() || line.startsWith(".")) {
                continue;
            }

            // Divide a linha em até 3 partes: rótulo, mnemônico e operando.
            String[] parts = line.split("\\s+", 3);
            String label = null;
            String mnemonic = null;
            String operand = null;

            if (parts.length > 0) {
                // Se o primeiro token for um mnemônico, não há rótulo.
                if (isMnemonic(parts[0])) {
                    mnemonic = parts[0];
                    if (parts.length > 1) {
                        operand = parts[1];
                    }
                } else if (parts.length > 1 && isMnemonic(parts[1])) {
                    label = parts[0];
                    mnemonic = parts[1];
                    if (parts.length > 2) {
                        operand = parts[2];
                    }
                }
            }

            if (mnemonic == null) {
                throw new IllegalArgumentException("Linha inválida na linha " + lineNumber + ": " + line);
            }

            // Processa a diretiva START: define o endereço inicial.
            if (mnemonic.equalsIgnoreCase("START")) {
                try {
                    startAddress = Integer.parseInt(operand, 16);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Formato inválido para START na linha " + lineNumber + ": " + operand);
                }
                locationCounter = startAddress;
                ir.setStartAddress(startAddress);
                if (label != null) {
                    ir.addSymbol(label, locationCounter);
                }
                continue;
            }

            // Processa a diretiva END: encerra a passagem.
            // Processa a diretiva END: encerra a passagem.
            if (mnemonic.equalsIgnoreCase("END")) {
                endFound = true;
                ir.setFinalAddress(locationCounter); // Define o endereço final como o contador de localização atual

                if (operand != null && !operand.trim().isEmpty()) { // Verifica se o operando não é nulo nem vazio
                    try {
                        int finalAddress = Integer.parseInt(operand.trim(), 16); // Tenta converter o operando para hexadecimal
                        ir.setFinalAddress(finalAddress); // Define o endereço final com o valor do operando
                        logger.info("Diretiva END com operando '" + operand + "' encontrada na linha " + lineNumber);
                    } catch (NumberFormatException e) {
                        // Log de erro se o operando não for um valor hexadecimal válido
                        logger.warning("Operando inválido na diretiva END: '" + operand + "' na linha " + lineNumber + ". Usando contador de localização como endereço final.");
                    }
                } else {
                    logger.info("Diretiva END sem operando encontrada na linha " + lineNumber);
                }

                // Não adiciona uma AssemblyLine para a diretiva END.
                continue;
            }
            // Diretivas ainda não implementadas: EQU, BASE, NOBASE, LTORG.
            if (mnemonic.equalsIgnoreCase("EQU") ||
                    mnemonic.equalsIgnoreCase("BASE") ||
                    mnemonic.equalsIgnoreCase("NOBASE") ||
                    mnemonic.equalsIgnoreCase("LTORG")) {
                logger.info("Diretiva " + mnemonic + " encontrada na linha " + lineNumber + " ainda não implementada.");
                continue;
            }

            // Se o operando for um literal (começando com "="), loga a ocorrência.
            if (operand != null && operand.startsWith("=")) {
                logger.info("Literal " + operand + " encontrado na linha " + lineNumber + " - tratamento não implementado.");
            }

            // Se houver rótulo, registra-o na tabela de símbolos.
            if (label != null) {
                ir.addSymbol(label, locationCounter);
            }

            // Calcula o tamanho da instrução (em palavras) e adiciona a linha processada à IR.
            int size = getInstructionSize(mnemonic, operand);
            AssemblyLine asmLine = new AssemblyLine(label, mnemonic, operand, locationCounter, lineNumber);
            ir.addAssemblyLine(asmLine);

            // Atualiza o contador de localização.
            locationCounter += size;
        }

        // Verifica se a diretiva END foi encontrada.
        if (!endFound) {
            throw new IllegalArgumentException("Diretiva END não encontrada.");
        }

        return ir;
    }

    /**
     * Reinicia o estado interno do processador.
     */
    public void reset() {
        locationCounter = 0;
        startAddress = 0;
    }

    /**
     * Verifica se o token é um mnemônico válido.
     *
     * @param token Token a ser verificado.
     * @return true se for mnemônico válido, false caso contrário.
     */
    private boolean isMnemonic(String token) {
        return VALID_MNEMONICS.contains(token.toUpperCase());
    }

    /**
     * Calcula o tamanho da instrução/diretiva (em palavras).
     * Para WORD, BYTE, RESW e RESB, o cálculo é feito conforme o operando.
     * Instruções padrão são consideradas de 1 palavra.
     *
     * @param mnemonic Mnemônico da instrução/diretiva.
     * @param operand  Operando associado (pode ser null).
     * @return Tamanho da instrução em palavras.
     */
    private int getInstructionSize(String mnemonic, String operand) {
        if (mnemonic.equalsIgnoreCase("WORD")) {
            return 1;
        }
        if (mnemonic.equalsIgnoreCase("RESW")) {
            return Integer.parseInt(operand);
        }
        if (mnemonic.equalsIgnoreCase("RESB")) {
            int numBytes = Integer.parseInt(operand);
            // Arredonda para cima para manter alinhamento de palavras (3 bytes cada).
            return (numBytes + 2) / 3;
        }
        if (mnemonic.equalsIgnoreCase("BYTE")) {
            int bytes;
            if (operand.startsWith("C'") && operand.endsWith("'")) {
                bytes = operand.length() - 3; // Ex.: C'ABC' tem 3 bytes.
            } else if (operand.startsWith("X'") && operand.endsWith("'")) {
                bytes = (operand.length() - 3 + 1) / 2; // Converte dígitos hexadecimais em bytes.
            } else {
                throw new IllegalArgumentException("Formato inválido para BYTE na linha: " + operand);
            }
            return (bytes + 2) / 3;
        }
        // Para demais instruções (formato 3, por exemplo), assume 1 palavra.
        return 1;
    }
}
