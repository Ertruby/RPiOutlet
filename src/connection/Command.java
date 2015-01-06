package nl.utwente.wsc.com.raspi;

import nl.utwente.wsc.com.model.exception.InvalidPacketException;

/**
 * A command with possible arguments
 *
 * @author rvemous
 */
public class Command {

    private String command;
    private String[] arguments;

    public Command(String command, String... arguments) {
        this.command = command;
        if (arguments != null && arguments.length != 0 && arguments[0] != null) {
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

    /**
     * Gets the power usage values.
     * 
     * @return the command
     */    
    public static Command goodBye() {
        return new Command("goodBye", "");
    }
    
    public static boolean isIsOnCommand(byte[] data) {
    	return new String(data).equalsIgnoreCase("isOn");
    }
    
    public static boolean isTurnOnCommand(byte[] data) {
    	return new String(data).equalsIgnoreCase("turnOn");
    }
    
    public static boolean isTurnOffCommand(byte[] data) {
    	return new String(data).equalsIgnoreCase("turnOff");
    }
    
    public static boolean isGetValuesCommand(byte[] data) {
    	return new String(data).equalsIgnoreCase("getValues");
    }
    
    public static boolean isGetColorCommand(byte[] data) {
    	return new String(data).equalsIgnoreCase("getColor");
    }
    
}
