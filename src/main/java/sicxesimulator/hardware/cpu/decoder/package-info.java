/**
 * Pacote responsável pela decodificação de instruções da CPU no simulador SIC/XE.
 * <p>
 * As principais responsabilidades deste pacote são:
 * <ul>
 *   <li><b>InstructionDecoder</b>: lê bytes da memória a partir do Program Counter,
 *       identifica o formato da instrução (1, 2, 3 ou 4), extrai campos de registradores,
 *       flags de endereçamento (n, i, x, b, p, e) e calcula o endereço efetivo.</li>
 * </ul>
 * <p>
 * Este pacote não deve conhecer detalhes de execução ou de controle de fluxo,
 * provendo apenas as estruturas e algoritmos necessários para traduzir
 * fluxos de bytes em objetos {@code Instruction} prontos para execução.
 *
 * @since 1.0.0
 */
package sicxesimulator.hardware.cpu.decoder;
