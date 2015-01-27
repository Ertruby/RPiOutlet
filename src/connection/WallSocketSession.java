
package connection;

import gpio.ColorType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import javax.net.ssl.SSLSocket;

import connection.exception.InvalidPacketException;
import tools.Logger;
import tools.Tools;
import main.MainManager;

public class WallSocketSession extends Thread {
	
	private final MainManager mm;
	private final WallSocketServer server;
	private final SSLSocket socket;
	
    private boolean stop = false;
    private boolean stopped = true;
    
    private BufferedInputStream in;
    private BufferedOutputStream out;

	public WallSocketSession(MainManager mm, WallSocketServer server, SSLSocket socket) throws IOException {
		this.mm = mm;
		this.server = server;
		this.socket = socket;		
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());       
	}
	
    /**
     * Sends one packet to the client.
     * 
     * @param packet to send
     * @throws IOException when connection problems occur.
     */
    public void sendPacket(Packet packet) throws IOException {
    	System.out.println("Send packet: " + packet.toString());
        out.write(packet.toSendablePacket());
        out.flush();
    }

	@Override
	public void run() {
		Logger.log("Starting session for " + socket.getInetAddress());
		byte[] headerbuff = new byte[PacketHeader.HEADER_LENGTH];
		stopped = false;
		mainloop:
        while (!stop) {
            do {
                try {
                    headerbuff[0] = (byte) in.read();
                } catch (IOException ex) {
                	Logger.logError("Connection dead - while waiting");
                	selfShutdown();
                    break mainloop;
                }
                Tools.waitForMs(50);
            } while (!stop && headerbuff[0] == -1);
            try {
                in.read(headerbuff, 1, headerbuff.length - 1);
            } catch (IOException ex) {
            	Logger.logError("Connection dead - while reading header");
            	selfShutdown();
                break mainloop;                  
            }
            PacketHeader header = null;
            try {
                header = new PacketHeader(headerbuff);
            } catch (InvalidPacketException ex) {
            	Logger.logError("Got invalid header: " + 
                        Arrays.toString(headerbuff));
                continue;
            }
            int len = header.getPacketLength();
            byte[] receiverBuff = new byte[len];
            int i = 0;
            try {
                while (i < len && 
                        (i += in.read(receiverBuff, i, len - i)) != -1){}
            } catch (IOException ex) {
            	Logger.logError("Connection dead - while reading data");
                selfShutdown();
                break mainloop;
            }
            Packet packet = new Packet(header, receiverBuff);
            packetHandler(packet);
        }
		stopped = true;
	}
	
	public String getHostAddress() {
		return socket.getInetAddress().getHostAddress();
	}
	
	private void packetHandler(Packet packet) {
		try { 
			if (!Packet.isCommandPacket(packet)) {
				Logger.log("Got " + (Packet.isDataPacket(packet) ? "data": "answer") 
						+ " packet: " + packet.toString());
				return;
			}
			Logger.log("Got command packet: " + packet.toString());	
			if (Command.isIsOnCommand(packet.getData())) {
				sendPacket(Packet.createResponse(mm.isOn()));
			} else if (Command.isTurnOnCommand(packet.getData())) {
				sendPacket(Packet.createResponse(mm.turnOn()));
			} else if (Command.isTurnOffCommand(packet.getData())) {
				sendPacket(Packet.createResponse(mm.turnOff()));
			} else if (Command.isGetValuesCommand(packet.getData())) {
				sendPacket(Packet.createDataPacket(mm.getValues()));
			} else if (Command.isGetColorCommand(packet.getData())) {
				sendPacket(Packet.createResponse(mm.getColor(false).toString()));
			} else {
				return;
			}		
		} catch(IOException e) {
			Logger.logError(e);
		}
	}
	
	public void selfShutdown() {
		stop = true;
		server.unregister(this);
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			// socket already closed
		}
	}
	
	public void stopSession(boolean sendDeadPacket) {
		stop = true;
		if (sendDeadPacket) {
			try {
				sendPacket(Packet.createCommandStringPacket("DEAD"));
			} catch (IOException e) {
				Logger.logError(e);
			}
		}
		/*while (!stopped) {
			 try {
				Thread.sleep(50);
			 } catch (InterruptedException e) {}
		}*/
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			Logger.logError(e);
		}
	}

}
