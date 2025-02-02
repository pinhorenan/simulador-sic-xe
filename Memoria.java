package sicxesimulator;

import java.util.ArrayList;

public class Memoria {
	
	public ArrayList<PalavraMemoria> memoria;
	
	public Memoria() {
		memoria = new ArrayList<>();
		
		for (int i=0; i < 1000; i++) {
			this.memoria.add( new PalavraMemoria());
			SetMemoria(i, new PalavraMemoria());
		}
	}
	
	public void ShowMemoria() {
		for (int i=0; i < this.memoria.size(); i++) {
			System.out.println(memoria.get(i));
		}
	}
	
	public ArrayList<PalavraMemoria> GetMemoria() {
		return this.memoria;
	}
	
	public void SetMemoria(int endereco, PalavraMemoria valor) {
		this.memoria.set(endereco, valor);
	}	
}
