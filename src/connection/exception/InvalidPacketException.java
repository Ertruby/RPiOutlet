package connection.exception;

/**
 * Used to signal a packets' contents are malformed or 
 * invalid.<br>
 *
 * @author rvemous
 */
public class InvalidPacketException extends Exception {
  
    /**
	 * 
	 */
	private static final long serialVersionUID = -5839021325843653898L;

	/**
     * Create a new packet exception.
     *
     * @param processId the name of the process that throws the exception
     * @param cause of the exception
     */
    public InvalidPacketException(String processId, String cause) {
        super(processId + " - " + cause);
    }
   
    /**
     * Create a new packet exception.
     */
    public InvalidPacketException() {
        super();
    }
    
}
