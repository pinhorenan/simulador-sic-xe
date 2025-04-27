/**
 * Pacote que define os modelos de dados usados no pipeline de execução
 * da CPU SIC/XE.
 * <p>
 * Contém:
 * <ul>
 *   <li><b>Instruction</b>: representa uma instrução já decodificada,
 *       com opcode, operands, formato, flag de indexação e endereço efetivo;
 *       fornece utilitário para obter o tamanho em bytes.</li>
 *   <li><b>ExecutionContext</b>: encapsula dados imutáveis passados a cada
 *       {@link sicxesimulator.hardware.cpu.exec.InstructionExecutor},
 *       incluindo operandos, informações de indexação, registradores e memória.</li>
 * </ul>
 * <p>
 * Este pacote não contém lógica de controle ou decodificação, servindo apenas
 * como núcleo de abstração para transporte de dados entre componentes
 * do simulador.
 *
 * @since 1.0.0
 */
package sicxesimulator.hardware.cpu.model;
