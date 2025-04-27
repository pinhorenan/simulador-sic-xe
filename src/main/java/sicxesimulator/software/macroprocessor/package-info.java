/**
 * Pacote que implementa o macroprocessador SIC/XE,
 * responsável por detectar definições de macros (MACRO...MEND),
 * armazená-las, expandir invocações substituindo parâmetros
 * por argumentos reais e gerar o código-fonte expandido.
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * MacroProcessor mp = new MacroProcessor();
 * mp.process("entrada.asm", "saida_expanded.asm");
 * }</pre>
 */
package sicxesimulator.software.macroprocessor;
