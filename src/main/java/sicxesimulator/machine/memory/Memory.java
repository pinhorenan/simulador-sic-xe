package sicxesimulator.machine.memory;

import sicxesimulator.utils.Convert;

public class Memory {
	private final Word[] memory;
	private static final int MIN_SIZE_IN_BYTES = 1024; // Tamanho mínimo em bytes
	private final int addressRange; // Número de palavras (cada uma com 3 bytes)

	public Memory(int sizeInBytes) {
		// Validação do tamanho mínimo
		if (sizeInBytes < MIN_SIZE_IN_BYTES) {
			throw new IllegalArgumentException("Tamanho mínimo da memória: " + MIN_SIZE_IN_BYTES + " bytes");
		}

		this.addressRange = sizeInBytes / 3; // Ex: 12288 bytes / 3 = 4096 words
		memory = new Word[addressRange];     // Array de tamanho 4096 (índices 0-4095)
		clearMemory();                       // Inicializa todas as posições
	}


	public int getSizeInBytes() {
		return addressRange * 3;
	}

	public int getAddressRange() {
		return addressRange;
	}

	public void writeWord(int wordAddress, byte[] wordData) {
		if (wordData == null || wordData.length != 3) {
			throw new IllegalArgumentException("Uma palavra deve ter exatamente 3 bytes e nao ser NULL.");
		}
		validateAddress(wordAddress);
		memory[wordAddress].setValue(wordData);
		System.out.println("Escrevendo palavra no endereco " + wordAddress + ": " + Convert.bytesToHex(wordData));
	}

	public byte[] readWord(int wordAddress) {
		validateAddress(wordAddress);
		//System.out.println("readWord(" + wordAddress + ")");
		return memory[wordAddress].getValue();
	}

	public int readByte(int wordAddress, int offset) {
		// Valida se o offset está dentro do intervalo 0-2
		if (offset < 0 || offset >= 3) {
			throw new IllegalArgumentException("Offset invalido: " + offset);
		}

		// Valida o endereço da palavra
		validateAddress(wordAddress);

		byte[] wordValue = memory[wordAddress].getValue();
		int byteValue = wordValue[offset] & 0xFF;
		System.out.println("Lendo byte no endereço " + wordAddress + " com offset " + offset + ": " + byteValue);
		return byteValue;
	}

	public int readByte(int byteAddress) {
		int wordAddress = byteAddress / 3; // Índice da palavra
		int offset = byteAddress % 3;      // Offset dentro da palavra
		return readByte(wordAddress, offset);
	}

	public void writeByte(int wordAddress, int offset, int value) {
		if (offset < 0 || offset >= 3) {
			throw new IllegalArgumentException("Offset inválido: " + offset);
		}
		validateAddress(wordAddress);
		// Obtém a cópia atual do array
		byte[] wordValue = memory[wordAddress].getValue();
		// Modifica o byte específico
		wordValue[offset] = (byte) (value & 0xFF);
		// Atualiza a palavra com o novo array
		memory[wordAddress].setValue(wordValue);
		System.out.println("Escrevendo byte no endereço " + wordAddress + " com offset " + offset + ": " + value);
	}

	private void validateAddress(int wordAddress) {
		if (wordAddress < 0 || wordAddress >= addressRange) {
			throw new IllegalArgumentException("Endereco invalido: " + wordAddress + ". Tamanho da memoria: " + addressRange + " palavras.");
		}
	}

	public void clearMemory() {
		for (int i = 0; i < addressRange; i++) { // Iteração correta: 0 ≤ i < 4096
			memory[i] = new Word(i); // Cria uma nova Word para cada posição
		}
	}

	// Novo método para escrever uma palavra diretamente pelo seu endereço
	public void writeWordByAddress(int wordAddress, byte[] data) {
		writeWord(wordAddress, data); // Já implementado
	}

	// Método para ler uma palavra pelo endereço
	public byte[] readWordByAddress(int wordAddress) {
		return readWord(wordAddress);
	}
}
