package TheCakeRouter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Config {
    public int Port = 33000;
    public int NodePort = 33001;
    public int SendingPort = 33002;
    public int Buffer_size = 65536;
    public InetAddress nodeAddress;
    public InetAddress recipientAddress;

    public Config(){
    	Port = 33000;
        NodePort = 33001;
        SendingPort = 33002;
        Buffer_size = 65536;
        try{
            nodeAddress = InetAddress.getByName("127.0.0.0");
        }catch (UnknownHostException e){
            throw new RuntimeException(e);
        }
       try{
           	recipientAddress = InetAddress.getByName("127.0.0.0");
       }catch (UnknownHostException e){
            throw new RuntimeException(e);
       }
    }	
    	
}
    
    
