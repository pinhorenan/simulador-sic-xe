/**
 * Pacote que implementa o linker SIC/XE,
 * responsável por resolver símbolos externos, aplicar relocação
 * (opcionalmente final ou pendente) e produzir o arquivo de objeto final.
 *
 * <p>Contém:</p>
 * <ul>
 *   <li>{@link sicxesimulator.software.linker.Linker} – classe principal que
 *       coordena as fases de atribuição de bases, validação de imports,
 *       geração de código final e escrita de arquivos .obj/.meta.</li>
 *   <li>{@link sicxesimulator.software.data.RelocationRecord} – modelo de registro
 *       de relocação usado quando não há relocação final.</li>
 *   <li>Classes internas de suporte:
 *     <ul>
 *       <li>{@link sicxesimulator.software.linker.Linker.LinkerContext} – contexto
 *           intermediário com bases, tabela global e tamanho total.</li>
 *       <li>{@link sicxesimulator.software.linker.Linker.FinalCodeData} – estrutura
 *           com código final, tabela global de símbolos e registros pendentes.</li>
 *       <li>{@link sicxesimulator.software.linker.Linker.TBlock} – bloco de texto (T-record)
 *           para escrita dos registros de texto no .obj.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * Linker linker = new Linker();
 * ObjectFile linked = linker.linkModules(
 *     listOfObjectFiles,
 *     finalRelocation= false,
 *     loadAddress= 0x1000,
 *     outputFileName= "PROG"
 * );
 * }</pre>
 */

package sicxesimulator.software.linker;
