package main;

import gpio.UsageMonitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import tools.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;

public class PowerReader extends Thread {
	
	private static final int SAMPLE_INTERVAL = 50; //ms
	private static final String PATH = "files/";
	
	private MainManager mm;
	private UsageMonitor monitor;

	private static GpioController GPIO;
	private static GpioPinDigitalInput pinPulseReader;
	
	private boolean stop = false;
	private boolean stopped = false;
	
	//private double currUsage = 0;
	private LocalDate lastDate = new LocalDate();
	
	static {
		if (!new File(PATH).exists()) {
			new File(PATH).mkdir();
		}
		if (MainManager.RUN_ON_PI) {
			GPIO = GpioFactory.getInstance();
			pinPulseReader = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_04, "powerPulse");
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					GPIO.shutdown();			
				}
			}));
		}
	}	
	
	public PowerReader(MainManager mm, UsageMonitor monitor) {
		this.mm = mm;
		this.monitor = monitor;
	}
	
	@Override
	public void run() {
		Logger.log("Power reader started successfully.");
		int loopCounter = 1;
		long start = System.currentTimeMillis();	
		long lastPulseTime =-1;
		boolean pulseActive = false;
		stopped = false;
		while (!stop) {	
			if (MainManager.RUN_ON_PI) {
				if (pinPulseReader.isHigh()) {
					if (!pulseActive) {
						pulseActive = true;
						long pulseTime = System.currentTimeMillis();
						System.out.println("Pulse: " + pulseTime);
						if (lastPulseTime > 0) {
							System.out.println("Last pulse: " + lastPulseTime);
							double currUsage = calcUsage(pulseTime - lastPulseTime);
							System.out.println("Usage: " + currUsage + " watt");
							writeUsage(currUsage);
							monitor.addPowerUsage(pulseTime - lastPulseTime, currUsage);
						}
						lastPulseTime = pulseTime;
					}
				} else {
					if (pulseActive) {
						pulseActive = false;
					}
				}
			}
			while (System.currentTimeMillis() < start + SAMPLE_INTERVAL * loopCounter) {
				try {
					Thread.sleep(SAMPLE_INTERVAL / 10);
				} catch (InterruptedException e) {}
			}
			loopCounter++;
		}	
		writeUsage(0);
		Logger.log("Power reader stopped sucessfully.");
		stopped = true;
	}
	
	private void writeUsage(double currUsage) {	
		LocalDate date = new LocalDate();
		File f = new File(PATH + date.toString());
		if (!f.exists() || !lastDate.equals(date)) {
			lastDate = date;
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		PrintWriter out;
		try {
			out = new PrintWriter(
					new BufferedWriter(new FileWriter(f , true)));
			out.println(new DateTime().getMillis() + "," + currUsage);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void deleteUsageHistory() {
		File fileDir = new File(PATH);
		for (File usageHistoryFile : fileDir.listFiles()) {
			usageHistoryFile.delete();
		}
	}
	
	private double calcUsage(long intervalTime) {
		double minFraq = (intervalTime / 1000d) / 60d;
		return 30d / minFraq;
	}
		
	public void shutdown() {
		stop = true;
		while (!stopped) {
			try {
				Thread.sleep(SAMPLE_INTERVAL);
			} catch (InterruptedException e) {}
		}
	}
}
