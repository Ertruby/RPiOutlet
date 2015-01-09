package connection;

//import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
//import java.util.HashSet;
import java.util.List;
//import java.util.Set;


import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import main.MainManager;

//import android.util.Log;

public class WallSocketServer extends Thread {
	
	private MainManager mm;
	private SSLServerSocket socket;
	private List<WallSocketSession> activeSessions;

	private boolean stop = false;

	public WallSocketServer(MainManager mm, int portNr) {
		this.mm = mm;
		activeSessions = new ArrayList<WallSocketSession>();
		setName("PiServer");
		System.setProperty("javax.net.ssl.keyStore", "keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "picloudkeypass");
//		Log.d(this.toString(), "Starting server on port 20022");
		try {
			SSLServerSocketFactory sslf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			socket = (SSLServerSocket) sslf.createServerSocket(portNr);
		} catch (IOException e) {
//			Log.e(this.toString(), "Could not create server socket: " + e);
			System.out.println("Could not create server socket: " + e);
			System.exit(1);
		}
		try {
			socket.setSoTimeout(1000);
		} catch (SocketException e) {
//			Log.e(this.toString(), "Could not set socket timeout: " + e);
		}
//		Log.d(this.toString(), "Socket created");
	}

	@Override
	public void run() {
		//Log.d(this.toString(), "Starting server loop.");
		while (!stop) {
			SSLSocket client = null;
			try {
				client = (SSLSocket) socket.accept();
			} catch (SocketTimeoutException e) {
				// do nothing, this is normal
				continue;
			} catch (IOException e) {
				//Log.e(this.toString(), "Exception accepting connection: " + e);
			}
			if (client != null) {
				//Log.d(this.toString(), "New client accepted: " + client.getInetAddress());
				WallSocketSession session;
				try {
					session = new WallSocketSession(mm, this, client);
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}			
				activeSessions.add(session);
				session.setName("Session-" + activeSessions.size() + "-" 
						+ client.getInetAddress().getHostAddress());
				session.start();
			}
		}
		//Logger.log("Server stopped.");
	}

	public void stopServer() {
		//Logger.log("Stopping Server.");
		for (WallSocketSession session: activeSessions) {
			session.stopSession();
		}
		stop = true;
		try {
			socket.close();
		} catch (IOException e) {
			//Logger.logError("Could not close server socket: " + e);
		}
	}

	public void unregister(WallSocketSession session) {
		activeSessions.remove(session);
	}
}
