package sicxesimulator;

import java.util.HashMap;

/**
 * Representa o conjunto de registradores do simulador SIC/XE.
 * Cada registrador é armazenado como uma entrada em um HashMap, a chave é o nome do registrador
 * e o valor é o seu conteúdo em hexadecimal.
 */
public class Register {

	/**
	 * HashMap que armazena os registradores.
	 * A chave representa o nome do registrador e o valor, o seu conteúdo em hexadecimal.
	 */
	HashMap<String, String> registers = new HashMap<>();

	/**
	 * Construtor padrão.
	 * Inicializa os registradores com valores padrão:
	 * A, X, L, PC, B, S e T são inicializados com seus 24 bits,
	 * enquanto F, que lida com ponto flutuante, possui 48 bits.
	 */
	public Register() {
		this.registers.put("A", "000000");
		this.registers.put("X", "000000");
		this.registers.put("L", "000000");
		this.registers.put("PC", "000000");
		this.registers.put("B", "000000");
		this.registers.put("S", "000000");
		this.registers.put("T", "000000");
		this.registers.put("F", "000000000000");
	}

	/**
	 * Retorna o valor armazenado no registrador especificado.
	 *
	 * @param register nome do registrador.
	 * @return String com o valor do registrador.
	 */
	public String getRegister(String register) {
		return this.registers.get(register);
	}

	/**
	 * Define o valor do registrador especificado.
	 *
	 * @param register nome do registrador.
	 * @param value valor a ser armazenado.
	 */
    public void setRegister(String register, String value) {
		this.registers.put(register, value);
	}

	/**
	 * Imprime o valor de todos os registradores.
	 * Para cada registrador, imprime o nome e o valor armazenado.
	 */
    public void viewRegisters() {
		for (String key : registers.keySet()) {
			System.out.println(key + " = " + this.getRegister(key));
		}
	}
}
