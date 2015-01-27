package gpio;

import java.util.HashMap;
import java.util.LinkedHashMap;

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
	
	public void setColor(ColorType color) {
		currColor = color;
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