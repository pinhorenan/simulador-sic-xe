package sicxesimulator.hardware.cpu.core;

import sicxesimulator.common.utils.Checker;

/**
 * Modelo de registrador SIC/XE, com nome, tamanho fixo e controle de bits.
 *
 * @author Renan
 * @since 1.0.0
 */
public class Register {
	private final String name;
	private long value;
	private final int size;

	/**
	 * Cria registrador com nome válido.
	 * @param name A, X, L, B, S, T, F, PC ou SW
	 * @throws IllegalArgumentException se nome inválido
	 */
	public Register(String name) {
		if (!Checker.isValidRegisterName(name)) {
			throw new IllegalArgumentException("Registrador invalido: " + name);
		}
		this.name = name;
		this.size = name.equals("F") ? 48 : 24;
		this.value = 0;
	}

	/** @return nome do registrador */
	public String getName() {
		return name;
	}

	/**
	 * @return valor como int (24 bits)
	 * @throws IllegalStateException se registrador for 48 bits
	 */
	public int getIntValue() {
		if (size == 48) {
			throw new IllegalStateException("F é 48 bits. Use getLongValue().");
		}
		return (int)(value & 0xFFFFFF);
	}

	/**
	 * @return valor como long (48 bits)
	 * @throws IllegalStateException se registrador for 24 bits
	 */
	public long getLongValue() {
		if (size != 48) {
			throw new IllegalStateException("Não é 48 bits. Use getIntValue().");
		}
		return value & 0xFFFFFFFFFFFFL;
	}

	/**
	 * Define valor, mascarando para o tamanho.
	 * @param newValue valor bruto
	 */
	public void setValue(long newValue) {
		this.value = (size == 24)
				? (newValue & 0xFFFFFF)
				: (newValue & 0xFFFFFFFFFFFFL);
	}

	/** Zera o registrador. */
	public void clearRegister() {
		value = 0;
	}
}
