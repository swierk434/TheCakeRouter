package TheCakeRouter;

public class Client{
	NodeThread nodeThread;
	UDPThread udpThread;
	
	public Client() {
		udpThread = new UDPThread(this);
		udpThread.start();
	}
    public static void main(String[] args) throws Exception {        
    	@SuppressWarnings("unused")
		Client client = new Client();
    }
}