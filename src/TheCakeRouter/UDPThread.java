package TheCakeRouter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;

public class UDPThread extends Thread {
	String recivedMessage, recivedTokens[], response, message, clientinput, clientTokens[];
	DatagramSocket datagramSocket;
	DatagramPacket receivedPacket, responsePacket;
	InetAddress previousAddress, nextAddress, thisAddress, tmpAddress;
	Integer previousPort, sendingPort;
	byte[] byteMessage, byteResponse;
	Vector<InetAddress> nodeVector;
	Client clientPointer;
	Scanner scanIn;
	
	public UDPThread(Client client) {
		super();
		clientPointer = client;
		byteMessage = new byte[Config.Buffer_size];
		byteResponse = new byte[Config.Buffer_size];
		response = "";
		previousPort =  null;
		previousAddress = null;
		clientinput = "";
		nodeVector = new Vector<InetAddress>(0);
		scanIn = new Scanner(System.in);
		try {
			thisAddress = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			datagramSocket = new DatagramSocket(Config.Port);
		} catch (SocketException e2) {
			System.out.println("[UDP]Opening Socket Failed");
		}
		try {
			datagramSocket.setSoTimeout(0);
		} catch (SocketException e) {
			System.out.println("[UDP]Changing Timeout Failed");
		}	
		receivedPacket = new DatagramPacket(new byte[Config.Buffer_size], Config.Buffer_size);
	}
	public String mergeMessage(Vector<InetAddress> vector, int[] nodeIndex, String gap, String message) {
		String out = "";
		nextAddress = vector.get(0);
		for(int index = 1; index < nodeIndex.length; index++) {
			try {
			out += vector.get(index).toString();
			out += gap;
			} catch (Exception e) {
				System.out.print("[UDP]Failed to build message");
			}
		}
		out += message;
		return out;
	}
	public int[] stringToIntArray(String[] tab) {
		int out[] = null;
		try {
			out = new int[tab.length];
			for (int i = 0; i < tab.length; i++) {
				out[i] = Integer.parseInt(tab[i]);
			}
		} catch (Exception e) {
			System.out.println("[UDP]Wrong input format");
		}
		return out;
	}
	public void updateNodes() {
		nodeVector = clientPointer.nodeThread.nodeVector;
	}
	public void printNodes() {
		for(int i = 0; i < nodeVector.size(); i++) {
			System.out.printf("[UDP]Node nr: %2.0d ip: %s%n", i,nodeVector.get(i).toString().split("/")[1]);
		}
	}
	public void sendResponse(byte[] byteResponsetmp, int len, InetAddress address, int port, String error_code) {

		byteResponse = byteResponsetmp;
		responsePacket = new DatagramPacket(byteResponse, len, address, port);
         try {
				datagramSocket.send(responsePacket);
			} catch (IOException e) {
				System.out.println(error_code);
			}
	}
	public void sendResponse(String stringResponse, InetAddress address, int port, String error_code) {
		try {
			byteResponse = stringResponse.getBytes("utf8");
		} catch (UnsupportedEncodingException e1) {
			System.out.println("[UDP]string yo byteresponse Failed");
		}
		responsePacket = new DatagramPacket(byteResponse, byteResponse.length, address, port); 
         try {
				datagramSocket.send(responsePacket);
			} catch (IOException e) {
				System.out.println(error_code);
				System.exit(1);
			}
	}
	public void recievePacket() {
		try {
			datagramSocket.receive(receivedPacket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public Vector<InetAddress> arrayToNodeVector(String[] tab, int startIndex, int endIndex) {
		Vector<InetAddress> outVector = new Vector<InetAddress>(0);
		for(Integer i = startIndex; i <= endIndex; i++) {
			try {
				tmpAddress = InetAddress.getByName(tab[i].split("/")[1]);
				outVector.add(tmpAddress);
			} catch (UnknownHostException e) {
				System.out.println("[UDP]Failed to add address to vector at index: "+i.toString());
			}
			
		}
		return outVector;
	}
	public void recodePacket() {
		int length = receivedPacket.getLength();
    	try {
    		recivedMessage = new String(receivedPacket.getData(), 0, length, "utf8");
    	} catch (UnsupportedEncodingException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	recivedTokens = recivedMessage.split(" ");
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
			System.out.println("[UDP]UDPThread Started");
	        sendResponse("#JOIN_REQUEST#", Config.nodeAddress, Config.NodePort, "[UDP]Error#JOIN_REQUEST# - Client");
	        System.out.println("[UDP]Join request sent");
	        recievePacket();
	        recodePacket();  
        	System.out.println("[UDP]Recived message:"+recivedMessage);
	       if(recivedTokens[0].equals("#REQUEST_ACCEPTED#")) {
	    	   System.out.println("[UDP]Request accepted");
	    	   nodeVector = arrayToNodeVector(recivedTokens, 1, recivedTokens.length-1);
	       }
	       clientPointer.nodeThread = new NodeThread(nodeVector);
	       clientPointer.nodeThread.start();
	       
			while (clientinput != "EXIT"){
				System.out.println("[UDP]type 'SEND' to send message; type 'EXIT' to exit");
				clientinput = scanIn.nextLine();
			    switch(clientinput) {
			    	case "EXIT":
            			response = "#EXIT# " + thisAddress.toString();
            			for(int n = 0; n < nodeVector.size(); n++) {
            				if(nodeVector.get(n) != thisAddress) {
            					sendResponse(response, nodeVector.get(n), Config.NodePort, "[UDP]error#ADD_NODE#");
            				}
            			}
            			clientPointer.nodeThread.stop();
			    		break;
			     	case "SEND":
			     		message = "#SEND# ";
			     		System.out.println("[UDP]Here is nodes list, type nodes' nr separated with spaces to set route"); // 7 9 3 4
			     		updateNodes();
			     		printNodes();
			     		clientinput = scanIn.nextLine();
			     		clientTokens = clientinput.split(" ");
			     		message += mergeMessage(nodeVector,stringToIntArray(clientTokens)," ", "[UDP]Failed to merge message (route)");
			     		message += Config.recipientAddress.toString();
			     		message += " ";
			     		System.out.println("[UDP]Type messsage content");
			     		clientinput = scanIn.nextLine();
			     		sendResponse(message, nextAddress, Config.NodePort, "[UDP]Ry¿");
			     		
			     		recievePacket();
			     		recodePacket();
			     		
			     		System.out.println("[UDP]You recived reply:");
			     		System.out.println("[UDP]"+recivedMessage);
			     		
			     		break;
			     }
	       }
			System.out.println("[UDP]UDPThread Finished");
	    }
}
