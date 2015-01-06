package nl.utwente.wsc.com.raspi;

/**
 * The type of packet.
 * 
 * @author rvemous
 */
public enum PacketType {
    COMMAND, DATA, RESPONSE;
                
    /**
     * Gets the packet type belonging to this byte.
     * 
     * @param codeByte byte to use
     * @return the type
     */
    public static PacketType getType(byte codeByte) {
        if (codeByte < 0 || codeByte > 3) {
            return null;
        }
        return values()[codeByte];
    }
    
    /**
     * Gets the byte code belonging to this packet type.
     * 
     * @return the code
     */
    public byte toByte() {
        return (byte) ordinal();
    }
}
