package gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class LampController extends Thread {
	private static final GpioController gpio = GpioFactory.getInstance();
	
	private ColorType currColor;
	
	private static final GpioPinDigitalOutput redPin = gpio.provisionDigitalOutputPin(
			RaspiPin.GPIO_00, "red", PinState.LOW);
	private static final GpioPinDigitalOutput greenPin = gpio
			.provisionDigitalOutputPin(RaspiPin.GPIO_01, "green", PinState.LOW);
	private static final GpioPinDigitalOutput bluePin = gpio
			.provisionDigitalOutputPin(RaspiPin.GPIO_02, "blue", PinState.LOW);
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				gpio.shutdown();			
			}
		}));
	}

	public LampController() {
		currColor = ColorType.NONE;
	}
	
	public ColorType getColor() {
		return currColor;
	} 
	
	public void setGreen() {
		currColor = ColorType.GREEN;
		redPin.low();
		greenPin.high();
		bluePin.low();
	}
	
	public void setOrange() {
		currColor = ColorType.ORANGE;
		redPin.high();
		greenPin.high();
		bluePin.low();
	}
	
	public void setRed() {
		currColor = ColorType.RED;
		redPin.high();
		greenPin.low();
		bluePin.low();
	}
	
	public void setBlue() {
		currColor = ColorType.BLUE;
		redPin.low();
		greenPin.low();
		bluePin.high();
	}
	
	public void setNone() {
		currColor = ColorType.NONE;
		redPin.low();
		greenPin.low();
		bluePin.low();
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

	public void shutdown() {
		currColor = ColorType.NONE;
		redPin.low();
		greenPin.low();
		bluePin.low();
	}
}