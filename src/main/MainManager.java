package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;

import tools.Logger;
import tools.Tools;
import connection.WallSocketServer;
import gpio.ColorType;
import gpio.LEDController;
import gpio.SwitchController;
import gpio.UsageMonitor;

public class MainManager {

	public static final int DEF_PORT = 7331;
	private static final String PATH = "files/";
	
	private String helpString = ">> Supported commands:\n  Program:\tq (quit program), h (display this help)\n"
			+ "  Socket:\tis on (check state), on (turn on), off (turn off), man (set manual color), auto (set automatic color)\n"
			+ "  LED color:\tis color (check color), r (set color red), g (set color green), b (set color blue), o (set color orange), n (set color none), reset color (reset auto color), reset hist (resets all usage history)";

	private int port = DEF_PORT;
	private boolean isOn = false;
	private WallSocketServer sock;
	private SwitchController switchController;
	private PowerReader powerReader;
	private UsageMonitor usageMonitor;
	private LEDController led;
	
	private boolean manual;
	private ColorType currMonitorColor;
	private ColorType currUserColor; // used when the user overrides the usage monitor's color or always when not running on the pi

	public static boolean RUN_ON_PI = 
			System.getProperty("os.name").equals("Linux") && 
			!System.getProperty("sun.arch.data.model").equals("64");

	public MainManager() {
		Logger.log("WSc program started.");
		currMonitorColor = ColorType.BLUE;
		currUserColor = ColorType.GREEN;
		manual = !RUN_ON_PI;
		// set up port
		setUpPort();
		// start switch controller
		switchController = new SwitchController("C", 149); //change letter and code if necessary.
		// start led controller if on RPi
		if (RUN_ON_PI) {
			led = LEDController.getInstance();
		}
		// create usage monitor (Link between usage reader and led)
		usageMonitor = new UsageMonitor(this);
		// start server
		Logger.log("Starting the socket server...");
		sock = new WallSocketServer(this, port);
		sock.start();
		// turn on socket (default)
		turnOn();
		// starts admin controller
		startAdminController();
		// update led color
		updateLEDColor();
	}

	private void setUpPort() {
		boolean valid = false;
		System.out.println(">> Type port number (use 1025 - 65535) "
				+ "or ENTER to use default (" + DEF_PORT + ")");
		while (!valid) {
			System.out.print("<");
			String res = Tools.waitForInput(System.in);
			if (res.equals("")) {
				res = DEF_PORT + "";
			}
			ServerSocket testSock = null;
			try {
				int portnr = Integer.parseInt(res);
				testSock = new ServerSocket(portnr);
				valid = true;
				port = portnr;
			} catch (IllegalArgumentException e) {
				System.out.println(">>Invalid port number, please try again");
			} catch (IOException e) {
				System.out.println(">>Port is already in use, please try again");
			} finally {
				if (testSock != null) {
					try {
						testSock.close();
					} catch (IOException e) {}
				}
			}
		}
	}

	/**
	 * WSc controller from terminal.<br>
	 * Overrides the app clients.
	 */
	private void startAdminController() {
		Thread controller = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean stop = false;
				while (!stop) {
					System.out.print("<");
					String line = Tools.waitForInput(System.in);
					if (line.equals("q")) {
						quit();
						stop = true;
					} else if (line.equals("h")) {
						System.out.println(">> Help\n" + helpString);
					} else if (line.equals("reset color")) {
				    	// resets the usage monitor history (and hereby its color)
						if (!RUN_ON_PI) {
							System.out.println(">> Reset auto color unavailable: Not running on a RPi!");
						} else {
							usageMonitor.resetHistory();
							setMonitorColor(ColorType.GREEN);
						}
					} else if (line.equals("reset hist")) {
				    	// resets the usage monitor history
						if (!RUN_ON_PI) {
							System.out.println(">> Reset history unavailable: Not running on a RPi!");
						} else {
							powerReader.deleteUsageHistory(); 
							//TODO broadcast new getValues()
						}
					} else if (line.equals("is on") || line.equals("is off")) {
						System.out.println(">> WSc socket is: " + (isOn ? "on" : "off"));
					} else if (line.equals("off")) {
						if (turnOff().equals("true")) {
							sock.broadcastState(false);
						}
					} else if (line.equals("on")) {
						if (turnOn().equals("true")) {
							sock.broadcastState(true);
						}
					} else if (line.equals("is man") || line.startsWith("is auto")) {
						System.out.println(">> WSc LED controller is: " + (manual ? "manual" : "automatic"));
					} else if (line.equals("man")) {
						// set manual and use user color
						if (manual) {
							return;
						}
						manual = true;
						updateLEDColor();
					} else if (line.equals("auto")) {
						// set automatic and use monitor color
						if (!RUN_ON_PI) {
							System.out.println(">> Auto mode unavailable: Not running on a RPi!");
						} else {
							if (!manual) {
								return;
							}
							manual = false;
							updateLEDColor();
						}
					} else if (line.equals("is color")) {
						System.out.println(">> WSc LED color is: " + getColor());
					} else if (line.equals("r")) {
						// set user color red
						setUserColor(ColorType.RED);
					} else if (line.equals("g")) {
						// set user color green
						setUserColor(ColorType.GREEN);
					} else if (line.equals("b")) {
						// set user color blue
						setUserColor(ColorType.BLUE);
					} else if (line.equals("o")) {
						// set user color orange
						setUserColor(ColorType.ORANGE);
					} else if (line.equals("n")) {
						// set user color none
						setUserColor(ColorType.NONE);
					} else {
						System.out.println(">> Unknown command.\n" + helpString);
					}
					
				}
			}
		}, "Admin-controller-thread");
		controller.start();
	}

	public synchronized String isOn() {
		return String.valueOf(isOn);
	}

	public synchronized String turnOn() {
		boolean toReturn = false;
		if (!isOn) {
			isOn = true;
			if (RUN_ON_PI) {
				Logger.log("Switching on socket: " + 
						(switchController.turnOn() ? "succeeded" : "failed"));
				Logger.log("Starting power reader...");
				powerReader = new PowerReader(this, usageMonitor);
				powerReader.start();
			}
			updateLEDColor();
			Logger.log("Turned socket on successfully.");
			toReturn = true;
		}
		return String.valueOf(toReturn);
	}

	public synchronized String turnOff() {
		boolean toReturn = false;
		if (isOn) {
			isOn = false;
			if (RUN_ON_PI) {
				Logger.log("Turning off socket: " + 
						(switchController.turnOff() ? "succeeded" : "failed"));
				Logger.log("Stopping power reader...");
				powerReader.shutdown();
				powerReader = null;
			}
			updateLEDColor();
			Logger.log("Turned socket off successfully.");
			toReturn = true;
		}
		return String.valueOf(toReturn);
	}

	public synchronized void quit() { 
		sock.stopServer();
		turnOff();
		Logger.log("WSc shutdown successful.");
	}

	public synchronized byte[] getValues() {
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

	public synchronized ColorType getColor() {
		if (isOn) {
			if (manual) {
				return currUserColor;
			} else {
				return currMonitorColor;
			}
		} else {
			return ColorType.NONE;
		}
	}
	
	public void setMonitorColor(ColorType color) {
		if (color.equals(currMonitorColor)) {
			return;
		}
		currMonitorColor = color;
		updateLEDColor();
	}
	
	private void setUserColor(ColorType color) {
		if (color.equals(currUserColor)) {
			return;
		}
		currUserColor = color;
		updateLEDColor();
	}
	
	/**
	 * Updates the color of the LED.<br>
	 * Can be called by the usage monitor or overridden using user input
	 * @param value
	 */
	private synchronized void updateLEDColor() {
		ColorType usedColor;
		if (isOn) {
			if (manual) {
				usedColor = currUserColor;
			} else {
				usedColor = currMonitorColor;
			}
		} else {
			usedColor = ColorType.NONE;
		}
		if (RUN_ON_PI) {
			if (led.getColor().equals(usedColor)) {
				return;
			}
			led.setColor(usedColor);
		}
		sock.broadcastColor(usedColor);

	}
	
	public static void main(String args[]) {
		new MainManager();
	}
}
