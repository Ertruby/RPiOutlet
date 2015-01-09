package tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class Logger implements Thread.UncaughtExceptionHandler {

	private static final String NEW_LINE = System.lineSeparator();

	@SuppressWarnings("unused")
	private static final File LOG;
	private static final FileWriter WRITER;
	private static final long START_TIMESTAMP;

	static {
		File l = null;
		FileWriter fw = null;

		try {
			l = new File("piCloud.log");
			l.createNewFile();
			fw = new FileWriter(l);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		LOG = l;
		WRITER = fw;
		START_TIMESTAMP = System.currentTimeMillis();
		Logger logger = new Logger();
		Thread.setDefaultUncaughtExceptionHandler(logger);
		Thread.currentThread().setUncaughtExceptionHandler(logger);
	}

	public static void init() {
		log("Logger Initialized at " + START_TIMESTAMP);
	}

	public static synchronized void log(String message) {
		String log = getPrefix() + message + NEW_LINE;
		System.out.print(log);
		try {
			WRITER.write(log);
			WRITER.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized void logError(String message) {
		String log = NEW_LINE + ">>>" + getPrefix() + " ERROR:" + message
				+ "<<<" + NEW_LINE + NEW_LINE;
		System.err.print(log);
		try {
			WRITER.write(log);
			WRITER.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized void logError(Throwable t) {
		logError(getStackTraceString(t));
	}

	private static String getPrefix() {
		return "[" + Thread.currentThread().getName() + "+" + currentTime()
				+ "]\t\t\t\t\t";
	}

	private static String getStackTraceString(Throwable e) {
		if (e == null)
			return "[unknown exception]";
		String res = e.toString() + System.lineSeparator();
		for (StackTraceElement ste : e.getStackTrace())
			res += "at " + ste.toString() + System.lineSeparator();
		return res;
	}

	public static double currentTime() {
		return (double) (System.currentTimeMillis() - START_TIMESTAMP) / 1000;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logError("UNCAUGHT THROWABLE: " + getStackTraceString(e));
		System.err.println("FATAL ERROR:");
		if (e != null)
			e.printStackTrace();
		else
			System.err.println("<Unable to print error message>");
		System.exit(1);
	}

	private Logger() {
	}
}
