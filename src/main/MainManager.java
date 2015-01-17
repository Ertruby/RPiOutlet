package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import org.joda.time.IllegalInstantException;

import tools.Tools;
import connection.WallSocketServer;
import gpio.ColorType;
import gpio.LampController;

public class MainManager {

	public static final int DEF_PORT = 7331;
	public static final int greenThreshold = 150;
	public static final int orangeThreshold = 350;
	
	private int port = DEF_PORT;
	private static boolean isOn = false;
	private static WallSocketServer sock = null;
	private static LampController lamp = null;
	private static PowerMonitor pm = null;
	
	private boolean runOnPI = System.getProperty("os.name").equals("Linux");

	public MainManager() {
		// set up port
		setUpPort();
		// start server
		System.out.println("Starting the socket server (type \"quit\" to shutdown)...");
		sock = new WallSocketServer(this, port);
		sock.start();
		startStopListener();
		turnOn();
	}
	
	private void setUpPort() {
		boolean valid = false;
		System.out.println("Type port number (use 1025 - 65535) or \"d\" to use default (" + DEF_PORT + ")");
		while (!valid) {
			String res = Tools.waitForInput(System.in);
			if (res.equals("d")) {
				valid = true;
			} else {
				ServerSocket testSock = null;
				try {
					int portnr = Integer.parseInt(res);
					testSock = new ServerSocket(portnr);
					valid = true;
					port = portnr;
				} catch (IllegalArgumentException e) {
					System.out.println("Invalid port number, please try again");
				} catch (IOException e) {
					System.out.println("Port is already in use, please try again");
				} finally {
					if (testSock != null) {
						try {
							testSock.close();
						} catch (IOException e) {}
					}
				}
			}
		}
	}
	
	private void startStopListener() {
		Thread thread = new Thread(new Runnable() {		
			@Override
			public void run() {
				while (true) {
					String line = Tools.waitForInput(System.in);
					if (line.equals("quit")) {
						quit();
					}
				}		
			}
		}, "Input-listener-thread");
		thread.setDaemon(true);
		thread.start();
	}

	public String isOn() {
		return String.valueOf(isOn);
	}

	public String turnOn() {
		boolean toReturn = false;
		if (pm == null) {
			isOn = true;
			if (runOnPI) {
				System.out.println("Starting a lamp controller...");
				lamp = new LampController();
				lamp.start();
			}
			System.out.println("Starting a power monitor...");
			pm = new PowerMonitor(this);
			pm.start();
		} else {
			turnOff();
		}
		return String.valueOf(toReturn);
	}

	public String turnOff() {
		boolean toReturn = false;
		if (pm != null) {
			pm.shutdown();
			pm = null;
			if (runOnPI) {
				lamp.shutdown();
				lamp = null;
			}
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
		if (runOnPI) {
			return lamp.getColor().toString();
		} else {
			return ColorType.NONE.toString();
		}
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
