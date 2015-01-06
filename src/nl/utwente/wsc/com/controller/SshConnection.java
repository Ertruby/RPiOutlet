package nl.utwente.wsc.com.controller;

import java.net.InetAddress;
import java.util.Properties;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Packet;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * Sets up and manages the SSH connection.
 *
 * @author rvemous
 */
public class SshConnection {
    
    private static final JSch JSCH = new JSch();
    
    private Session session;

    public SshConnection(InetAddress address, int portNr, String username, char[] password, int timeout) throws JSchException {
        session = JSCH.getSession(username, address.getHostAddress(), portNr);
        session.setPassword(new String(password));
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        localUserInfo lui = new localUserInfo();
        session.setUserInfo(lui);
        session.connect(timeout);
        try {
            session.write(new Packet(new Buffer("Stuff".getBytes())));
        } catch (Exception ex) {
            System.err.println("Problemo");
            ex.printStackTrace();
        }
    }
    
    private class localUserInfo implements UserInfo {
        String passwd;
        @Override
        public String getPassword(){ return passwd; }
        @Override
        public boolean promptYesNo(String str){return true;}
        @Override
        public String getPassphrase(){ return null; }
        @Override
        public boolean promptPassphrase(String message){return true; }
        @Override
        public boolean promptPassword(String message){return true;}
        @Override
        public void showMessage(String message){}
    }
    
    public Channel getChannel(String type, int timeout) {
        Channel channel;
        try {
            channel = session.openChannel(type);
            channel.connect(timeout);
        } catch (JSchException ex) {
            return null;
        }      
        return channel;
    } 
    
    public boolean isConnected() {
        return session.isConnected();
    }
    
    public void connect(int timeout) throws JSchException {
        session.connect(timeout);
    }


    public void disconnect() {
        session.disconnect();
    }
    
}
