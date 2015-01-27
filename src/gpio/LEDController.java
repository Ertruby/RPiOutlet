package gpio;

import java.util.HashMap;
import java.util.LinkedHashMap;

import tools.Tools;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Controls the color of the LED on the WSc.
 * 
 * @author Rob
 * @author Mark
 */
public class LEDController {
	
	private static LEDController INSTANCE;
	
	private ColorType currColor;
	private GpioController GPIO;
	
	private GpioPinDigitalOutput pinRed;
	private GpioPinDigitalOutput pinGreen;
	private GpioPinDigitalOutput pinBlue;
	
	private boolean flash = false;
	
	private Thread greenThread;
	private Thread redThread;
	
	public static LEDController getInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		} else {
			return INSTANCE = new LEDController();
		}
	}

	private LEDController() {
		GPIO = GpioFactory.getInstance();
		pinRed = GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_00, "red", PinState.LOW);
		pinGreen = GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_01, "green", PinState.LOW);
		pinBlue = GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_02, "blue", PinState.LOW);
		currColor = ColorType.NONE;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				GPIO.shutdown();			
			}
		}));
	}
	
	public ColorType getColor() {
		return currColor;
	} 
	
	public void setGreen() {
		currColor = ColorType.GREEN;
		pinRed.low();
		pinGreen.high();
		pinBlue.low();
	}
	
	public void setOrange() {
		currColor = ColorType.ORANGE;
		pinRed.high();
		pinGreen.high();
		pinBlue.low();
	}
	
	public void setRed() {
		currColor = ColorType.RED;
		pinRed.high();
		pinGreen.low();
		pinBlue.low();
	}
	
	public void setRedFlashing() {
		currColor = ColorType.RED_FLASHING;
		flash = true;
		redThread = new Thread(new Runnable() {		
			@Override
			public void run() {
				while (flash) {
					pinRed.high();
					pinGreen.low();
					pinBlue.low();
					Tools.waitForMs(500);
					if (!flash) {
						break;
					}
					pinRed.low();
					pinGreen.low();
					pinBlue.low();
					Tools.waitForMs(500);
					if (!flash) {
						break;
					}
				}				
			}
		});
		redThread.setDaemon(true);
		redThread.start();
	}
	
	public void setGreenFlashing() {
		currColor = ColorType.GREEN_FLASHING;
		flash = true;
		greenThread = new Thread(new Runnable() {		
			@Override
			public void run() {
				while (flash) {
					pinRed.low();
					pinGreen.high();
					pinBlue.low();
					Tools.waitForMs(1000);
					if (!flash) {
						break;
					}
					pinRed.low();
					pinGreen.low();
					pinBlue.low();
					Tools.waitForMs(1000);
					if (!flash) {
						break;
					}
				}				
			}
		});
		greenThread.setDaemon(true);
		greenThread.start();
	}
	
	public void setBlue() {
		currColor = ColorType.BLUE;
		pinRed.low();
		pinGreen.low();
		pinBlue.high();
	}
	
	public void setNone() {
		currColor = ColorType.NONE;
		pinRed.low();
		pinGreen.low();
		pinBlue.low();
	}
	
	@SuppressWarnings("incomplete-switch")
	public void setColor(ColorType color) {
		if (color.equals(currColor)) {
			return;
		}
		if (color.equals(ColorType.RED_FLASHING)) {
			if (flash) {
				flash = false;
				greenThread.interrupt();
				Tools.waitForMs(50);
			}
			flash = true;
			setRedFlashing();
			return;
		}
		if (color.equals(ColorType.GREEN_FLASHING)) {
			if (flash) {
				flash = false;
				redThread.interrupt();
				Tools.waitForMs(50);
			}
			flash = true;
			setGreenFlashing();
			return;
		}
		flash = false;
		switch(color) {
		case NONE:
			setNone();
			break;
		case BLUE:
			setBlue();
			break;
		case GREEN:
			setGreen();
			break;
		case ORANGE:
			setOrange();
			break;
		case RED:
			setRed();
			break;	
		}
	}
}