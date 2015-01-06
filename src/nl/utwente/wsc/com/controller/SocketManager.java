package nl.utwente.wsc.com.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import nl.utwente.wsc.com.model.Command;
import nl.utwente.wsc.com.model.Packet;
import nl.utwente.wsc.com.model.PacketHeader;
import nl.utwente.wsc.com.model.exception.InvalidPacketException;
import nl.utwente.wsc.utils.Timer;
import nl.utwente.wsc.utils.Tools;

/**
 * Client socket implementation.
 *
 * @author rvemous
 * @param <receiveBuffer>
 */
public class SocketManager extends java.util.Observable {
    
    public static final int TIMEOUT = 10000; //ms
    public static final String CONNECTION_DEAD = "DEAD";
    private final Object lock = new Object();
    
    private boolean stop = false;
    
    private Socket sock;
    private SSLSession session;
    
    private BufferedInputStream in;
    private BufferedOutputStream out;
    
    private volatile LinkedList<Packet> receiveBuffer;

    /**
     * Creates a new socket manager which is connected to the requested 
     * host.
     * 
     * @param address the address of the host
     * @param portNr the post to connect to
     * @param timeout time-out to use for connecting
     * @throws IOException 
     */
    public SocketManager(InetAddress address, int portNr, int timeout) throws IOException {
        System.setProperty("javax.net.ssl.trustStore", "keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "picloudkeypass");

        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        sock = ssf.createSocket(address.getHostAddress(), portNr);
        session = ((SSLSocket) sock).getSession();
        
        in = new BufferedInputStream(sock.getInputStream());
        out = new BufferedOutputStream(sock.getOutputStream());
        
        receiveBuffer = new LinkedList<Packet>();
        startReceiverThread();
    }
    
    public boolean alive() {
        return session != null && session.isValid() && !stop;
    }
    
    /**
     * Sends one packet to the server.
     * 
     * @param packet to send
     * @throws IOException when connection problems occur.
     */
    public void sendPacket(Packet packet) throws IOException {
        out.write(packet.toSendablePacket());
        out.flush();
    }
    
    /**
     * Starts the receiver thread which will read packats and will send 
     * them to any observers.
     */
    private void startReceiverThread() {
    	Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				byte[] headerbuff = new byte[PacketHeader.HEADER_LENGTH];
	            while (!stop) {
	                do {
	                    try {
	                        headerbuff[0] = (byte) in.read();
	                    } catch (IOException ex) {
	                        //notifyObserversSetchanged(CONNECTION_DEAD);
	                        ex.printStackTrace();
	                        stop = true;
	                        continue;
	                    }
	                    Tools.waitForMs(50);
	                } while (!stop && headerbuff[0] == -1);
	                try {
	                    in.read(headerbuff, 1, headerbuff.length - 1);
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                    stop = true;
	                    //notifyObserversSetchanged(CONNECTION_DEAD);
	                    continue;                  
	                }
	                PacketHeader header = null;
	                try {
	                    header = new PacketHeader(headerbuff);
	                } catch (InvalidPacketException ex) {
	                    System.out.println("Got invalid header: " + 
	                            Arrays.toString(headerbuff));
	                    continue;
	                }
	                int len = header.getPacketLength();
	                //System.out.println(header.toString());
	                byte[] receiverBuff = new byte[len];
	                int i = 0;
	                try {
	                    while (i < len && 
	                            (i += in.read(receiverBuff, i, len - i)) != -1){}
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                    stop = true;
	                    //notifyObserversSetchanged(CONNECTION_DEAD);
	                    continue;
	                }
	                Packet packet = new Packet(header, receiverBuff);
	                //synchronized (lock) {
	                    receiveBuffer.add(packet);
	                //}
	                //System.out.println("Got packet: " + packet.toString());
	            }
			}
		});
        thread.setName("Packet-receiver");
        thread.setDaemon(true);
        thread.start();
    }
    
    public Packet waitForPacket(int timeOut) {
        Packet packet = null;
        Timer timer = new Timer(timeOut, true);
        while (!stop && !timer.hasExpired()) {
            //synchronized (lock) {
                if (!receiveBuffer.isEmpty()) {
                    return receiveBuffer.removeFirst();
                }
            //}
            Tools.waitForMs(50);
        }
        return packet;
    }
    
    public synchronized boolean checkFile(long id) throws IOException {
        sendPacket(Packet.createCommandPacket(
                Command.checkFileCommand(id)));  
        Packet ans = waitForPacket(TIMEOUT);
        return Packet.isSuccesResponse(ans);
    }
        
    public synchronized byte[] downloadFile(long id) throws IOException {
        sendPacket(Packet.createCommandPacket(
                Command.downloadFileCommand(id)));  
        Packet ans = waitForPacket(TIMEOUT);
        if (!Packet.isSuccesResponse(ans)) {
            return null;
        }
        Packet data = waitForPacket(TIMEOUT * 6);
        if (!Packet.isDataPacket(data)) {
            return null;
        }
        return data.getData();
    }
    
    public synchronized long uploadFile(byte[] data) throws IOException {
        sendPacket(Packet.createCommandPacket(
                Command.uploadFileCommand(data.length)));  
        Packet ans1 = waitForPacket(TIMEOUT);
        if (!Packet.isSuccesResponse(ans1)) {
            return -1;
        }
        sendPacket(Packet.createDataPacket(data));
        Packet ans2 = waitForPacket(TIMEOUT);
        if (!Packet.isResponsePacket(ans2)) {
            return -2;
        }
        long id = 0;
        try {
            id = Long.parseLong(new String(ans2.getData()));
        } catch (NumberFormatException e) {
            return -3;
        }
        return id;
    }
    
    public synchronized boolean deleteFile(long id) throws IOException {
        sendPacket(Packet.createCommandPacket(
                Command.deleteFileCommand(id)));         
        Packet ans = waitForPacket(TIMEOUT);
        return Packet.isSuccesResponse(ans);  
    }
    
    public synchronized void stopServer() throws IOException {
        sendPacket(Packet.createCommandPacket(
                Command.stopServerCommand()));  
    }
    
    /**
     * Shuts down client nicely.
     */
    public void shutdown() {
        stop = true;
        Tools.waitForMs(100);
        session.invalidate();
        try {
            sock.close();
        } catch (IOException ex) {/* socket closed by server */}
    }
    
    public void notifyObserversSetchanged(Object arg) {
        setChanged();
        notifyObservers(arg);
    }
    
    private void printSessionInfo() {
        Certificate[] cchain = null;
        try {
            cchain = session.getPeerCertificates();
        } catch (SSLPeerUnverifiedException ex) {
            ex.printStackTrace();
        }
        System.out.println("The Certificates used by peer");
        for (int i = 0; i < cchain.length; i++) {
            System.out.println(((X509Certificate) cchain[i]).getSubjectDN());
        }
        System.out.println("Peer host is " + session.getPeerHost());
        System.out.println("Cipher is " + session.getCipherSuite());
        System.out.println("Protocol is " + session.getProtocol());
        System.out.println("ID is " + new BigInteger(session.getId()));
        System.out.println("Session created in " + session.getCreationTime());
        System.out.println("Session accessed in " + session.getLastAccessedTime());
    }
    
}
