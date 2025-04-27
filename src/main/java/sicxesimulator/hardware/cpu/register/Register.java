package sicxesimulator.hardware.cpu.register;

import sicxesimulator.common.utils.Checker;

/**
 * Modelo de registrador SIC/XE (24 ou 48 bits).
 *
 * <p>As operações de leitura e escrita aplicam máscara automática para manter
 * o tamanho do registrador e validar usos incorretos (ex.: ler 48 bits como
 * {@code int}).</p>
 *
 * @author Renan
 * @since 1.0.0
 */
public final class Register {

	private final String name;
	private final int    size;          // 24 ou 48
	private long         value;         // armazenado sempre como 64 bits

	/**
	 * @param name A, X, L, B, S, T, F, PC ou SW
	 * @throws IllegalArgumentException se o nome for inválido
	 */
	public Register(String name) {
		if (!Checker.isValidRegisterName(name)) {
			throw new IllegalArgumentException("Registrador inválido: " + name);
		}
		this.name  = name;
		this.size  = name.equals("F") ? 48 : 24;
		this.value = 0;
	}

	/** @return nome simbólico do registrador */
	public String getName() {
		return name;
	}

	/** @return conteúdo como {@code int} (24 bits) */
	public int getIntValue() {
		ensureSize(24);
		return (int) (value & 0xFFFFFF);
	}

	/** @return conteúdo como {@code long} (48 bits) */
	public long getLongValue() {
		ensureSize(48);
		return value & 0xFFFFFFFFFFFFL;
	}

	/**
	 * Sobrescreve o conteúdo, mantendo apenas o número de bits válido.
	 *
	 * @param newValue valor bruto
	 */
	public void setValue(long newValue) {
		value = (size == 24)
				? (newValue & 0xFFFFFF)
				: (newValue & 0xFFFFFFFFFFFFL);
	}

	/** Zera o registrador. */
	public void clearRegister() {
		value = 0;
	}

	/* ------------------------------------------------------------------ */
	/*                              utilidade                             */
	/* ------------------------------------------------------------------ */

	private void ensureSize(int expected) {
		if (size != expected) {
			throw new IllegalStateException(
					"Registrador " + name + " possui " + size + " bits; "
							+ "use o getter apropriado.");
		}
	}
}
