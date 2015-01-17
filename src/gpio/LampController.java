package gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class LampController extends Thread {
	private final GpioController gpio = GpioFactory.getInstance();
	
	private ColorType currColor;

	private final GpioPinDigitalOutput greenPin = gpio
			.provisionDigitalOutputPin(RaspiPin.GPIO_00, "green", PinState.LOW);
	private final GpioPinDigitalOutput bluePin = gpio
			.provisionDigitalOutputPin(RaspiPin.GPIO_01, "blue", PinState.LOW);
	private final GpioPinDigitalOutput redPin = gpio.provisionDigitalOutputPin(
			RaspiPin.GPIO_05, "red", PinState.LOW);

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

	public void shutdown() {
		currColor = ColorType.NONE;
		redPin.low();
		greenPin.low();
		bluePin.low();
		gpio.shutdown();
	}
}