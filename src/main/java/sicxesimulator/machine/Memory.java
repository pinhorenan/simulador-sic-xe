package sicxesimulator.machine;

import java.util.Arrays;

public class Memory {
	private final byte[] memory;
	private static final int MIN_SIZE = 1024; // 1 KB (tamanho mínimo exigido)

	public Memory(int size) {
		if (size < MIN_SIZE) {
			throw new IllegalArgumentException("Tamanho mínimo da memória: " + MIN_SIZE + " bytes");
		}
		this.memory = new byte[size];
		clearMemory();
	}

	// ================ ESCRITA DE BYTES (PARA CARREGAR PROGRAMAS) ================
	public void writeBytes(int startAddress, byte[] data) {
		validateAddress(startAddress);
		validateAddress(startAddress + data.length - 1); // Verifica se o último byte cabe na memória

		for (int i = 0; i < data.length; i++) {
			memory[startAddress + i] = data[i];
		}
	}

	public byte[] readBytes(int startAddress, int numBytes) {
		validateAddress(startAddress);
		validateAddress(startAddress + numBytes - 1);
		byte[] result = new byte[numBytes];
		for (int i = 0; i < numBytes; i++) {
			result[i] = memory[startAddress + i];
		}
		return result;
	}

	// ================ MÉTODOS BÁSICOS (BYTE) ================
	public int readByte(int address) {
		validateAddress(address);
		return memory[address] & 0xFF;
	}

	public void writeByte(int address, int value) {
		validateAddress(address);
		memory[address] = (byte) (value & 0xFF);
	}

	// ================ PALAVRAS (3 BYTES) ================
	public int readWord(int address) {
		validateAddress(address + 2);
		return (memory[address] & 0xFF) << 16
				| (memory[address+1] & 0xFF) << 8
				| (memory[address+2] & 0xFF);
	}

	public void writeWord(int address, int value) {
		validateAddress(address + 2);
		memory[address]   = (byte) ((value >> 16) & 0xFF);
		memory[address+1] = (byte) ((value >> 8) & 0xFF);
		memory[address+2] = (byte) (value & 0xFF);
	}

	// ================ INSTRUÇÕES ESTENDIDAS (4 BYTES) ================
	public int readExtended(int address) {
		validateAddress(address + 3);
		return (memory[address] & 0xFF) << 24
				| (memory[address+1] & 0xFF) << 16
				| (memory[address+2] & 0xFF) << 8
				| (memory[address+3] & 0xFF);
	}

	public void writeExtended(int address, int value) {
		validateAddress(address + 3);
		memory[address]   = (byte) ((value >> 24) & 0xFF);
		memory[address+1] = (byte) ((value >> 16) & 0xFF);
		memory[address+2] = (byte) ((value >> 8) & 0xFF);
		memory[address+3] = (byte) (value & 0xFF);
	}

	// ================ PONTO FLUTUANTE (6 BYTES) ================
	public long readFloat(int address) {
		validateAddress(address + 5);
		long value = 0;
		for (int i = 0; i < 6; i++) {
			value = (value << 8) | (memory[address + i] & 0xFF);
		}
		return value;
	}

	public void writeFloat(int address, long value) {
		validateAddress(address + 5);
		for (int i = 5; i >= 0; i--) {
			memory[address + i] = (byte) (value & 0xFF);
			value >>= 8;
		}
	}

	// ================ MÉTODOS AUXILIARES ================
	public void clearMemory() {
		Arrays.fill(memory, (byte) 0);
	}

	public int getSize() {
		return memory.length;
	}

	private void validateAddress(int address) {
		if (address < 0 || address >= memory.length) {
			throw new IllegalArgumentException("Endereço inválido: " + address + ". Tamanho da memória: " + memory.length);
		}
	}
}