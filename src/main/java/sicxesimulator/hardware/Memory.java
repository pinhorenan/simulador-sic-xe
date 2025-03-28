package sicxesimulator.hardware;

import java.util.Arrays;

/**
 * Representa a memória da máquina SIC/XE.
 *
 * A memória é tratada como um vetor de bytes, acessível por palavras (3 bytes)
 * ou bytes individuais. Oferece métodos de leitura, escrita, reset e mapeamento.
 */
public class Memory {
	private final byte[] memory;
	private final int memorySize;

	public Memory(int size) {
		this.memorySize = size;
		this.memory = new byte[size];
	}

	/**
	 * Lê uma palavra (3 bytes) a partir do índice especificado.
	 *
	 * @param wordIndex Índice da palavra (base 0).
	 * @return Vetor de 3 bytes lidos da memória.
	 * @throws IndexOutOfBoundsException Se a leitura ultrapassar os limites da memória.
	 */
	public byte[] readWord(int wordIndex) {
		if (wordIndex * 3 + 3 > memorySize) {
			throw new IndexOutOfBoundsException("Tentativa de ler fora dos limites da memoria.");
		}
		byte[] word = new byte[3];
		System.arraycopy(memory, wordIndex * 3, word, 0, 3);
		return word;
	}

	/**
	 * Lê um único byte da memória no endereço especificado.
	 *
	 * @param byteAddress Endereço do byte.
	 * @return Valor do byte lido, como inteiro (0 a 255).
	 * @throws IndexOutOfBoundsException Se o endereço for inválido.
	 */
	public int readByte(int byteAddress) {
		if (byteAddress >= memorySize) {
			throw new IndexOutOfBoundsException("Tentativa de ler fora dos limites da memoria.");
		}
        // Retorna o byte como valor positivo (0-255)
        return memory[byteAddress] & 0xFF;
	}

	/**
	 * Escreve uma palavra (3 bytes) na memória no índice especificado.
	 *
	 * @param wordIndex Índice da palavra.
	 * @param word Array de 3 bytes a serem gravados.
	 * @throws IllegalArgumentException Se o array não tiver exatamente 3 bytes.
	 * @throws IndexOutOfBoundsException Se a escrita ultrapassar o limite da memória.
	 */
	public void writeWord(int wordIndex, byte[] word) {
		if (word.length != 3) {
			throw new IllegalArgumentException("Uma palavra deve ter exatamente 3 bytes.");
		}
		if (wordIndex * 3 + 3 > memorySize) {
			throw new IndexOutOfBoundsException("Tentativa de escrever fora dos limites da memoria.");
		}
		System.arraycopy(word, 0, memory, wordIndex * 3, 3);
	}

	/**
	 * Escreve um único byte na memória.
	 *
	 * @param byteAddress Endereço de escrita.
	 * @param value Valor a ser escrito (apenas o byte menos significativo será usado).
	 * @throws IndexOutOfBoundsException Se o endereço for inválido.
	 */
	public void writeByte(int byteAddress, int value) {
		if (byteAddress >= memorySize) {
			throw new IndexOutOfBoundsException("Tentativa de escrever fora dos limites da memoria.");
		}
		memory[byteAddress] = (byte) (value & 0xFF);  // Armazena apenas o byte
	}

	/**
	 * Retorna o tamanho da memória em bytes.
	 *
	 * @return Tamanho total da memória.
	 */
	public int getSize() {
		return memorySize;
	}

	/**
	 * Retorna a quantidade de palavras de 3 bytes que cabem na memória.
	 *
	 * @return Quantidade de palavras (memorySize / 3).
	 */
	public int getAddressRange() {
		return memorySize / 3;
	}

	/**
	 * Retorna uma cópia do mapa de memória completo.
	 *
	 * @return Array de bytes representando toda a memória.
	 */
	public byte[] getMemoryMap() {
		return Arrays.copyOf(memory, memory.length);
	}

	/**
	 * Zera toda a memória.
	 */
	public void reset() {
		Arrays.fill(memory, (byte) 0);
	}

	/**
	 * Retorna uma representação em string da memória em formato hexadecimal.
	 *
	 * @return String formatada da memória.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < memorySize; i++) {
			sb.append(String.format("%02X ", memory[i]));
			if ((i + 1) % 16 == 0) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
