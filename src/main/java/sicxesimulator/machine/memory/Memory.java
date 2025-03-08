package sicxesimulator.machine.memory;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Representa a memória do computador, que é um array de bytes.
 */
public class Memory {
	private static final Logger logger = Logger.getLogger(Memory.class.getName());
	private final byte[] memory;  // Memória em bytes
	private final int memorySize; // Tamanho da memória

	public Memory(int size) {
		this.memorySize = size;
		this.memory = new byte[size];
		logger.info("Memória inicializada com tamanho de " + size + " bytes.");
	}

	/**
	 * Lê uma palavra de memória, composta por 3 bytes (palavra de 24 bits).
	 * @param wordIndex Índice da palavra a ser lida
	 * @return Um array de 3 bytes representando a palavra de memória
	 */
	public byte[] readWord(int wordIndex) {
		if (wordIndex * 3 + 3 > memorySize) {
			throw new IndexOutOfBoundsException("Tentativa de ler fora dos limites da memória.");
		}
		byte[] word = new byte[3];
		System.arraycopy(memory, wordIndex * 3, word, 0, 3);
		logger.fine(String.format("readWord: Lida palavra no índice %d -> %02X %02X %02X",
				wordIndex, word[0], word[1], word[2]));
		return word;
	}

	/**
	 * Lê um byte específico da memória, dado um endereço em bytes.
	 * @param byteAddr O endereço do byte a ser lido
	 * @return O valor do byte no endereço especificado
	 */
	public int readByte(int byteAddr) {
		if (byteAddr >= memorySize) {
			throw new IndexOutOfBoundsException("Tentativa de ler fora dos limites da memória.");
		}
		int value = memory[byteAddr] & 0xFF;  // Retorna o byte como valor positivo (0-255)
		logger.fine(String.format("readByte: Lido byte no endereço %d -> %02X", byteAddr, value));
		return value;
	}

	/**
	 * Escreve uma palavra de 3 bytes na memória, dado um índice de palavra.
	 * @param wordIndex Índice da palavra a ser escrita
	 * @param word A palavra de 3 bytes a ser escrita
	 */
	public void writeWord(int wordIndex, byte[] word) {
		if (word.length != 3) {
			throw new IllegalArgumentException("Uma palavra deve ter exatamente 3 bytes.");
		}
		if (wordIndex * 3 + 3 > memorySize) {
			throw new IndexOutOfBoundsException("Tentativa de escrever fora dos limites da memória.");
		}
		System.arraycopy(word, 0, memory, wordIndex * 3, 3);
		logger.fine(String.format("writeWord: Escrita palavra no índice %d -> %02X %02X %02X",
				wordIndex, word[0], word[1], word[2]));
	}

	/**
	 * Escreve um byte na memória em um endereço específico.
	 * @param byteAddr O endereço do byte a ser escrito
	 * @param value O valor do byte a ser escrito
	 */
	public void writeByte(int byteAddr, int value) {
		if (byteAddr >= memorySize) {
			throw new IndexOutOfBoundsException("Tentativa de escrever fora dos limites da memória.");
		}
		memory[byteAddr] = (byte) (value & 0xFF);  // Armazena apenas o byte
		logger.fine(String.format("writeByte: Escrito byte no endereço %d -> %02X", byteAddr, value & 0xFF));
	}

	/**
	 * Retorna o tamanho total da memória em bytes.
	 * @return O tamanho da memória em bytes
	 */
	public int getSize() {
		return memorySize;
	}

	/**
	 * Retorna o mapa de memória para depuração, ou seja, uma cópia completa do array de bytes.
	 * @return Cópia do array de bytes que representa a memória
	 */
	public byte[] getMemoryMap() {
		return Arrays.copyOf(memory, memory.length);
	}

	/**
	 * Retorna o número total de palavras da memória (cada palavra possui 3 bytes).
	 * @return Número de palavras
	 */
	public int getAddressRange() {
		return memorySize / 3;
	}

	/**
	 * Reinicializa a memória, zerando todos os bytes.
	 */
	public void clearMemory() {
		Arrays.fill(memory, (byte) 0);
		logger.info("Memória limpa.");
	}

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
