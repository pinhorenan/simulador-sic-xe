package sicxesimulator.simulation.virtualMachine;

public class Register {

	private final String name;
	private String value;
	private final int size;

	public Register(String name) {
		this.name = name;

		if (name.equals("F")) {
			this.size = 48; // Registrador de ponto flutuante (48 bits).
			this.value = "000000000000";
		} else {
			this.size = 24; // Demais registradores (24 bits).
			this.value = "000000";
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

	/**
	 * Define o valor do registrador a partir de uma String hexadecimal.
	 * A String deve ter o número correto de dígitos hexadecimais para o tamanho do registrador.
	 *
	 * @param value Novo valor hexadecimal.
	 */
	public void setValue(String value) {
		if (isValidHex(value)) {
			this.value = padHex(value);
		} else {
			System.out.println("Erro: Valor inválido para o registrador " + name);
		}
	}

	/**
	 * Define o valor do registrador a partir de um número inteiro.
	 * O valor é convertido para hexadecimal e ajustado para o tamanho correto do registrador.
	 *
	 * @param value Novo valor inteiro.
	 */
	public void setValue(int value) {
		String hexValue = Integer.toHexString(value).toUpperCase();
		this.value = padHex(hexValue);
	}

	/**
	 * Define o valor do registrador a partir de uma Word.
	 * A conversão para String hexadecimal é feita automaticamente.
	 *
	 * @param value Objeto Word contendo o novo valor.
	 */
	public void setValue(Word value) {
		this.value = padHex(value.toString());
	}

	// ** MÉTODOS AUXILIARES **

	/**
	 * Verifica se uma String representa um número hexadecimal válido.
	 *
	 * @param hex String a ser validada.
	 * @return true se for hexadecimal válido, false caso contrário.
	 */
	private boolean isValidHex(String hex) {
		return hex.matches("[0-9A-Fa-f]+");
	}

	/**
	 * Ajusta uma String hexadecimal para garantir o tamanho correto do registrador.
	 * Se for menor, preenche com zeros à esquerda; se for maior, corta os dígitos extras.
	 *
	 * @param hex String hexadecimal original.
	 * @return String ajustada para o tamanho correto.
	 */
	private String padHex(String hex) {
		int hexLength = size / 4; // Converte bits para número de caracteres hexadecimais

		if (hex.length() > hexLength) {
			return hex.substring(hex.length() - hexLength); // Mantém apenas os últimos dígitos
		} else {
			return String.format("%" + hexLength + "s", hex).replace(' ', '0'); // Preenche com zeros à esquerda
		}
	}
}