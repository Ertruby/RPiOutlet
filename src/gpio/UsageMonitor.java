package gpio;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import main.MainManager;

/**
 * Gets the usage from the Power monitor and controls the color of LED accordingly (via the MainManager).
 * @author Rob
 *
 */
public class UsageMonitor {
	
	private static final int MAX_SIZE = 5;
	
	private static final double ORANGE_THRESHOLD_FACTOR = 1.10; // 10% more than reference
	private static final double RED_THRESHOLD_FACTOR = 1.25; // 25% more than reference
	private static final double SILENT_THRESHOLD_FACTOR = 0.10; // 90% less than reference (device disconnected maybe)
	
	private MainManager mm;
	
	private Queue<UsageElement> usageHistory;
	private UsageElement referenceElement;
	private long totalUsageTime;	
	private double totalUsagePower;
	
	public UsageMonitor(MainManager mm) {
		this.mm = mm;
		usageHistory = new LinkedBlockingQueue<UsageElement>();
		totalUsageTime = 0;
		totalUsagePower = 0;
		updateColor();
	}
	
	public void addPowerUsage(long timeInterval, double powerUsage) {
		UsageElement newElement = new UsageElement(timeInterval, powerUsage);
		if (usageHistory.isEmpty()) {
			referenceElement = newElement;
		}
		usageHistory.add(newElement);
		totalUsageTime += newElement.getTimeInterval();
		totalUsagePower += newElement.getPowerUsage();
		if (usageHistory.size() > MAX_SIZE) {
			usageHistory.remove();
		}
		updateColor();
	}
	
	public void resetHistory() {
		usageHistory.clear();
		totalUsageTime = 0;
		totalUsagePower = 0;
	}
	
	private void updateColor() {
		if (usageHistory.isEmpty()) {
			mm.setMonitorColor(ColorType.GREEN_FLASHING);
			return;
		}
		double totalPower = 0; // in watt/hour
		Iterator<UsageElement> iterator = usageHistory.iterator();
		while (iterator.hasNext()) {
			totalPower += iterator.next().getPowerUsage();
		}
		double average = totalPower / usageHistory.size();
		double usageToRefUsageFactor = average / referenceElement.getPowerUsage();
		if (usageToRefUsageFactor >= RED_THRESHOLD_FACTOR) {
			// way to much usage
			mm.setMonitorColor(ColorType.RED);
		} else if (usageToRefUsageFactor >= ORANGE_THRESHOLD_FACTOR)  {
			// little too much usage
			mm.setMonitorColor(ColorType.ORANGE);
		} else if (usageToRefUsageFactor <= SILENT_THRESHOLD_FACTOR) {
			// very little usage (device probably disconnected, but the charger not)
			mm.setMonitorColor(ColorType.RED_FLASHING);			
		} else {
			// average usage (or less) 
			mm.setMonitorColor(ColorType.GREEN);
		}
	}

	private class UsageElement {
		
		private long timeInterval;
		private double powerUsage;
		
		public UsageElement(long timeInterval, double powerUsage) {
			this.timeInterval = timeInterval;
			this.powerUsage = powerUsage;
		}
		
		public long getTimeInterval() {
			return timeInterval;
		}
		
		public double getPowerUsage() {
			return powerUsage;
		}
	}
}
