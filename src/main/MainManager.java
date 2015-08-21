package main;

import gpio.ColorType;
import gpio.LEDController;
import gpio.SwitchController;
import gpio.UsageMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;

import org.joda.time.DateTime;

import tools.Logger;
import tools.Tools;
import connection.WallSocketServer;

public class MainManager {

	public static final int DEF_PORT = 7331;
	private static final String PATH = "files/";
	
	private String helpString = ">> Supported commands:\n  Program:\tq (quit program), h (display this help)\n"
			+ "  Socket:\tis on (check state), on (turn on), off (turn off), man (set manual color), auto (set automatic color), get dip (get the dip code setting), set dip (set dip code setting)\n"
			+ "  LED color:\tis color (check color), r (set color red), rf (set color red flashing), g (set color green), gf (set color green flashing), b (set color blue), o (set color orange), n (set color none), reset color (reset auto color), reset hist (resets all usage history)";

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

	public MainManager(boolean selectPortArg) {
		Logger.log("WSc program started.");
		currMonitorColor = ColorType.BLUE;
		currUserColor = ColorType.NONE;
		manual = !RUN_ON_PI;
		// set up port
		if (selectPortArg || !RUN_ON_PI) {
			setUpPort();
		}
		// start switch controller
		switchController = new SwitchController("C", 149); //change letter and code if necessary.
		// start led controller if on RPi
		if (RUN_ON_PI) {
			led = LEDController.getInstance();
		}
		// start server
		Logger.log("Starting the socket server...");
		sock = new WallSocketServer(this, port);
		sock.start();
		// create usage monitor (Link between usage reader and led)
		usageMonitor = new UsageMonitor(this);
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
					if (line.equals("q") || line.equals("quit")) {
						quit();
						stop = true;
					} else if (line.equals("h") || line.equals("help")) {
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
					} else if (line.equals("ofo")) {
						if (turnOff().equals("true")) {
							sock.broadcastState(false);
						}
						if (turnOn().equals("true")) {
							sock.broadcastState(true);
						}
					} else if (line.equals("is man") || line.startsWith("is auto")) {
						System.out.println(">> WSc LED controller is: " + (manual ? "manual" : "automatic"));
					} else if (line.equals("man")) {
						// set manual and use user color
						if (manual) {
							continue;
						}
						manual = true;
						updateLEDColor();
					} else if (line.equals("auto")) {
						// set automatic and use monitor color
						if (!RUN_ON_PI) {
							System.out.println(">> Auto mode unavailable: Not running on a RPi!");
						} else {
							if (!manual) {
								continue;
							}
							manual = false;
							updateLEDColor();
						}
					} else if (line.equals("get dip")) {
						System.out.println(">> The current dip config is: " + switchController.toString());
					} else if (line.startsWith("set dip")) {
						if (line.length() < 8) {
							System.out.println(">> Changing dip failed: invalid arguments");
							continue;	
						}
						line = line.substring(8);
						String[] args = line.split(" ");
						if (args.length != 2 || args[0].length() == 0 || args[1].length() == 0) {
							System.out.println(">> Changing dip failed: invalid arguments");
							continue;	
						}
						if (!Character.isUpperCase(args[0].charAt(0)) || !Character.isLetter(args[0].charAt(0))) {
							System.out.println(">> Changing dip failed: code letter \"" + args[1] + "\" is invalid (try A-Z)");
							continue;							
						}
						switchController.setCodeLetter(args[0]);
						try {
							int code = Integer.parseInt(args[1]);
							if (code < 0 || code > 1023) {
								System.out.println(">> Changing dip failed: code number \"" + args[1] + "\" is not within range (try 0-1023)!");
								continue;
							}
							switchController.setCodeNR(code);
							System.out.println(">> The new dip config is: " + switchController.toString());
						} catch (NumberFormatException e) {
							System.out.println(">> Changing dip failed: \"" + args[1] + "\" is not a valid code number!");	
						}	
					} else if (line.equals("is color")) {
						System.out.println(">> WSc LED color is: " + getColor(true));
					} else if (line.equals("r") || line.equals("red")) {
						// set user color red
						setUserColor(ColorType.RED);
					} else if (line.equals("rf") || line.equals("red flashing")) {
						// set user color red flashing
						setUserColor(ColorType.RED_FLASHING);
					} else if (line.equals("g") || line.equals("green")) {
						// set user color green
						setUserColor(ColorType.GREEN);
					} else if (line.equals("gf") || line.equals("green flashing")) {
						// set user color green flashing
						setUserColor(ColorType.GREEN_FLASHING);
					} else if (line.equals("b") || line.equals("blue")) {
						// set user color blue
						setUserColor(ColorType.BLUE);
					} else if (line.equals("o") || line.equals("orange")) {
						// set user color orange
						setUserColor(ColorType.ORANGE);
					} else if (line.equals("n") || line.equals("none")) {
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
	
	public synchronized void sendPowerValue(long time, double value) {	
		sock.broadcastPowerValue(time, value);
	}

	/**
	 * Gets usage data from the date specified.<br>
	 * Will return all data when formDate is 0 and will return an empty
	 * byte array when no data is available.
	 * 
	 * @param fromDate the date from which to send data
	 * @return an array of data
	 */
	public synchronized byte[] getValues(long fromDate) {	
		String toReturn = "";
		for (File fileEntry : new File(PATH).listFiles()) {
			// check whether it is a file
			if (!fileEntry.isDirectory()) {
				DateTime fileDate = new DateTime(fileEntry.getName());
				// check whether file date is not older than requested
				if (!fileDate.isBefore(fromDate)) {
					try {
						BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
						String line;
						while ((line = reader.readLine()) != null) {
							// check whether sample date is not older than requested
							long sampleDate = Long.parseLong(line.split(",")[0]);
							if (fromDate <= sampleDate) {
								toReturn += line + ";";
							}
						}
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}		
			}
		}
		if (toReturn.equals("")) {
			return new byte[0];
		}
		return toReturn.substring(0, toReturn.length() - 2).getBytes();
	}

	public synchronized ColorType getColor(boolean mayFlash) {
		if (isOn) {
			if (manual) {
				return mayFlash ? currUserColor : currUserColor.getNonFlashing();
			} else {
				return mayFlash ? currMonitorColor : currMonitorColor.getNonFlashing();
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
		sock.broadcastColor(usedColor.getNonFlashing());
	}
	
	public static void main(String args[]) {
		new MainManager(args.length > 0);
	}
}
