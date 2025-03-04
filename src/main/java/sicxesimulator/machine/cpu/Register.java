package sicxesimulator.machine.cpu;

public class Register {
	private final String name;
	private long value;
	private final int size; // Tamanho em bits: 24 (padrão) ou 48 para o registrador F

	// Registradores válidos e seus tamanhos
	public static final String[] VALID_REGISTERS = {"A", "X", "L", "B", "S", "T", "F", "PC", "SW"};

	/*
	Registradores e suas propriedades:

	Registrador A:
	Acumulador, número 0, tamanho 24bits.
	Armazena os dados (carregados e resultantes) das operações da Unidade de Lógica e Aritmética.

	Registrador X:
	Registrador de índice, número 1, tamanho 24bits.
	Usado para endereçamento.

	Registrador L:
	Registrador de ligação, número 2, tamanho 24.
	A instrução Jump to Subroutine (JSUB) armazena o endereço de retorno nesse registrador.

	Registrador B:
	Registrador Base, número 3, tamanho 24.
	Usado para endereçamento.

	Registrador S:
	Registrador de uso geral, número 4, tamanho 24.
	USO GERAL

	Registrador T:
	Registrador de uso geral, número 5, tamanho 24.
	USO GERAL

	Registrador F:
	Acumulador de ponto flutuante, número 6, tamanho 48.
	Armazena os dados (carregados e resultantes) das operações da Unidade de Lógica e Aritmética em ponto flutuante.

	Registrador PC:
	Contador de Instruções (Program Counter), número 8, tamanho 24.
	Mantém o endereço da próxima instrução a ser executada.

	Registrador SW:
	Palavra de condição, número 9, tamanho 24.
	Contém várias informações, incluindo código condicional (CC).

	 */

	public Register(String name) {
		if (!isValidName(name)) {
			throw new IllegalArgumentException("Registrador inválido: " + name);
		}
		this.name = name;
		this.size = name.equals("F") ? 48 : 24;
		this.value = 0; // Inicializa com zero
	}

	// ================ GETTERS ================
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


	// ================ SETTERS ================
	public void setValue(long newValue) {
		if (size == 24) {
			this.value = newValue & 0xFFFFFF; // Mantém 24 bits
		} else {
			this.value = newValue & 0xFFFFFFFFFFFFL; // Mantém 48 bits
		}
	}

	public void clearRegister() { value = 0;}

	// ================ AUXILIARES ================

	private boolean isValidName(String name) {
		for (String validName : VALID_REGISTERS) {
			if (validName.equals(name)) return true;
		}
		return false;
	}
}