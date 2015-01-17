package gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class LampController extends Thread {
	private final GpioController gpio = GpioFactory.getInstance();

	private final GpioPinDigitalOutput greenPin = gpio
			.provisionDigitalOutputPin(RaspiPin.GPIO_00, "green", PinState.LOW);
	private final GpioPinDigitalOutput bluePin = gpio
			.provisionDigitalOutputPin(RaspiPin.GPIO_01, "blue", PinState.LOW);
	private final GpioPinDigitalOutput redPin = gpio.provisionDigitalOutputPin(
			RaspiPin.GPIO_05, "red", PinState.LOW);

	public LampController() {}
	
	public ColorType getColor() {
		ColorType toReturn = ColorType.NONE;
//		redPin.isHigh();
//		greenPin.isHigh();
		if (redPin.isHigh() && greenPin.isLow()) {
			toReturn = ColorType.RED;
		} else if (greenPin.isHigh() && redPin.isLow()) {
			toReturn = ColorType.GREEN;
		} else if (greenPin.isHigh() && redPin.isHigh()) {
			toReturn = ColorType.ORANGE;
		}
		return toReturn;
	} 
	
	public void setGreen() {
		redPin.low();
		greenPin.high();
		bluePin.low();
	}
	
	public void setOrange() {
		redPin.high();
		greenPin.high();
		bluePin.low();
	}
	
	public void setRed() {
		redPin.high();
		greenPin.low();
		bluePin.low();
	}

	public void shutdown() {
		redPin.low();
		greenPin.low();
		bluePin.low();
		gpio.shutdown();
	}
}