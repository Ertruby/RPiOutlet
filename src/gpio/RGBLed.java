package gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class RGBLed extends Thread {
	private final GpioController gpio = GpioFactory.getInstance();

	private final GpioPinDigitalOutput greenPin = gpio
			.provisionDigitalOutputPin(RaspiPin.GPIO_00, "green", PinState.LOW);
	private final GpioPinDigitalOutput bluePin = gpio
			.provisionDigitalOutputPin(RaspiPin.GPIO_01, "blue", PinState.LOW);
	private final GpioPinDigitalOutput redPin = gpio.provisionDigitalOutputPin(
			RaspiPin.GPIO_05, "red", PinState.LOW);

	public RGBLed() {
		
	}
	
	public void toggleRed() {
		redPin.toggle();
	}
	
	public void toggleBlue() {
		bluePin.toggle();
	}
	
	public void toggleGreen() {
		greenPin.toggle();
	}

	public void shutdown() {
		redPin.low();
		greenPin.low();
		bluePin.low();
		gpio.shutdown();
	}
}