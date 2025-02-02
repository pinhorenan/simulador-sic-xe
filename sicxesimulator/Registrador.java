package sicxesimulator;

import java.util.HashMap;

public class Registrador {
	
	HashMap<String, String> registradores = new HashMap<String, String>();
	
	public Registrador() {
		this.registradores.put("A", "000000");
		this.registradores.put("X", "000000");
		this.registradores.put("L", "000000");
		this.registradores.put("PC", "000000");
		this.registradores.put("B", "000000");
		this.registradores.put("S", "000000");
		this.registradores.put("T", "000000");
		this.registradores.put("F", "000000000000");
	}
	
	public String getRegistrador(String registrador) {
		return this.registradores.get(registrador);
	}
	
	public void setRegistrador(String registrador, String valor) {
		this.registradores.put(registrador, valor);
	}
	
	public void viewRegistradores() {
		for (String chave : registradores.keySet()) {
			System.out.println(chave + " = " + this.getRegistrador(chave));
		}
	}
}
