package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;

import tools.Tools;
import connection.WallSocketServer;
import gpio.ColorType;
import gpio.LampController;

public class MainManager {

	public static final int DEF_PORT = 7331;
	public static final int greenThreshold = 50;
	public static final int orangeThreshold = 150;
	private static final String PATH = "files/";


	private int port = DEF_PORT;
	private static boolean isOn = true;
	private static WallSocketServer sock = null;
	private static LampController lamp = null;
	private static PowerMonitor pm = null;

	private boolean runOnPI = !System.getProperty("sun.arch.data.model").equals("64");

	public MainManager() {
		// set up port
		setUpPort();
		// start server
		System.out.println("Starting the socket server (type \"q\" to shutdown)...");
		sock = new WallSocketServer(this, port);
		sock.start();
		turnOn();
		if (!runOnPI) {
			startStopListener();
		}
	}

	private void setUpPort() {
		boolean valid = false;
		System.out
				.println("Type port number (use 1025 - 65535) or \"d\" to use default ("
						+ DEF_PORT + ")");
		while (!valid) {
			String res = Tools.waitForInput(System.in);
			if (res.equals("d")) {
				res = DEF_PORT + "";
			}
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
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Only used when running on laptop.
	 */
	private void startStopListener() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					String line = Tools.waitForInput(System.in);
					if (line.startsWith("q")) {
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
			toReturn = true;
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
				System.gc();
			}
			isOn = false;
			toReturn = true;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		return String.valueOf(toReturn);
	}

	public void quit() {
		sock.stopServer();
		turnOff();
	}

	public byte[] getValues() {
		if (!isOn) {
			return null;
		}
		String toReturn = "";
		for (final File fileEntry : new File(PATH).listFiles()) {
			if (!fileEntry.isDirectory()) {
				try {
					System.out.println(fileEntry.getName());
					BufferedReader reader = new BufferedReader(new FileReader(
							fileEntry));
					String line = reader.readLine();
					while (line != null) {
						toReturn += line + ";";
						line = reader.readLine();
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return toReturn.getBytes();
	}

	public String getColor() {
		if (isOn && runOnPI) {
			return lamp.getColor().toString();
		} else {
			return ColorType.NONE.toString();
		}
	}

	public void colorChanger(ColorType color) {
		lamp.setColor(color);
	}
	
	public void colorChanger(int value) {
		if (value < 0) {
			return; //test case
		}
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
