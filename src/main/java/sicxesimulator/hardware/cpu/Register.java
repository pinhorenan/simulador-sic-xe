package sicxesimulator.hardware.cpu;

import sicxesimulator.utils.Checker;

public class Register {
	private final String name;
	private long value;
	private final int size;

	/**
	 * Cria um novo registrador com o nome especificado.
	 * @param name Nome do registrador.
	 */
	public Register(String name) {
		if (!Checker.isValidRegisterName(name)) {
			throw new IllegalArgumentException("Registrador inválido: " + name);
		}
		this.name = name;
		this.size = name.equals("F") ? 48 : 24;
		this.value = 0;
	}

	/// ===== Métodos Getters =====

	public String getName() {
		return name;
	}

	public int getIntValue() {
		if (size == 48) {
			throw new IllegalStateException("O registrador F é de 48 bits. Use getLongValue().");
		}
		return (int) (value & 0xFFFFFF);
	}

	public long getLongValue() {
		if (size != 48) {
			throw new IllegalStateException("Esse registrador não é de 48 bits. Use getIntValue().");
		}
		return value & 0xFFFFFFFFFFFFL;
	}

	/// ===== Métodos Setters =====

	public void setValue(long newValue) {
		if (size == 24) {
			this.value = newValue & 0xFFFFFF; // Mantém 24 bits
		} else {
			this.value = newValue & 0xFFFFFFFFFFFFL; // Mantém 48 bits
		}
	}

	/// ===== Métodos Auxiliares =====

	public void clearRegister() { value = 0;}

}