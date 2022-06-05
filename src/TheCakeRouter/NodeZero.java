package TheCakeRouter;

public class NodeZero {
	NodeThread NodeThread;
	public NodeZero() {
		NodeThread = new NodeThread();
    	NodeThread.start();
	}
    public static void main(String[] args) throws Exception {        
    	@SuppressWarnings("unused")
		NodeZero node0 = new NodeZero();
    }
}
