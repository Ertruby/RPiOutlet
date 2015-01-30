package connection;

import gpio.ColorType;
import connection.exception.InvalidPacketException;


/**
 * A command with possible arguments.
 *
 * @author rvemous
 */
public class Command {

    private String command;
    private String[] arguments;

    public Command(String command, String... arguments) {
        this.command = command;
        if (arguments != null && arguments.length != 0 && arguments[0] != null && arguments[0] != "") {
            this.arguments = arguments;
        }
    }
    
    public Command(String fromPacketData) throws InvalidPacketException {
        String[] commAndArgs = fromPacketData.split("-");
        if (commAndArgs.length == 0) {
            throw new InvalidPacketException(this.getClass().getName(), 
                    "Command packet contains no command");
        }
        command = commAndArgs[0];
        if (commAndArgs.length > 1) {
           arguments = new String[commAndArgs.length - 1];
           System.arraycopy(commAndArgs, 1, arguments, 0, arguments.length);
        }
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
    
    public boolean hasArguments() {
        return arguments != null;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    } 
    
    public String toSendableCommand() {
        StringBuilder sb = new StringBuilder(command);
        if (!hasArguments()) {
            return sb.toString(); 
        }
        for (String argument : arguments) {
            sb.append("-");
            sb.append(argument);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(command);
        if (!hasArguments()) {
            return sb.toString(); 
        }
        for (String argument : arguments) {
            sb.append(" ");
            sb.append(argument);
        }
        return sb.toString(); 
    }
    
    public static boolean isIsOnCommand(Command comm) {
    	return comm.getCommand().equalsIgnoreCase("isOn");
    }
    
    public static boolean isTurnOnCommand(Command comm) {
    	return comm.getCommand().equalsIgnoreCase("turnOn");
    }
    
    public static boolean isTurnOffCommand(Command comm) {
    	return comm.getCommand().equalsIgnoreCase("turnOff");
    }
    
    public static boolean isGetValuesCommand(Command comm) {
    	return comm.getCommand().equalsIgnoreCase("getValues");
    }
    
    public static boolean isGetColorCommand(Command comm) {
    	return comm.getCommand().equalsIgnoreCase("getColor");
    }
    
    /**
     * Turns on/off the wall socket.
     * 
     * @return the command
     */      
    public static Command setState(boolean turnOn) {
        return new Command("turn" + (turnOn ? "On" : "Off"), "");
    }
    
    /**
     * Sets the color values.
     * 
     * @return the command
     */    
    public static Command setColor(ColorType type) {
        return new Command("setColor", type.toString());
    }
    
    /**
     * Sets the power usage value.
     * 
     * @return the command
     */    
    public static Command addValue(long time, double value) {
        return new Command("addValue", new String[]{time+"", value+""});
    }
       
    
}
