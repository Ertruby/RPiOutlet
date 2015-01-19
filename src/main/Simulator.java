package main;

import gpio.ColorType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class Simulator extends Thread {
	
	private Timer timer = new Timer();
	private PowerMonitor pm;
	private MainManager mm;
	
	private String helpString = "Items: mic(rowave), mix(er), ref(rigerator), s(haver), l(amp), tv, c(offee), "
			+ "d(ishwasher)\nColors: r(ed), o(range), g(reen), b(lue), n(one)\nOther: h(elp), q(uit)";
	
	private TimerTask task = new TimerTask() {
		public void run() {
			String s = userInput("> ");
			//Microwave = 700, Mixer = 150, Refrigerator = 500, Shaver = 9, Light = 16, Notebook = 50, TV = 50,
			//Coffee maker = 800, Dishwasher = 1000,
			if (s.startsWith("ref")) {
				pm.setPulse(500);
			} else if (s.startsWith("mic")) {
				pm.setPulse(700);
			} else if (s.startsWith("mix")) {
				pm.setPulse(150);
			} else if (s.startsWith("s")) {
				pm.setPulse(9);
			} else if (s.startsWith("l")) {
				pm.setPulse(50);
			} else if (s.startsWith("tv")) {
				pm.setPulse(50);
			} else if (s.startsWith("c")) {
				pm.setPulse(800);
			} else if (s.startsWith("d")) {
				pm.setPulse(1000);
			} else if (s.startsWith("r")) {
				pm.setPulse(351);
			} else if (s.startsWith("o")) {
				pm.setPulse(151);
			} else if (s.startsWith("g")) {
				pm.setPulse(0);
			} else if (s.startsWith("b")) {
				pm.setPulse(-2);
				mm.colorChanger(ColorType.BLUE);
			} else if (s.startsWith("n")) {
				pm.setPulse(-1);
				mm.colorChanger(ColorType.NONE);
			} else if (s.startsWith("h")) {
				System.out.println(helpString);
			} else if (s.startsWith("q")) {
				mm.quit();
				System.exit(0);
			} else {
				System.out.println("Unknown command [" + s + "]");
				System.out.println("Choose: "+ helpString);
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
