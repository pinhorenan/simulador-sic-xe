package sicxesimulator.machine.memory;

public class Memory {
	private final Word[] memory;
	private static final int MIN_SIZE = 1024; // Tamanho mínimo em bytes
	private final int addressRange; // Número de palavras (cada uma com 3 bytes)

	/**
	 * Construtor da memória.
	 * @param size Tamanho total da memória em bytes. Deve ser pelo menos MIN_SIZE.
	 */
	public Memory(int size) {
		if (size < MIN_SIZE) {
			throw new IllegalArgumentException("Tamanho mínimo da memória: " + MIN_SIZE + " bytes");
		}
		// Cada palavra possui 3 bytes. O número de palavras é o tamanho total dividido por 3.
		this.addressRange = size / 3;
		memory = new Word[addressRange];
		clearMemory();
	}

	/// GETTERS

	/**
	 * Retorna o tamanho total da memória em bytes.
	 */
	public int getSize() {
		return addressRange * 3;
	}

	/**
	 * Retorna o número total de palavras na memória.
	 */
	public int getAddressRange() {
		return addressRange;
	}

	/// MANIPULAÇÃO DE PALAVRAS

	/**
	 * Escreve uma palavra (3 bytes) na memória, usando o endereço em bytes.
	 * @param address Endereço em bytes.
	 * @param wordData Array de 3 bytes.
	 */
	public void writeWord(int address, byte[] wordData) {
		if (wordData == null || wordData.length != 3) {
			throw new IllegalArgumentException("Uma palavra deve ter exatamente 3 bytes e não ser NULL.");
		}
		validateAddress(address);
		int index = address / 3;
		memory[index].setValue(wordData);
	}

	/**
	 * Lê uma palavra (3 bytes) da memória, usando o endereço em bytes.
	 * @param address Endereço em bytes.
	 * @return Array de 3 bytes correspondente à palavra.
	 */
	public byte[] readWord(int address) {
		validateAddress(address);
		int index = address / 3;
		return memory[index].getValue();
	}

	///  ESCRITA EM MASSA DE BYTES (útil para carregar programas)

	/**
	 * Escreve um array de bytes na memória a partir de um endereço inicial.
	 * @param startAddress Endereço inicial (em bytes).
	 * @param data Array de bytes a serem escritos.
	 */
	public void writeBytes(int startAddress, byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("Dados não podem ser nulos.");
		}
		validateAddress(startAddress);
		validateAddress(startAddress + data.length - 1);
		for (int i = 0; i < data.length; i++) {
			writeByte(startAddress + i, data[i] & 0xFF);
		}
	}

	/**
	 * Lê um array de bytes da memória.
	 * @param startAddress Endereço inicial (em bytes).
	 * @param numBytes Número de bytes a serem lidos.
	 * @return Array de bytes lido da memória.
	 */
	public byte[] readBytes(int startAddress, int numBytes) {
		validateAddress(startAddress);
		validateAddress(startAddress + numBytes - 1);
		byte[] result = new byte[numBytes];
		for (int i = 0; i < numBytes; i++) {
			result[i] = (byte) readByte(startAddress + i);
		}
		return result;
	}

	///  MÉTODOS BÁSICOS (BYTE)

	/**
	 * Lê um byte da memória, dado um endereço em bytes.
	 * @param address Endereço em bytes.
	 * @return O valor do byte (0-255).
	 */
	public int readByte(int address) {
		validateAddress(address);
		int index = address / 3;
		int offset = address % 3;
		byte[] wordValue = memory[index].getValue();
		return wordValue[offset] & 0xFF;
	}

	/**
	 * Escreve um byte na memória.
	 * @param address Endereço em bytes.
	 * @param value Valor a ser escrito (0-255).
	 */
	public void writeByte(int address, int value) {
		validateAddress(address);
		int index = address / 3;
		int offset = address % 3;
		byte [] wordValue = memory[index].getValue();
		wordValue[offset] = (byte) (value & 0xFF);
	}

	/// MÉTODOS PARA PONTO FLUTUANTE (6 BYTES)

	/**
	 * Lê um valor de ponto flutuante (representado em 6 bytes) da memória.
	 * @param address Endereço inicial (em bytes) do ponto flutuante.
	 * @return Valor lido.
	 */
	public long readFloat(int address) {
		validateAddress(address + 5);
		long value = 0;
		for (int i = 0; i < 6; i++) {
			value = (value << 8) | (readByte(address + i) & 0xFF);
		}
		return value;
	}

	/**
	 * Escreve um valor de ponto flutuante (6 bytes) na memória.
	 * @param address Endereço inicial (em bytes).
	 * @param value Valor a ser escrito.
	 */
	public void writeFloat(int address, long value) {
		validateAddress(address + 5);
		for (int i = 5; i >= 0; i--) {
			writeByte(address + i, (int)(value & 0xFF));
			value >>= 8;
		}
	}

	///  MÉTODOS AUXILIARES

	/**
	 * Valida se o endereço (em bytes) está dentro dos limites da memória.
	 * @param address Endereço em bytes.
	 */
	private void validateAddress(int address) {
		if (address < 0 || address >= getSize()) {
			throw new IllegalArgumentException("Endereço inválido: " + address + ". Tamanho da memória: " + getSize());
		}
	}

	/**
	 * Inicializa a memória criando um Word para cada posição.
	 * Se o endereço de uma palavra for considerado em bytes, multiplicamos o índice por 3.
	 */
	public void clearMemory() {
		for (int i = 0; i < addressRange; i++) {
			memory[i] = new Word(i); // TODO: Se o endereço for em bytes, preciso multiplicar por 3: Word(i*3)
		}
	}
}