package sicxesimulator.model.components;

public class Register {
	private final String name;
	private String value;
	private final int size;

	public Register(String name) {
		this.name = name;
		this.size = name.equals("F") ? 48 : 24; // F: 48 bits, outros: 24 bits
		this.value = "0".repeat(size / 4); // Inicializa com zeros em hex
	}

	// ================ GETTERS ================
	public String getName() { return name; }
	public String getValue() { return value; }
	public int getIntValue() { return Integer.parseInt(value, 16); }
	public long getLongValue() { return Long.parseLong(value, 16); }

	// ================ SETTERS ================
	public void setValue(String value) {
		if (isValidHex(value)) {
			this.value = padHex(value);
		} else {
			throw new IllegalArgumentException("Valor hexadecimal inválido: " + value);
		}
	}

	public void setValue(int value) {
		if (size == 48) {
			throw new IllegalStateException("Use setValue(long) para registrador F");
		}
		this.value = String.format("%06X", value & 0xFFFFFF);
	}

	public void setValue(long value) {
		if (size != 48) {
			throw new IllegalStateException("Use setValue(int) para registradores de 24 bits");
		}
		this.value = String.format("%012X", value & 0xFFFFFFFFFFFFL);
	}

	// ================ HELPER METHODS ================
	private boolean isValidHex(String hex) {
		return hex.matches("[0-9A-Fa-f]+");
	}

	private String padHex(String hex) {
		int requiredLength = size / 4;
		hex = hex.replaceAll("^0x", "").toUpperCase();

		if (hex.length() > requiredLength) {
			return hex.substring(hex.length() - requiredLength);
		} else {
			return String.format("%" + requiredLength + "s", hex).replace(' ', '0');
		}
	}

	public void increment() {
		if (size == 48) {
			long val = Long.parseLong(value, 16);
			setValue(val + 1);
		} else {
			int val = Integer.parseInt(value, 16);
			setValue(val + 1);
		}
	}
}