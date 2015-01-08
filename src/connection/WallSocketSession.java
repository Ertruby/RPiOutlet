package connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import javax.net.ssl.SSLSocket;

import nl.utwente.wsc.com.model.exception.InvalidPacketException;
import nl.utwente.wsc.utils.Tools;

public class WallSocketSession extends Thread {
	
	private final MainManager mm;
	private final WallSocketServer server;
	private final SSLSocket socket;
	
    private boolean stop = false;
    
    private BufferedInputStream in;
    private BufferedOutputStream out;
    
    private volatile LinkedList<Packet> receiveBuffer;
    private final Object lock = new Object();

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
        out.write(packet.toSendablePacket());
        out.flush();
    }

	@Override
	public void run() {
		//Logger.log("Starting session for " + socket.getInetAddress());
		byte[] headerbuff = new byte[PacketHeader.HEADER_LENGTH];
        while (!stop) {
            do {
                try {
                    headerbuff[0] = (byte) in.read();
                } catch (IOException ex) {
                    //Log.e(this.toString(), "Connection dead");
                    stop = true;
                    continue;
                }
                Tools.waitForMs(50);
            } while (!stop && headerbuff[0] == -1);
            try {
                in.read(headerbuff, 1, headerbuff.length - 1);
            } catch (IOException ex) {
            	//Log.e(this.toString(), "Connection dead");
            	server.unregister(this);
                stop = true;
                continue;                  
            }
            PacketHeader header = null;
            try {
                header = new PacketHeader(headerbuff);
            } catch (InvalidPacketException ex) {
                //Log.e(this.toString(), "Got invalid header: " + 
                        //Arrays.toString(headerbuff));
                continue;
            }
            int len = header.getPacketLength();
            byte[] receiverBuff = new byte[len];
            int i = 0;
            try {
                while (i < len && 
                        (i += in.read(receiverBuff, i, len - i)) != -1){}
            } catch (IOException ex) {
                ex.printStackTrace();
                stop = true;
                continue;
            }
            Packet packet = new Packet(header, receiverBuff);
            packetHandler(packet);
            //Log.d(this.toString(), "Got packet: " + packet.toString());
        }
	}
	
	private void packetHandler(Packet packet) {
		if (!Packet.isCommandPacket(packet)) {
			return;
		}
		if (Command.isIsOnCommand(packet.getData())) {
			//TODO sendPacket(Packet.createResponse(mm.IsOn());
		} else if (Command.isTurnOnCommand(packet.getData())) {
			//TODO sendPacket(Packet.createResponse(mm.turnOn());
		} else if (Command.isTurnOffCommand(packet.getData())) {
			//TODO sendPacket(Packet.createResponse(mm.turnOff());
		} else if (Command.isGetValuesCommand(packet.getData())) {
			//TODO sendPacket(Packet.createResponse(mm.getValues());
		} else if (Command.isGetColorCommand(packet.getData())) {
			//TODO sendPacket(Packet.createResponse(mm.getColor());
		} else {
			return;
		}
	}
	
	public void stopSession() {
		stop = true;
		try {
			sendPacket(Packet.createCommandPacket(Command.goodBye()));
		} catch (IOException e) {
			//Logger.logError(e);
		}
		try {
			socket.close();
		} catch (IOException e) {
			//Logger.logError(e);
		}
	}

}