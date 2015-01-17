package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    
	public static synchronized String waitForInput(InputStream stream) {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));	
		String line = null;
		try {
			while ((line = in.readLine()) == null) {}
		} catch (IOException e) {}
		return line;
	}
}
