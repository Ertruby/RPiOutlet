package gpio;

import java.io.IOException;

// code for device A = 21, C = 149

/**
 * Controls the power switch of the WSc (whether to turn the device on or off)
 * 
 * @author Rob
 */
public class SwitchController {
	
	private static final String COMMAND = "./../wiringPi/examples/lights/action";
	
	private String codeLetter;
	private int codeNR;
	
	public SwitchController(String codeLetter, int codeNR) {
		this.codeLetter = codeLetter;
		this.codeNR = codeNR;
	}
	
	public boolean turnOn() {
		try {
			new ProcessBuilder(COMMAND, codeNR+"", codeLetter, "on").start();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
		
	public boolean turnOff() {
		try {
			new ProcessBuilder(COMMAND, codeNR+"", codeLetter, "off").start();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
}
