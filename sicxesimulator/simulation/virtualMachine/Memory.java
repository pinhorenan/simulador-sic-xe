package sicxesimulator.simulation.virtualMachine;

import java.util.ArrayList;

public class Memory {

	protected final ArrayList<Word> words;
	protected final int SIZE = 1000; // Tamanho da memória, não tenho certeza se vai ser fixo, logo isso aqui é temp.

	/**
	 * Construtor padrão. Inicializa todas as posições da memória com Word's padrão.
	 */
	public Memory() {
		words = new ArrayList<>(SIZE);

		for (int i = 0; i < SIZE; i++) {
			words.add(new Word());
		}
	}

	public int getSize() {
		return words.size();
	}

	public String read(int address) {
		return words.get(address).getValue();
	}

	public void write(int address, String value) {
		words.get(address).setValue(value);
	}

	// TODO; Implementar operações byte à byte.

}
