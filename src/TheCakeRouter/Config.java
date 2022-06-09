package TheCakeRouter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Config {
    public static final int Port = 33000;
    public static final int NodePort = 33001;
    public static final int SendingPort = 33002;
    public static final int Buffer_size = 65536;
    public static final InetAddress nodeAddress;
    static {
        try{
            nodeAddress = InetAddress.getByName("172.20.10.5");
        }catch (UnknownHostException e){
            throw new RuntimeException(e);
        }
    }
    public static final InetAddress recipientAddress;
    static {
        try{
        	recipientAddress = InetAddress.getByName("172.20.10.2");
        }catch (UnknownHostException e){
            throw new RuntimeException(e);
        }
    }
    
}
