package nl.utwente.wsc.com.model;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.utwente.wsc.com.model.exception.InvalidPacketException;

/**
 * Packet implementation
 *
 * @author rvemous
 */
public class Packet {

    private PacketHeader header;
    byte[] data;

    public Packet(PacketHeader header, byte[] data) {
        this.header = header;
        this.data = data;
    }
    
    public Packet(byte[] data) throws InvalidPacketException {
        byte[] headerBytes = new byte[PacketHeader.HEADER_LENGTH];
        System.arraycopy(data, 0, headerBytes, 0, headerBytes.length);
        header = new PacketHeader(headerBytes);
        this.data = new byte[data.length - headerBytes.length];
        System.arraycopy(data, headerBytes.length, this.data, 0, this.data.length);
    }  

    /**
     * Creates a test packet.
     */
    public Packet() {
        header = new PacketHeader(4);
        data = new byte[]{84, 69, 83, 84}; // "TEST" in ASCII
    }

    public PacketHeader getHeader() {
        return header;
    }

    public void setHeader(PacketHeader header) {
        this.header = header;
    }
    
    public byte[] getData() {
        return data;
    }

    /**
     * Also updates the packet length
     * @param data 
     */
    public void setData(byte[] data) {
        this.data = data;
        header.setPacketLength(data.length);
    }
    
    public byte[] toSendablePacket() {
        byte[] headerBytes = header.toSendableHeader();
        byte[] allData = new byte[headerBytes.length + data.length];
        System.arraycopy(headerBytes, 0, allData, 0, headerBytes.length);
        System.arraycopy(data, 0, allData, headerBytes.length, data.length);
        return allData;
    }

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder("Packet - ");
            sb.append(header.toString());
            sb.append(", data: ");
            switch (header.getPacketType()) {
                case COMMAND:
                    sb.append(new Command(new String(data)));
                    sb.append(", ");
                    break;
                case DATA:
                    sb.append(Arrays.toString(data));
                    sb.append(", ");
                    break;
                case RESPONSE:
                    sb.append(new String(data));
                    sb.append(", ");
                    break;
            }
            sb.append("raw data: ");
            sb.append(Arrays.toString(data));
            return sb.toString();
        } catch (InvalidPacketException ex) {
            Logger.getLogger(Packet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
         
    public static Packet createCommandPacket(Command command) {
       byte[] commandBytes = command.toSendableCommand().getBytes();
       PacketHeader header = new PacketHeader(PacketType.COMMAND, commandBytes.length);
       return new Packet(header, commandBytes);
    }
    
    public static Packet createDataPacket(byte[] data) {
       PacketHeader header = new PacketHeader(PacketType.DATA, data.length);
       return new Packet(header, data);
    }

    public static Packet createResponse(String response) {
       PacketHeader header = new PacketHeader(PacketType.RESPONSE, response.length());
       return new Packet(header, response.getBytes());
    }
        
    public static boolean isSuccesResponse(Packet packet) {
        if (!isResponsePacket(packet)) {
            return false;
        }
        return Boolean.parseBoolean(new String(packet.getData()));
    }
    
    public static boolean isResponsePacket(Packet packet) {
        if (packet == null) {
            return false;
        }
        return packet.getHeader().getPacketType().equals(PacketType.RESPONSE);
    }
    
    public static boolean isDataPacket(Packet packet) {
        if (packet == null) {
            return false;
        }
        return packet.getHeader().getPacketType().equals(PacketType.DATA);
    }
    
}
