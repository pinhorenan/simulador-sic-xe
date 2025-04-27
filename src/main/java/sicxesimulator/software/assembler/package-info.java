/**
 * Pacote que implementa o montador (assembler) SIC/XE:
 * - {@link sicxesimulator.software.assembler.Assembler}: orquestra
 *   o fluxo de montagem, executando primeira e segunda passagens
 * - {@link sicxesimulator.software.assembler.AssemblerFirstPass}: analisa
 *   o código-fonte, trata diretivas, preenche a tabela de símbolos
 *   e gera a representação intermediária
 * - {@link sicxesimulator.software.assembler.AssemblerSecondPass}: converte
 *   a representação intermediária em código de máquina, gera registros
 *   Text (T), Modification (M) e grava o arquivo objeto textual (.obj)

 */
package sicxesimulator.software.assembler;
