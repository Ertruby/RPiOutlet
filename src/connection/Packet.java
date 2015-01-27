package connection;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import connection.exception.InvalidPacketException;

/**
 * A packet which contains a header and data.
 *
 * @author rvemous
 */
public class Packet {

    private PacketHeader header;
    byte[] data;

    /**
     * Creates a new packet.
     * 
     * @param header the header to use
     * @param data the data to use
     */
    public Packet(PacketHeader header, byte[] data) {
        this.header = header;
        this.data = data;
    }
    
    /**
     * Creates a packet from (received) packet data bytes.
     * 
     * @param data the data to use
     * @throws InvalidPacketException when the data contains an invalid header
     */
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

    /**
     * Gets the header of the packet.
     * 
     * @return the header
     */
    public PacketHeader getHeader() {
        return header;
    }

    /**
     * Sets the header of the packet.
     * 
     * @param header the header to use
     */
    public void setHeader(PacketHeader header) {
        this.header = header;
    }
    
    /**
     * Gets the data of the packet.
     * 
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the data of the packet.<br>
     * It also updates the packet length field of the header.
     * 
     * @param data the data to use
     */
    public void setData(byte[] data) {
        this.data = data;
        header.setPacketLength(data.length);
    }
    
    /**
     * Puts the header and data of the packet into a (sendable) array of bytes.
     * 
     * @return the array of bytes
     */
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
       
    /**
     * Creates a packet which is a command.
     * 
     * @param command the command to use
     * @return the packet
     */
    public static Packet createCommandPacket(Command command) {
       byte[] commandBytes = command.toSendableCommand().getBytes();
       PacketHeader header = new PacketHeader(PacketType.COMMAND, commandBytes.length);
       return new Packet(header, commandBytes);
    }
    
    /**
     * Creates a packet which is a command and contains only a string.
     * 
     * @param command the string contained in the command
     * @return the packet
     */    
    public static Packet createCommandStringPacket(String command) {
        byte[] commandBytes = command.getBytes();
        PacketHeader header = new PacketHeader(PacketType.COMMAND, commandBytes.length);
        return new Packet(header, commandBytes);
    }
    
    /**
     * Creates a packet which is a data packet.
     * 
     * @param data the data to use
     * @return the packet
     */
    public static Packet createDataPacket(byte[] data) {
       PacketHeader header = new PacketHeader(PacketType.DATA, data.length);
       return new Packet(header, data);
    }

    /**
     * Creates a packet which is a response.
     * 
     * @param response the response string to use
     * @return the packet
     */
    public static Packet createResponse(String response) {
       PacketHeader header = new PacketHeader(PacketType.RESPONSE, response.length());
       return new Packet(header, response.getBytes());
    }
    
    /**
     * Checks whether the packet is a command packet.
     * 
     * @param packet the packet to validate
     * @return whether the packet is a command packet
     */
    public static boolean isCommandPacket(Packet packet) {
        if (packet == null) {
            return false;
        }
        return packet.getHeader().getPacketType().equals(PacketType.COMMAND);
    }

    /**
     * Checks whether the packet is a success (contains "true") response packet.
     * 
     * @param packet the packet to validate
     * @return whether the packet is a success response packet
     */
    public static boolean isSuccesResponse(Packet packet) {
        if (!isResponsePacket(packet)) {
            return false;
        }
        return Boolean.parseBoolean(new String(packet.getData()));
    }
 
    /**
     * Checks whether the packet is a response packet.
     * 
     * @param packet the packet to validate
     * @return whether the packet is a response packet
     */
    public static boolean isResponsePacket(Packet packet) {
        if (packet == null) {
            return false;
        }
        return packet.getHeader().getPacketType().equals(PacketType.RESPONSE);
    }

    /**
     * Checks whether the packet is a data packet.
     * 
     * @param packet the packet to validate
     * @return whether the packet is a data packet
     */
    public static boolean isDataPacket(Packet packet) {
        if (packet == null) {
            return false;
        }
        return packet.getHeader().getPacketType().equals(PacketType.DATA);
    }
    
}
