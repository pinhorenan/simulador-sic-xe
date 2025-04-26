package sicxesimulator.hardware;

import java.util.Arrays;

/**
 * Simula a memória da máquina SIC/XE como um vetor de bytes.
 * <p>
 * Suporta leitura/gravação de bytes e palavras (3 bytes), reset
 * e dump textual em hexadecimal.
 *
 * @author Renan
 * @since 1.0.0
 */
public class Memory {

	private final byte[] data;

	/**
	 * Cria memória com o tamanho especificado.
	 *
	 * @param sizeInBytes número de bytes de memória
	 */
	public Memory(int sizeInBytes) {
		this.data = new byte[sizeInBytes];
	}

	/**
	 * Lê uma palavra de 3 bytes (big-endian) a partir do índice de palavra.
	 *
	 * @param wordIndex índice da palavra (0..)
	 * @return array de 3 bytes lidos
	 * @throws IndexOutOfBoundsException se ultrapassar limites
	 */
	public byte[] readWord(int wordIndex) {
		int pos = wordIndex * 3;
		if (pos + 3 > data.length) {
			throw new IndexOutOfBoundsException("Leitura fora dos limites da memória.");
		}
		return Arrays.copyOfRange(data, pos, pos + 3);
	}

	/**
	 * Lê um byte e retorna como int (0–255).
	 *
	 * @param address endereço do byte (0..)
	 * @return valor positivo do byte
	 * @throws IndexOutOfBoundsException se fora do range
	 */
	public int readByte(int address) {
		if (address < 0 || address >= data.length) {
			throw new IndexOutOfBoundsException("Leitura fora dos limites da memória.");
		}
		return data[address] & 0xFF;
	}

	/**
	 * Grava uma palavra de 3 bytes no índice de palavra.
	 *
	 * @param wordIndex índice da palavra
	 * @param word array de exatamente 3 bytes
	 * @throws IllegalArgumentException se array length ≠ 3
	 * @throws IndexOutOfBoundsException se fora do range
	 */
	public void writeWord(int wordIndex, byte[] word) {
		if (word.length != 3) {
			throw new IllegalArgumentException("Palavra deve ter 3 bytes.");
		}
		int pos = wordIndex * 3;
		if (pos + 3 > data.length) {
			throw new IndexOutOfBoundsException("Gravação fora dos limites da memória.");
		}
		System.arraycopy(word, 0, data, pos, 3);
	}

	/**
	 * Grava um único byte (menos significativo) no endereço dado.
	 *
	 * @param address posição do byte
	 * @param value valor inteiro (só byte baixo é usado)
	 * @throws IndexOutOfBoundsException se fora do range
	 */
	public void writeByte(int address, int value) {
		if (address < 0 || address >= data.length) {
			throw new IndexOutOfBoundsException("Gravação fora dos limites da memória.");
		}
		data[address] = (byte)(value & 0xFF);
	}

	/** @return tamanho da memória em bytes */
	public int getSize() {
		return data.length;
	}

	/** @return cópia do mapa completo de memória */
	public byte[] getMemoryMap() {
		return Arrays.copyOf(data, data.length);
	}

	/** Zera toda a memória (todos os bytes = 0). */
	public void reset() {
		Arrays.fill(data, (byte)0);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			sb.append(String.format("%02X ", data[i]));
			if ((i + 1) % 16 == 0) sb.append("\n");
		}
		return sb.toString();
	}
}
