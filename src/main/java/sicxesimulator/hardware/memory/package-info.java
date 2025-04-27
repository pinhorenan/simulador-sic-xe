/**
 * Pacote responsável pela simulação da memória da máquina SIC/XE.
 * <p>
 * Fornece a classe {@link sicxesimulator.hardware.memory.Memory}, que:
 * <ul>
 *   <li>Armazena os dados como um vetor de bytes;</li>
 *   <li>Permite leitura e escrita de bytes e de palavras de 3 bytes (big-endian);</li>
 *   <li>Suporta operações de reset, dump em hexadecimal e consulta do tamanho;</li>
 *   <li>Garante validação de limites de acesso e alinhamento onde aplicável.</li>
 * </ul>
 * <p>
 * Este pacote não deve depender de camadas de CPU, controle ou decodificação,
 * atuando unicamente como meio de armazenamento e acesso a dados.
 *
 * @since 1.0.0
 */
package sicxesimulator.hardware.memory;
