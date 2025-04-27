/**
 * Pacote que agrupa e modela os registradores da CPU SIC/XE.
 * <p>
 * Principais componentes:
 * <ul>
 *   <li><b>RegisterSet</b>: coleção imutável de registradores padrão
 *       (A, X, L, B, S, T, F, PC, SW), com métodos de acesso por nome
 *       e limpeza em lote.</li>
 *   <li><b>Register</b>: representação de um registrador individual
 *       (24 ou 48 bits), com leitura/escrita mascarada e validação
 *       de tamanho via {@code Checker}.</li>
 * </ul>
 * <p>
 * Não contém lógica de decodificação ou execução de instruções;
 * serve apenas como abstração de armazenamento de estado da CPU.
 *
 * @since 1.0.0
 */
package sicxesimulator.hardware.cpu.register;
