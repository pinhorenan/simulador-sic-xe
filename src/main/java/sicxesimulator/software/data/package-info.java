/**
 * Estruturas de dados centrais do simulador SIC/XE.
 *
 * <p>O pacote reúne modelos simples e serializáveis que descrevem:</p>
 *
 * <ul>
 *   <li>{@link sicxesimulator.software.data.AssemblyLine} – linha de código após a 1ª passagem;</li>
 *   <li>{@link sicxesimulator.software.data.IntermediateRepresentation} – resultado da 1ª passagem;</li>
 *   <li>{@link sicxesimulator.software.data.Symbol} e {@link sicxesimulator.software.data.SymbolTable} – gestão de símbolos locais/externos;</li>
 *   <li>{@link sicxesimulator.software.data.MacroDefinition} – definição de macro no pré-processador;</li>
 *   <li>{@link sicxesimulator.software.data.RelocationRecord} – informações usadas por linker/loader;</li>
 *   <li>{@link sicxesimulator.software.data.ObjectFile} – módulo objeto serializável
 *       (resultado do assembler ou linker).</li>
 * </ul>
 *
 * Todas as classes são <em>POJOs</em>, focadas em imutabilidade quando possível e
 * compatíveis com serialização Java padrão.
 */
package sicxesimulator.software.data;
