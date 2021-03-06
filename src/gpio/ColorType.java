package gpio;

/**
 * The type of color.
 * 
 * @author rvemous
 */
public enum ColorType {
    NONE, GREEN, ORANGE, RED, BLUE, RED_FLASHING, GREEN_FLASHING;
                
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
    
    public ColorType getNonFlashing() {
    	return getType(this.toString().split("_")[0]);
    }
}
