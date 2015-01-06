package nl.utwente.wsc.utils;


import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * A timer which can be used to check whether a certain time has elapsed.
 * 
 * @author rvemous
 */
public class Timer {

	private long relExpireTime; // time from start time to expire
	private long startTime;
	private boolean started;

	/**
	 * Creates a new timer.
	 * 
	 * @param expireTime time in ms after <code>start()</code> until it 
	 * expires or time in ms after Epoch until it expires
	 * @param start whether to immediately start the timer after the call
	 */
	public Timer(long expireTime, boolean start) {
		relExpireTime = expireTime;
		if (start) {
			start();
		}
	}
	
	/**
	 * Creates a new timer which is not already started.
	 * 
	 * @param expireTime time in ms after <code>start()</code> until it 
	 * expires or time in ms after Epoch until it expires0
	 */
	public Timer(long expireTime) {
		this(expireTime, false);
	}

	/**
	 * Gets the time in ms after <code>start()</code> until it expires.
	 * 
	 * @return the expire time
	 */
	public long getRelExpireTime() {
		return relExpireTime;
	}
	
	/**
	 * Gets the absolute time in ms after Epoch when it expires if 
	 * <code>start()</code> would be called at this moment.
	 * 
	 * @return the absolute expire time
	 */
	public long getAbsExpireTime() {
		return System.currentTimeMillis() + relExpireTime;
	}	
	
	/**
	 * Sets the time in ms after <code>start()</code> until it expires.
	 * 
	 * @param expireTime the expire time
	 */ 
	void setRelExpireTime(long expireTime) {
		relExpireTime = expireTime;
	}

	/**
	 * Sets the absolute time in ms after Epoch when it expires if 
	 * <code>start()</code> would be called at this moment.
	 * 
	 * @param expireTime the absolute expire time
	 */
	public void setAbsExpireTime(long expireTime) {
		relExpireTime = System.currentTimeMillis() + expireTime;
	}
	
	/**
	 * Starts the timer with the current expire time.
	 */
	public synchronized void start() {
		if (started) {
			return;
		}
		startTime = System.currentTimeMillis();
		started = true;
	}
	
	/**
	 * Restarts the timer with the current expire time.
	 */
	public synchronized void restart() {
		started = false;
		start();
	}
	
	/**
	 * Gets whether the timer has expired.
	 * 
	 * @return whether the timer has expired
	 */
	public boolean hasExpired() {
		return !started || timeLeft() <= 0;
	}
	
	/**
	 * Gets the time in ms left until the timer expires.<br>
	 * Returns -1 when the timer is not started yet.
	 * 
	 * @return the time left
	 */
	public long timeLeft() {
		if (!started) {
			return -1;
		} else {
			return relExpireTime - (System.currentTimeMillis() - startTime);
		}
	}
	
	/**
	 * Adds the time to the timer, whether it is running or not.
	 * 
	 * @param timeToAdd time to add in ms
	 */
	public void addTime(long timeToAdd) {
		relExpireTime += timeToAdd;
	}
	
	/**
	 * Stops the currently running timer.
	 */
	public void stop() {
		started = false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Timer - rel expire time:");
		sb.append(relExpireTime);
		sb.append(", start time:");
		sb.append(startTime);
		sb.append(", started:");
		sb.append(started);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		// read picture
		File file = new File("pf.png");
		File fileSmall = new File("pf-small.png");
		DataInputStream fis;
		byte[] pic = new byte[(int) file.length()];
		try {
			fis = new DataInputStream(new FileInputStream(file));
			fis.readFully(pic);
		} catch (IOException e) {}
		System.out.println("Size before: " + file.length());
		// compress and write picture
        OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(fileSmall));
		} catch (FileNotFoundException e1) {}
        Deflater def = new Deflater(Deflater.BEST_SPEED);
        DeflaterOutputStream dout = new DeflaterOutputStream(out, def);
        try {
			dout.write(pic);
	        dout.close();
	        out.close();
		} catch (IOException e) {}     
		System.out.println("Size after: " + fileSmall.length());
		
	}
}
