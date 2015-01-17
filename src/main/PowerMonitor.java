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
	private static final String PATH = "files/";
	private Timer timer = new Timer();
	private MainManager mm;
	private Simulator sim;
	//true = simulation, false = normal execution
	private boolean simulate = System.getProperty("os.name").equals("Linux");
	
	//testing only
	//Microwave = 700, Mixer = 150, Refrigerator = 500, Shaver = 9, Light = 16, Notebook = 50, TV = 50,
	//Coffee maker = 800, Dishwasher = 1000,
//	private int[] test = {23,243,564,150,147,240,254,700,800,230,245,20,239,2394,29,210,459,983,648,1837,453};
//	private int j = 0;
	
	//n * 1 sec
	private int interval = 1*1000;
	private int pulseCounter = 0;
	private int lastValue = 0;
	
	static {
		if (!new File(PATH).exists()) {
			new File(PATH).mkdir();
		}
	}
	
	private TimerTask task = new TimerTask() {
		public void run() {
			try {
				LocalDate date = new LocalDate();
				File f = new File(PATH + date.toString());
				f.createNewFile();
				LocalTime currentTime = new LocalTime();
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(PATH + date.toString(), true)));
				
				//testing only
//				if (j < test.length) {
//					out.println(currentTime + "," + test[j]);
//					mm.colorChanger(test[j]);
//					j++;
//				} else {
//					j = 0;
//				}
				//else 
				out.println(currentTime + "," + pulseCounter);
				if (simulate) {
					mm.colorChanger(pulseCounter);				
				}
				//pulseCounter = 0;
				
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	public PowerMonitor(MainManager mm) {
		this.mm = mm;
		if (simulate) {
			System.out.println("Simulation started");
			sim = new Simulator(this, mm);
			sim.start();
		}
	}
	
	public int getLast() {
		return lastValue;
	}

	public void run() {
		//task, delay, interval
		timer.scheduleAtFixedRate(task, 60, interval);
	}

	public void receivePulse() {
		pulseCounter++;
	}
	
	//simulation only
	public void setPulse(int watt) {
		pulseCounter = watt;
	}
	
	public void shutdown() {
		timer.cancel();
	}
	
	public String readFromFiles() {
		String toReturn = "";
		for (final File fileEntry : new File(PATH).listFiles()) {
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
}
