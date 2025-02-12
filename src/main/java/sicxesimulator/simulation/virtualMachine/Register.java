package sicxesimulator.simulation.virtualMachine;

public class Register {
	private final String name;
	private String value;
	private final int size;

	public Register(String name) {
		this.name = name;
		if (name.equals("F")) {
			this.size = 48; // Registrador F: 48 bits
			this.value = "000000000000";
		} else {
			this.size = 24; // Demais registradores: 24 bits
			this.value = "000000";
		}
	}

	/// Getters
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	/// Setters
	public void setValue(String value) {
		if (isValidHex(value)) {
			this.value = padHex(value);
		} else {
			System.out.println("Erro: Valor inválido para o registrador " + name);
		}
	}

	/// Métodos auxiliares
	private boolean isValidHex(String hex) {
		return hex.matches("[0-9A-Fa-f]+");
	}

	private String padHex(String hex) {
		int hexLength = size / 4; // Converte bits para número de caracteres hexadecimais
		if (hex.length() > hexLength) {
			return hex.substring(hex.length() - hexLength); // Mantém apenas os últimos dígitos
		} else {
			return String.format("%" + hexLength + "s", hex).replace(' ', '0'); // Preenche com zeros à esquerda
		}
	}
}