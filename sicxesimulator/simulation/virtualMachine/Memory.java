package sicxesimulator.simulation.virtualMachine;

import java.util.ArrayList;

public class Memory {
	protected final ArrayList<Word> words;
	protected final int SIZE = 1000; // Tamanho arbitrário.

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

	// Função para alterar um byte (usado na instrução STCH)
	public void setByte(int address, String byteValue) {
		Word word = words.get(address);
		String currentValue = word.getValue();
		if (currentValue.length() < 2) {
			System.out.println("Erro: Word inválido para setByte");
			return;
		}
		String newValue = currentValue.substring(0, currentValue.length() - 2) + byteValue;
		word.setValue(newValue);
	}
}
