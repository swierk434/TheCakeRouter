package TheCakeRouter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

public class NodeThread extends Thread{
	String recivedMessage, recivedTokens[], response;
	DatagramSocket datagramSocket;
	DatagramPacket receivedPacket, responsePacket;
	InetAddress previousAddress, nextAddress, thisAddress, tmpAddress, sendBackAddress;
	Integer previousPort, sendingPort;
	byte[] byteMessage, byteResponse;
	Vector<InetAddress> nodeVector;
	
	public NodeThread() {
		super();
		byteMessage = new byte[Config.Buffer_size];
		byteResponse = new byte[Config.Buffer_size];
		response = "";
		previousPort =  null;
		previousAddress = null;
		sendBackAddress = null;
		try {
			thisAddress = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			datagramSocket = new DatagramSocket(Config.NodePort);
		} catch (SocketException e2) {
			System.out.println("[Node]Opening Socket Failed");
		}
		try {
			datagramSocket.setSoTimeout(0);
		} catch (SocketException e) {
			System.out.println("[Node]Changing Timeout Failed");
		}
		nodeVector = new Vector<InetAddress>(0);
		nodeVector.add(thisAddress);  
		receivedPacket = new DatagramPacket(new byte[Config.Buffer_size], Config.Buffer_size);
		
	}
	public NodeThread(Vector<InetAddress> vector) {
		super();
		byteMessage = new byte[Config.Buffer_size];
		byteResponse = new byte[Config.Buffer_size];
		response = "";
		previousPort =  null;
		previousAddress = null;
		sendBackAddress = null;
		try {
			thisAddress = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			datagramSocket = new DatagramSocket(Config.NodePort);
		} catch (SocketException e2) {
			System.out.println("[Node]Opening Socket Failed");
		}
		try {
			datagramSocket.setSoTimeout(0);
		} catch (SocketException e) {
			System.out.println("[Node]Changing Timeout Failed");
		}
		nodeVector = vector;
		receivedPacket = new DatagramPacket(new byte[Config.Buffer_size], Config.Buffer_size);
	}
	public String merge(String[] tab, int firstIndex, int lastIndex, String gap) {
		String out = "";
		for(int i = firstIndex; i <= lastIndex; i++) {
			out += tab[i];
			out += gap;
		}
		return out;
	}
	public String mergeVector(Vector<InetAddress> vector, int firstIndex, int lastIndex, String gap) {
		String out = "";
		for(int i = firstIndex; i <= lastIndex; i++) {
			out += vector.get(i).toString();
			out += gap;
		}
		return out;
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
			System.out.println("[Node]string yo byteresponse Failed");
		}
		responsePacket = new DatagramPacket(byteResponse, byteResponse.length, address, port); 
         try {
				datagramSocket.send(responsePacket);
			} catch (IOException e) {
				System.out.println(error_code);
			}
	}
	public void receievePacket() {
		try {
			datagramSocket.receive(receivedPacket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
	
	public void run() {
		System.out.println("[Node]Node is running; this address is:");
		System.out.println("[Node]"+thisAddress);
	        while (true) {
	        	System.out.println("[Node]Nodes' adresses in network");
	        	System.out.println("[Node]"+nodeVector);
	            
	        	receievePacket();
	            
	            System.out.println("[Node]PacketRecived");
	            
	            if(receivedPacket.getAddress() == sendBackAddress) { // sprawcz czy wiadomoœæ wraca po wêz³ach 
	            	byteResponse = receivedPacket.getData();
	            	sendResponse(byteResponse,receivedPacket.getLength() , previousAddress, previousPort, "[Node]error'NO_TAG' (sending back)");	            	
	            	previousAddress = null;
	            	previousPort = null;
	            }
	            else {
	            	recodePacket();
	            	System.out.println("[Node]"+recivedMessage);
	            	
	            	previousAddress = receivedPacket.getAddress(); // Port i host który wys³a³ nam zapytanie
	            	previousPort = receivedPacket.getPort();
	            	
	            	//System.out.println(recivedTokens[]);
		            if(recivedTokens.length >= 1) {
		        	   switch (recivedTokens[0]){
		            		case "#SEND#":
		            			sendBackAddress = previousAddress;
		            			sendingPort = Config.SendingPort;
		            			try {
		            				nextAddress = InetAddress.getByName(recivedTokens[1].split("/")[1]);
		            				if(nodeVector.contains(nextAddress) == true) {
			            				response = "#SEND# ";
			            				sendingPort = Config.NodePort;
			            			}
			            			response += merge(recivedTokens, 2, recivedTokens.length-1, " ");
			            			System.out.println("[Node]Response: "+ response);
			            			System.out.println("[Node]On Address: " + nextAddress.toString());
			            			System.out.println("[Node]On Port: " + sendingPort.toString());
			            			sendResponse(response, nextAddress, sendingPort, "[Node]error#SEND#");
		            			} catch (UnknownHostException e) {
		            				System.out.println("[Node]Seting Address Failed");
		            			}
		            			break;
		            		case "#JOIN_REQUEST#":
		            			if(recivedTokens.length == 1) {
			            			nextAddress = previousAddress;
			            			response = "#ADD_NODE# ";
			            			response += previousAddress.toString();
			            		for(int n = 0; n < nodeVector.size(); n++) {
			    	 	        	if(nodeVector.get(n) != thisAddress) {
			    	 	        		sendResponse(response, nodeVector.get(n), Config.NodePort, "error#ADD_NODE#");
			    	 	        	}
			    	 	        }
			            		if(nodeVector.contains(previousAddress) == false) {
			            			nodeVector.add(previousAddress);
			            		}
			            			response = "#REQUEST_ACCEPTED# "; // Tag dla udpThread
			            			response += mergeVector(nodeVector, 0, nodeVector.size()-1, " ");
			            			System.out.println("[Node]"+response);
			            			sendResponse(response, nextAddress, previousPort, "error#JOIN_REQUEST#");
			      
			    	 	            nextAddress = null;
			    	 	            previousAddress = null;
		            			}
		            			else break;
		            		case "#ADD_NODE#":
		            			if(recivedTokens.length == 2) {
		            				try {
										tmpAddress = InetAddress.getByName(recivedTokens[1].split("/")[1]);
										nodeVector.add(tmpAddress);
		            				} catch (UnknownHostException e) {
										System.out.println("[Node]Wrong IP Format");
									}
		            				previousAddress = null;
		            			}
		            			break;
		            		case "#EXIT#":
		            			if(recivedTokens.length == 2) {
		            				try {
										tmpAddress = InetAddress.getByName(recivedTokens[1].split("/")[1]);
										nodeVector.remove(tmpAddress);
		            				} catch (UnknownHostException e) {
										System.out.println("[Node]Wrong IP Format");
									}
		            				previousAddress = null;
		            			}
		            			break;
		        	   }	
		           }
		           else {
		        	   System.out.println("[Node]Message is empty");
		           }
	            }
	        System.out.println("[Node]");    
	        }
	    }
}
