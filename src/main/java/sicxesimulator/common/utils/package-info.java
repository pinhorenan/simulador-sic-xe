/**
 * Utilitários de uso geral do simulador SIC/XE.
 *
 * <p>Principais responsabilidades:</p>
 * <ul>
 *   <li><b>Validação&nbsp; e mapeamentos</b> – {@link sicxesimulator.common.utils.Checker},
 *       {@link sicxesimulator.common.utils.Mapper};</li>
 *   <li><b>Constantes globais</b> – {@link sicxesimulator.common.utils.Constants};</li>
 *   <li><b>Conversões binário/hex/bytes</b> – {@link sicxesimulator.common.utils.Convert};</li>
 *   <li><b>Entrada/saída de arquivos</b> – {@link sicxesimulator.common.utils.FileUtils};</li>
 *   <li><b>Registro de logs</b> – {@link sicxesimulator.common.utils.Logger}.</li>
 * </ul>
 *
 * <p>Estas classes são <em>stateless</em> (métodos 'static') e
 * podem ser utilizadas livremente por qualquer módulo da aplicação –
 * hardware, software ou UI.</p>
 */
package sicxesimulator.common.utils;
