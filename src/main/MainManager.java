package main;

import connection.WallSocketServer;
import gpio.RGBLed;

public class MainManager {
	
	public static final int port = 7331;
	private static WallSocketServer sock = null;
	private static RGBLed lamp = null;
	
	public MainManager() {
		//create a socketmanager
		System.out.println("Starting a socket manager...");
		newSocketManager();
		
		//create a lampmanager
		System.out.println("Starting a lamp controller...");
		//newLampManager();
	}
	
	public void newSocketManager() {
		sock = new WallSocketServer(this, port);
		sock.start();
	}
	
	public void newLampManager() {
		lamp = new RGBLed();
		lamp.start();
		int i = 0;
		boolean go = true;
		
		while (go) {
			if (i == 0) {
				lamp.toggleGreen();
				i++;
			} else if (i == 1) {
				lamp.toggleRed();
				i++;
			} else if (i == 2) {
				lamp.toggleGreen();
				i++;
			} else {
				go = false;
			}
			
			try {
				RGBLed.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		lamp.shutdown();
	}
	
	public static void main(String args[]) {
		new MainManager();
	
		
	}
}
