/**
 * Pacote raiz que agrupa todas as funcionalidades relativas à CPU no simulador SIC/XE.
 * <p>
 * Os sub pacotes são:
 * <ul>
 *   <li><b>control</b>: coordenação do ciclo fetch-decode-execute;</li>
 *   <li><b>decoder</b>: decodificação de instruções a partir dos bytes em memória;</li>
 *   <li><b>exec</b>: infraestrutura e implementações de executores de instrução;</li>
 *   <li><b>model</b>: modelos de dados imutáveis (Instruction, ExecutionContext);</li>
 *   <li><b>register</b>: abstração e gerenciamento dos registradores da CPU.</li>
 * </ul>
 * <p>
 * Este pacote não contém classes próprias, servindo apenas como ponto de
 * organização e documentação da arquitetura de CPU do simulador.
 *
 * @since 1.0.0
 */
package sicxesimulator.hardware.cpu;
