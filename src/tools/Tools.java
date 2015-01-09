package tools;

/**
 * Tools holding class.
 *
 * @author rvemous
 */
public class Tools {
    
    /**
     * Waits for the specified time in milliseconds.<br>
     * It can be interrupted.
     * 
     * @param sleepTime time to sleep
     */
    public static void waitForMs(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {}
    }

    /**
     * Waits for the specified time in nanoseconds.<br>
     * It cannot be interrupted.
     * 
     * @param sleepTime time to sleep
     */	
    public static void waitForNs(long sleepTime) {
        long currTime = System.nanoTime();
        while (System.nanoTime() - currTime <= sleepTime);
    }
}
