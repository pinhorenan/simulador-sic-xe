package sicxesimulator.hardware.cpu;

import sicxesimulator.utils.Checker;

/**
 * Representa um registrador da arquitetura SIC/XE.
 *
 * Cada registrador possui um nome, um tamanho fixo (24 ou 48 bits)
 * e armazena um valor numérico. A classe oferece acesso controlado
 * com máscaras de bits para garantir o tamanho correto.
 */
public class Register {
	private final String name;
	private long value;
	private final int size;

	/**
	 * Cria um novo registrador com o nome especificado.
	 *
	 * O tamanho é definido com base no nome:
	 * - Registrador "F" → 48 bits
	 * - Demais registradores → 24 bits
	 *
	 * @param name Nome do registrador (ex: "A", "X", "F", etc).
	 * @throws IllegalArgumentException se o nome for inválido.
	 */
	public Register(String name) {
		if (!Checker.isValidRegisterName(name)) {
			throw new IllegalArgumentException("Registrador invalido: " + name);
		}
		this.name = name;
		this.size = name.equals("F") ? 48 : 24;
		this.value = 0;
	}

	/**
	 * Retorna o nome do registrador.
	 *
	 * @return Nome do registrador.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retorna o valor armazenado (24 bits) como inteiro.
	 *
	 * @return Valor do registrador como int (apenas para registradores de 24 bits).
	 * @throws IllegalStateException se chamado em um registrador de 48 bits.
	 */
	public int getIntValue() {
		if (size == 48) {
			throw new IllegalStateException("O registrador F e de 48 bits. Use getLongValue().");
		}
		return (int) (value & 0xFFFFFF);
	}

	/**
	 * Retorna o valor armazenado (48 bits) como long.
	 *
	 * @return Valor do registrador como long (apenas para registrador "F").
	 * @throws IllegalStateException se chamado em um registrador de 24 bits.
	 */
	public long getLongValue() {
		if (size != 48) {
			throw new IllegalStateException("Esse registrador nao e de 48 bits. Use getIntValue().");
		}
		return value & 0xFFFFFFFFFFFFL;
	}

	/**
	 * Define o valor do registrador, respeitando seu tamanho.
	 *
	 * Aplica uma máscara para manter apenas os bits válidos:
	 * - 24 bits para registradores normais.
	 * - 48 bits para o registrador "F".
	 *
	 * @param newValue Novo valor a ser armazenado.
	 */
	public void setValue(long newValue) {
		if (size == 24) {
			this.value = newValue & 0xFFFFFF; // Mantém 24 bits
		} else {
			this.value = newValue & 0xFFFFFFFFFFFFL; // Mantém 48 bits
		}
	}

	/**
	 * Zera o valor armazenado no registrador.
	 */
	public void clearRegister() { value = 0;}
}
