package sicxesimulator.simulation.virtualMachine;

public class Register {

	private final String name;
	private String value;
	private final int size;

	/**
	 * Opções válidas para os registradores.
	 */
	private static final String[] VALID_OPTIONS = {"A", "X", "L", "PC", "B", "S", "T", "F", "PC", "SW"};


	public Register(String name) {
		this.name = name;
		if (name.equals("F")) {
			this.size = 48; // Tamanho em bits. Para o registrador dos floats.
			this.value = "000000000000000000000000000000000000000000000000"; // TODO isso deve ser temporário.
		} else {
			this.size = 24;
			this.value = "000000000000000000000000";
		}
		// TODO
		// Condicional para garantir que o valor de entrada seja válido.
	}

	// Getters

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public int getSize() {
		return size;
	}

	/// Setters

	// Setter que recebe uma String
	public void setValue(String value) {
		this.value = value;
	}

	// Setter que recebe um inteiro
	public void setValue(int value) {
		this.value = String.valueOf(value);
	}

	// Setters que recebe uma Word
	public void setValue(Word value) {
		this.value = value.toString();
	}
}
