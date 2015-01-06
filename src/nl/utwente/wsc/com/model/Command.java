package nl.utwente.wsc.com.model;

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
     * Logs in a user.
     * 
     * @param username of the user
     * @param password of the user
     * @return the command
     */    
    public static Command loginUserCommand(String username, String password) {
        return new Command("login", new String[]{username, password});
    }
    
    /**
     * Logs out a user.
     * 
     * @return the command
     */    
    public static Command logoutUserCommand() {
        return new Command("logout", (String)null);
    }
    
    /**
     * Creates a new user.
     * 
     * @param username of the user
     * @param password of the user
     * @return the command
     */    
    public static Command createUserCommand(String username, String password) {
        return new Command("create", new String[]{username, password});
    }
    
    /**
     * Checks the existence of the user.
     * 
     * @param username of the user
     * @return the command
     */
    public static Command checkUserCommand(String username) {
        return new Command("checkUser", username); 
    }
    
    /**
     * Deletes a user.
     * 
     * @return the command
     */    
    public static Command deleteUserCommand() {
        return new Command("deleteUser", (String)null); 
    }
    
    /**
     * Checks whether a file exists.
     * 
     * @param id of file
     * @return the command
     */    
    public static Command checkFileCommand(long id) {
        return new Command("checkFile", id + ""); 
    }  
 
    /**
     * Donwloads a file.
     * 
     * @param id of file
     * @return the command
     */    
    public static Command downloadFileCommand(long id) {
        return new Command("download", id + ""); 
    } 
    
    /**
     * Asks whether it can upload a file.
     * 
     * @param fileSize the size of the file
     * @return the command
     */    
    public static Command uploadFileCommand(int fileSize) {
        return new Command("upload", fileSize + ""); 
    } 
     
    /**
     * Deletes a file.
     * 
     * @param id of file
     * @return the command
     */    
    public static Command deleteFileCommand(long id) {
        return new Command("deleteFile", id + ""); 
    }    
    /**
     * Test command.
     * 
     * @return the command
     */    
    public static Command stopServerCommand() {
        return new Command("stop", "reallyactuallystop"); 
    }
    
    /**
     * Test command.
     * 
     * @return the command
     */    
    public static Command testCommand() {
        return new Command("deleteUser", (String)null); 
    }
    
    
}
