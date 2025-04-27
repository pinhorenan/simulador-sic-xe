/**
 * Pacote que integra os componentes de hardware do simulador SIC/XE,
 * reunindo CPU e memória em uma abstração de máquina completa.
 * <p>
 * Componentes principais:
 * <ul>
 *   <li><b>Machine</b>: representa a instância completa da máquina,
 *       combinando {@link sicxesimulator.hardware.memory.Memory} e
 *       {@link sicxesimulator.hardware.cpu.control.ControlUnit}; fornece
 *       métodos para executar ciclos de CPU, resetar estado e redimensionar
 *       a memória dinamicamente.</li>
 * </ul>
 * <p>
 * Serve como fachada de alto nível para uso pela camada de software
 * (por exemplo, loaders, interface gráfica ou testes), sem expor detalhes
 * de decodificação ou execução de instruções.
 *
 * @since 1.0.0
 */
package sicxesimulator.hardware.system;
