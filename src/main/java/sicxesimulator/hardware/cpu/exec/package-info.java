/**
 * Pacote que define a infraestrutura para execução de instruções SIC/XE.
 * <p>
 * Este pacote fornece:
 * <ul>
 *   <li><b>InstructionExecutor</b>: interface funcional que representa um executor
 *       de instrução, recebendo um {@link sicxesimulator.hardware.cpu.model.ExecutionContext}
 *       e retornando um log de execução.</li>
 *   <li><b>BaseExecutor</b>: classe abstrata com utilitários comuns (tratamento
 *       de endereçamento, atualização do CC, etc.) para facilitar a criação
 *       de executores concretos.</li>
 * </ul>
 * <p>
 * As implementações específicas das instruções estão organizadas em sub pacotes:
 * <ul>
 *   <li><b>arith</b>: instruções aritméticas e registrador-aritméticas</li>
 *   <li><b>jump</b>: instruções de salto</li>
 *   <li><b>load</b>: instruções de carregamento</li>
 *   <li><b>logic</b>: instruções lógicas e de comparação</li>
 *   <li><b>store</b>: instruções de armazenamento</li>
 *   <li><b>sys</b>: instruções de sistema (I/O, chamadas de serviço, etc.)</li>
 * </ul>
 * <p>
 * Cada sub pacote contém classes que implementam {@link sicxesimulator.hardware.cpu.exec.InstructionExecutor}
 * para os opcodes correspondentes, registradas automaticamente pelo
 * {@code ExecutionDispatcher}.
 *
 * @since 1.0.0
 */
package sicxesimulator.hardware.cpu.exec;
