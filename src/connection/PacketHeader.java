package nl.utwente.wsc.com.raspi;

import java.util.Arrays;

import nl.utwente.wsc.com.model.exception.InvalidPacketException;

/**
 * PacketHeader implementation for communication with Pi.<br>
 * The header is always 6 bytes long and consists of:<br>
 * <li> 2 bytes: "PI" in ASCII
 * <li> 2 bits:  command/multiple commands/data/response
 * <li> 30 bits: length in bytes of the rest of the packet
 * @author rvemous
 */
public class PacketHeader {

    public static final int HEADER_LENGTH = 7; //bytes
    private static final byte[] START_BYTES = new byte[]{87, 83, 67}; // "WSC" in ASCII
    
    private PacketType packetType;
    private int packetLength;

    public PacketHeader(PacketType packetType, int packetLength) {
        this.packetType = packetType;
        this.packetLength = packetLength;
    }
    
    public PacketHeader(byte[] receivedPacket) throws InvalidPacketException {
        if (receivedPacket.length < HEADER_LENGTH) {
            throw new InvalidPacketException(this.getClass().getName(), 
                "Invalid header size: " + receivedPacket.length + 
                " (must be " + HEADER_LENGTH + ")");
        }
        int readIndex = 0;
        for (; readIndex < START_BYTES.length; readIndex++) {
            if (receivedPacket[readIndex] != START_BYTES[readIndex]) {
                throw new InvalidPacketException(this.getClass().getName(), 
                    "Invalid start bytes: " + receivedPacket[readIndex] + 
                    " != " + START_BYTES[readIndex]);
            }
        } 
        byte sharedByte = receivedPacket[readIndex];
        packetType = PacketType.getType((byte) ((sharedByte & 0xFF) >>> 6));
        packetLength = sharedByte & 0x3F;
        packetLength <<= 8;
        packetLength += receivedPacket[++readIndex] & 0xFF;
        packetLength <<= 8;
        packetLength += receivedPacket[++readIndex] & 0xFF;
        packetLength <<= 8;
        packetLength += receivedPacket[++readIndex] & 0xFF;       
    }    

    /**
     * Generates a test packet header.
     * 
     * @param packetSize size of the packet
     */
    public PacketHeader(int packetSize) {
        this(PacketType.RESPONSE, packetSize);
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
    }

    public int getPacketLength() {
        return packetLength;
    }

    protected void setPacketLength(int packetLength) {
        this.packetLength = packetLength;
    }   
    
    public byte[] toSendableHeader() {
        byte[] headerBytes = new byte[HEADER_LENGTH];
        int writeIndex = 0;
        for (; writeIndex < START_BYTES.length; writeIndex++) {
            headerBytes[writeIndex] = START_BYTES[writeIndex];
        }
        headerBytes[writeIndex] = (byte) ((packetType.toByte() << 6) | 
                (byte) ((packetLength & 0xFFFFFFFF) >>> 24));
        headerBytes[++writeIndex] = (byte) ((0x00FFFFFF & packetLength) >>> 16);
        headerBytes[++writeIndex] = (byte) ((0x0000FFFF & packetLength) >>> 8);
        headerBytes[++writeIndex] = (byte) (0x000000FF & packetLength);
        return headerBytes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Packet header - start bytes: ");
        sb.append(Arrays.toString(START_BYTES));
        sb.append(", packet type: ");
        sb.append(packetType.toString());
        sb.append(", packet length: ");
        sb.append(packetLength);
        sb.append(", raw header: ");
        sb.append(Arrays.toString(toSendableHeader()));
        return sb.toString();
    }
    
    // for testing only   
    public static void main(String[] args) {
        PacketHeader ph = new PacketHeader(0);
        PacketHeader ph2 = new PacketHeader(PacketType.COMMAND, 1000000000);
        System.out.println(ph);
        System.out.println(ph2);
        try {
            System.out.println(new PacketHeader(ph.toSendableHeader()));
        } catch (InvalidPacketException ex) {
            ex.printStackTrace();
        }
        try {
            System.out.println(new PacketHeader(ph2.toSendableHeader()));
        } catch (InvalidPacketException ex) {
            ex.printStackTrace();
        }
    }
    
}
