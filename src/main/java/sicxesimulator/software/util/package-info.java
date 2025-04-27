/**
 * Pacote de utilitários gerais para o simulador SIC/XE.
 *
 * <p>Contém classes de apoio usadas em todo o software, incluindo:</p>
 * <ul>
 *   <li>{@link sicxesimulator.software.util.InstructionSizeCalculator} – fornece cálculo de tamanho em bytes
 *       para diretivas (WORD, RESW, RESB, BYTE) e instruções nos formatos 1–4;</li>
 *   <li>{@link sicxesimulator.software.util.Parser} – interpreta endereços e literais (decimal/hex),
 *       converte operandos BYTE em bytes, resolve endereços de símbolos
 *       e determina o formato de instruções pelo mnemônico.</li>
 * </ul>
 *
 * <p>Essas utilidades são amplamente utilizadas pelos pacotes
 * assembler, linker e loader.</p>
 */
package sicxesimulator.software.util;
