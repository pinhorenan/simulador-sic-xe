/**
 * Pacote que implementa o loader SIC/XE,
 * responsável por carregar o programa na memória principal,
 * copiar o código objeto e aplicar relocação quando necessário.
 *
 * <p>Principais responsabilidades:</p>
 * <ul>
 *   <li>Determinar o endereço efetivo de carga (absoluto ou baseado em parâmetro).</li>
 *   <li>Verificar se o programa cabe na memória.</li>
 *   <li>Copiar o array de bytes para a memória.</li>
 *   <li>Ajustar endereços na {@link sicxesimulator.software.data.SymbolTable}.</li>
 *   <li>Aplicar cada {@link sicxesimulator.software.data.RelocationRecord}
 *       diretamente na memória (incluindo correção PC–relativa).</li>
 * </ul>
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * Loader loader = new Loader();
 * loader.loadObjectFile(objectFile, memory, 0x1000);
 * }</pre>
 */
package sicxesimulator.software.loader;
