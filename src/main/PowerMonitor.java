package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class PowerMonitor extends Thread {
	private Timer timer = new Timer();
	private String path = "files/";
	private File folder = new File("files");
	private int pulseCounter = 0;
	private TimerTask task = new TimerTask() {
		public void run() {
			try {
				LocalDate date = new LocalDate();
				File f = new File(path + date.toString());
				f.createNewFile();
				LocalTime currentTime = new LocalTime();
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(path + date.toString(), true)));
				out.println(currentTime + "," + pulseCounter);
				pulseCounter = 0;
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	public PowerMonitor() {

	}

	public void run() {
		timer.scheduleAtFixedRate(task, 0, 1000);
//		System.out.println(readFromFiles());
	}

	public void receivePulse() {
		pulseCounter++;
	}
	
	public void shutdown() {
		timer.cancel();
	}
	
	public String readFromFiles() {
		String toReturn = "";
		for (final File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				try {
					System.out.println(fileEntry.getName());
					BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
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
		return toReturn;
	}

//	public static void main(String[] args) {
//		PowerMonitor p = new PowerMonitor();
//		p.start();
//	}
}
