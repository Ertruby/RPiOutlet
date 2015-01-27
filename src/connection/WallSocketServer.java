package connection;

import gpio.ColorType;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import main.MainManager;
import tools.Logger;
import tools.Tools;

//import android.util.Log;

public class WallSocketServer extends Thread {
	
	private MainManager mm;
	private SSLServerSocket socket;
	private List<WallSocketSession> activeSessions;

	private boolean stop = false;
	private boolean stopped;

	public WallSocketServer(MainManager mm, int portNr) {
		this.mm = mm;
		activeSessions = new ArrayList<WallSocketSession>();
		setName("PiServer");
		System.setProperty("javax.net.ssl.keyStore", "server.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "WScDrone5A");
		try {
			Logger.log("Starting server on: " + Arrays.toString(getCurrIPs(true).toArray()) + " : " + portNr);
		} catch (SocketException e) {
			Logger.logError("Could not create server socket: " + e);
			System.exit(1);
		}
		try {
			SSLServerSocketFactory sslf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			socket = (SSLServerSocket) sslf.createServerSocket(portNr);
		} catch (IOException e) {
			Logger.logError("Could not create server socket: " + e);
			System.exit(1);
		}
		try {
			socket.setSoTimeout(1000);
		} catch (SocketException e) {
			Logger.logError("Could not set socket timeout: " + e);
		}
	}

	@Override
	public void run() {
		Logger.log("WSc server started successfully.");
		while (!stop) {
			stopped = false;
			SSLSocket client = null;
			try {
				client = (SSLSocket) socket.accept();
			} catch (SocketTimeoutException e) {
				// do nothing, this is normal
				continue;
			} catch (IOException e) {
				Logger.log("Exception accepting connection: " + e);
			}
			if (client != null) {
				Logger.log("New client accepted: " + client.getInetAddress());
				WallSocketSession session;
				try {
					session = new WallSocketSession(mm, this, client);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				// find and remove old duplicates
				WallSocketSession duplicate = null;
				for (WallSocketSession aSession : activeSessions) {
					if (aSession.getHostAddress().equals(session.getHostAddress())) {
						duplicate = aSession;
					}
				}
				if (duplicate != null) {
					duplicate.stopSession(false);
					activeSessions.remove(duplicate);
				}
				activeSessions.add(session);
				session.setName("Session-" + activeSessions.size() + "-" 
						+ client.getInetAddress().getHostAddress());
				session.start();
			}
		}
		stopped = true;
		Logger.log("WSc server stopped successfully.");
	}

	public void stopServer() {
		Logger.log("Stopping WSc server...");
		for (WallSocketSession session : activeSessions) {
			session.stopSession(true);
		}
		stop = true;
		while (!stopped) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
		try {
			socket.close();
		} catch (IOException e) {
			Logger.logError("Could not close WSc server socket: " + e);
		}
	}
	
	public void broadcastColor(ColorType color) {
		List<WallSocketSession> deadSessions = new ArrayList<WallSocketSession>();
		for (WallSocketSession session : activeSessions) {
			try {
				session.sendPacket(Packet.createCommandPacket(Command.setColor(color)));
			} catch (IOException e) {
				deadSessions.add(session);
			}
		}
		activeSessions.removeAll(deadSessions);
	}
	
	public void broadcastState(boolean turnedOn) {
		List<WallSocketSession> deadSessions = new ArrayList<WallSocketSession>();
		for (WallSocketSession session : activeSessions) {
			try {
				session.sendPacket(Packet.createCommandPacket(Command.setState(turnedOn)));
			} catch (IOException e) {
				deadSessions.add(session);
			}
		}
		activeSessions.removeAll(deadSessions);		
	}
	
	private List<InetAddress> getCurrIPs(boolean onlyIPv4) throws SocketException {
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while (en.hasMoreElements()) {
		    NetworkInterface ni=(NetworkInterface) en.nextElement();
		    Enumeration<InetAddress> ee = ni.getInetAddresses();
		    while (ee.hasMoreElements()) {
		    	InetAddress addr = ee.nextElement();
		    	if (!addr.getHostAddress().equals(InetAddress.getLoopbackAddress().getHostAddress())) {
			    	if ((onlyIPv4 && addr instanceof Inet4Address) || !onlyIPv4) {
			    		addresses.add((InetAddress) addr);
			    	}
		    	}

		    }
		}
		return addresses;
	}

	public void unregister(WallSocketSession session) {
		activeSessions.remove(session);
	}
}
