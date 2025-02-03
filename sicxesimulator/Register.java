package sicxesimulator;

import java.util.HashMap;

public class Register {
	
	HashMap<String, String> registers = new HashMap<String, String>();
	
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
	
	public String getRegister(String register) {
		return this.registers.get(register);
	}
	
	public void setRegister(String register, String value) {
		this.registers.put(register, value);
	}
	
	public void viewRegisters() {
		for (String key : registers.keySet()) {
			System.out.println(key + " = " + this.getRegister(key));
		}
	}
}
