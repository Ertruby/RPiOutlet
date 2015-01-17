package main;

import connection.WallSocketServer;
import gpio.LampController;

public class MainManager {

	public static final int port = 7332;
	public static final int greenThreshold = 150;
	public static final int orangeThreshold = 350;
	private static boolean isOn = false;
	private static WallSocketServer sock = null;
	private static LampController lamp = null;
	private static PowerMonitor pm = null;

	public MainManager() {
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
			lamp = new LampController();
			lamp.start();

			System.out.println("Starting a power monitor...");
			pm = new PowerMonitor(this);
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
	
	public void quit() {
		turnOff();
		sock.stopServer();
	}

	public String getValues() {
		return pm.readFromFiles();
	}
	
	public String getColor() {
		return lamp.getColor().toString();
	}
	
	public void colorChanger(int value) {
		if (value <= greenThreshold) {
			lamp.setGreen();
		} else if (value <= orangeThreshold) {
			lamp.setOrange();
		} else {
			lamp.setRed();
		}
	}

	public static void main(String args[]) {
		new MainManager();
	}
}
