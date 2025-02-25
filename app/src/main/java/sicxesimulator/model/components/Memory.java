package sicxesimulator.model.components;

import java.util.Arrays;

public class Memory {
	private final String[] memory;

	public Memory(int size) {
		this.memory = new String[size];
		Arrays.fill(memory, "00");
	}

	// ================ BASIC ACCESS ================
	public String read(int address) {
		validateAddress(address);
		return memory[address];
	}

	public void write(int address, String value) {
		validateAddress(address);
		if (!value.matches("[0-9A-Fa-f]{2}")) {
			throw new IllegalArgumentException("Valor de memória inválido: " + value);
		}
		memory[address] = value.toUpperCase();
	}

	public int getSize() {
		return memory.length;
	}

	// ================ WORD ACCESS (3 bytes) ================
	@SuppressWarnings("unused")
    public int readWord(int address) {
		validateAddress(address + 2);
		return Integer.parseInt(
				read(address) + read(address + 1) + read(address + 2),
				16
		);
	}

	@SuppressWarnings("unused")
    public void writeWord(int address, int value) {
		validateAddress(address + 2);
		String hex = String.format("%06X", value & 0xFFFFFF);
		write(address, hex.substring(0, 2));
		write(address + 1, hex.substring(2, 4));
		write(address + 2, hex.substring(4, 6));
	}

	// ================ VALIDATION ================
	private void validateAddress(int address) {
		if (address < 0 || address >= memory.length) {
			throw new IllegalArgumentException("Endereço inválido: " + address);
		}
	}

	public void clear() {
		Arrays.fill(memory, "00");
	}
}