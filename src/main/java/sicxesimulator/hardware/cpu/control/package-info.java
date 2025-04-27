/**
 * Contém as classes responsáveis pelo controle do ciclo de instruções
 * da CPU no simulador SIC/XE.
 * <p>
 * As principais responsabilidades deste pacote são:
 * <ul>
 *   <li><b>ControlUnit</b>: coordena o ciclo fetch-decode-execute,
 *       gerencia o Program Counter, estado de halted, histórico de execução
 *       e limpeza de registradores.</li>
 *   <li><b>ExecutionDispatcher</b>: faz o roteamento de opcodes para
 *       instâncias de {@code InstructionExecutor}, carregando as
 *       implementações básicas de instruções aritméticas, lógicas, de
 *       salto, load/store e syscalls.</li>
 * </ul>
 * <p>
 * Este pacote não deve depender diretamente de camadas superiores
 * (UI, software) nem de detalhes de decodificação e execução de instruções,
 * sendo reuseable para testes de unidade e possíveis extensões via
 * registro dinâmico de executores.
 *
 * @since 1.0.0
 */
package sicxesimulator.hardware.cpu.control;
