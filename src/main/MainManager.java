package main;

import connection.WallSocketServer;
import gpio.LampController;

public class MainManager {

	public static final int port = 7332;
	private static boolean isOn = false;
	private static WallSocketServer sock = null;
	private static LampController lamp = null;
	private static PowerMonitor pm = null;

	public MainManager() {
		// create a socketmanager
		System.out.println("Starting a socket manager...");
		sock = new WallSocketServer(this, port);
		sock.start();
		turnOn();
	}

	public String isOn() {
		return String.valueOf(isOn);
	}

	public String turnOn() {
		boolean toReturn = false;
		if (pm == null & lamp == null) {
			isOn = true;
			System.out.println("Starting a lamp controller...");
			// lamp = new LampController();
			// lamp.start();

			System.out.println("Starting a power monitor...");
			pm = new PowerMonitor();
			pm.start();
		}
		return String.valueOf(toReturn);
	}

	public String turnOff() {
		boolean toReturn = false;
		if (pm != null & lamp != null) {
			pm.shutdown();
			pm = null;
			lamp.shutdown();
			lamp = null;
			isOn = false;
		}		
		return String.valueOf(toReturn);
	}

	public String getValues() {
		return pm.readFromFiles();
	}
	
	public String getColor() {
		return lamp.getColor().toString();
	}

//	private void newLampManager() {
//
//		int i = 0;
//		boolean go = true;
//
//		while (go) {
//			if (i == 0) {
//				lamp.toggleGreen();
//				i++;
//			} else if (i == 1) {
//				lamp.toggleRed();
//				i++;
//			} else if (i == 2) {
//				lamp.toggleGreen();
//				i++;
//			} else {
//				go = false;
//			}
//
//			try {
//				LampController.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//
//		lamp.shutdown();
//	}

	public static void main(String args[]) {
		new MainManager();

	}
}
