package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class Simulator extends Thread {
	private Timer timer = new Timer();
	private PowerMonitor pm;
	private MainManager mm;
	private TimerTask task = new TimerTask() {
		public void run() {
			String s = userInput("> ");
			//Microwave = 700, Mixer = 150, Refrigerator = 500, Shaver = 9, Light = 16, Notebook = 50, TV = 50,
			//Coffee maker = 800, Dishwasher = 1000,
			if ("refrigerator".equals(s.toLowerCase())) {
				pm.setPulse(500);
			} else if ("microwave".equals(s.toLowerCase())) {
				pm.setPulse(700);
			} else if ("mixer".equals(s.toLowerCase())) {
				pm.setPulse(150);
			} else if ("shaver".equals(s.toLowerCase())) {
				pm.setPulse(9);
			} else if ("lamp".equals(s.toLowerCase())) {
				pm.setPulse(50);
			} else if ("tv".equals(s.toLowerCase())) {
				pm.setPulse(50);
			} else if ("coffee".equals(s.toLowerCase())) {
				pm.setPulse(800);
			} else if ("dishwasher".equals(s.toLowerCase())) {
				pm.setPulse(1000);
			} else if ("red".equals(s.toLowerCase())) {
				pm.setPulse(351);
			} else if ("orange".equals(s.toLowerCase())) {
				pm.setPulse(151);
			} else if ("green".equals(s.toLowerCase())) {
				pm.setPulse(0);
			} else if ("help".equals(s.toLowerCase())) {
				System.out.println("microwave, mixer, shaver, lamp, tv, coffee, dishwasher, red, orange, green or quit");
			} else if ("quit".equals(s.toLowerCase())) {
				timer.cancel();
				mm.quit();
			} else {
				System.out.println("Unknown command [" + s + "]");
				System.out.println("Choose: microwave, mixer, shaver, lamp, tv, coffee, dishwasher, red, orange, green or quit");
			}
		}
	};
	private static final BufferedReader stdin = new BufferedReader(
			new InputStreamReader(System.in));

	public Simulator(PowerMonitor pm, MainManager mm) {
		this.pm = pm;
		this.mm = mm;
	}
	
	public static String userInput(String prompt) {
		String retString = "";
		System.err.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			System.out.println(e);
			try {
				userInput("<Oooch/>");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return retString;
	}

	public void run() {
		timer.scheduleAtFixedRate(task, 0, 1000);
	}
}
