package sicxesimulator;

import java.util.ArrayList;

public class Memory {
	
	private ArrayList<MemoryWord> memory;

	private final int SIZE = 1000; // Tamanho da memória, não tenho certeza se vai ser fixo, logo isso aqui é temp.

	public Memory() {
		memory = new ArrayList<>(SIZE);

		// Cria todas posições de memória
		for (int i = 0; i < SIZE; i++) {
			memory.add(new MemoryWord());
		}
	}

	// Imprime o conteúdo de cada endereço de memória. DEBUG.
	public void printMemory() {
		for (int i = 0; i < memory.size(); i++) {
			System.out.println("Endereço " + i + ": " + memory.get(i));
		}
	}
	
	public ArrayList<MemoryWord> getMemory() {
		return memory;
	}
	
	public void setMemory(int address, MemoryWord value) {
		if (address >= 0 && address < SIZE) {
				memory.set(address, value); // Validação dos limites da memória.
		} else {
			System.out.println("Erro: Endereço fora dos limites.");
		}
	}

	public int read(int address) {
		if (address >= 0 && address < SIZE) {
			return memory.get(address).getValue();
		} else {
			System.out.println("Erro: Endereço fora dos limites.");
			return -1;
		}
	}

	public void write(int address, int value) {
		if (address >= 0 && address < SIZE) {
			memory.get(address).setValue(value);
		} else {
			System.out.println("Erro: Endereço fora dos limites.");
		}
	}
}
