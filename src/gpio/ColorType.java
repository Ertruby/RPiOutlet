package gpio;

/**
 * The type of packet.
 * 
 * @author rvemous
 */
public enum ColorType {
    NONE, GREEN, ORANGE, RED, BLUE;
                
    /**
     * Gets the color type belonging to this string.
     * 
     * @param description to use
     * @return the type
     */
    public static ColorType getType(String description) { 
        for (ColorType type : values()) {
        	if (type.toString().equalsIgnoreCase(description)) {
        		return type;
        	}
        }
    	return null;
    }
    
    /**
     * Gets the description of this color type.
     * 
     * @return the description
     */
    public String toString() {
        return this.name();
    }
}
