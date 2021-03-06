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
	Config config;
	String recivedMessage, recivedTokens[], response;
	DatagramSocket datagramSocket;
	DatagramPacket receivedPacket, responsePacket;
	InetAddress previousAddress, nextAddress, thisAddress, tmpAddress, sendBackAddress,resetAddress;
	Integer previousPort, sendingPort;
	byte[] byteMessage, byteResponse;
	Vector<InetAddress> nodeVector;
	
	public NodeThread() {
		super();
		config = new Config();
		byteMessage = new byte[config.Buffer_size];
		byteResponse = new byte[config.Buffer_size];
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
			datagramSocket = new DatagramSocket(config.NodePort);
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
		receivedPacket = new DatagramPacket(new byte[config.Buffer_size], config.Buffer_size);
		nextAddress = resetIP();
	}
	public NodeThread(Vector<InetAddress> vector) {
		super();
		config = new Config();
		byteMessage = new byte[config.Buffer_size];
		byteResponse = new byte[config.Buffer_size];
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
			datagramSocket = new DatagramSocket(config.NodePort);
		} catch (SocketException e2) {
			System.out.println("[Node]Opening Socket Failed");
		}
		try {
			datagramSocket.setSoTimeout(0);
		} catch (SocketException e) {
			System.out.println("[Node]Changing Timeout Failed");
		}
		nodeVector = vector;
		receivedPacket = new DatagramPacket(new byte[config.Buffer_size], config.Buffer_size);
		nextAddress = resetIP();
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
	public InetAddress resetIP() {
		try{
           resetAddress = InetAddress.getByName("127.0.0.0");
        }catch (UnknownHostException e){
            throw new RuntimeException(e);
        }
		return resetAddress;
	}
	
	public void run() {
		System.out.println("[Node]Node is running; this address is:");
		System.out.println("[Node]"+thisAddress);
		System.out.println("[Node]Nodes' adresses in network");
    	System.out.println("[Node]"+nodeVector);
	        while (true) {
	        	//System.out.println("[Node]Nodes' adresses in network");
	        	//System.out.println("[Node]"+nodeVector);
	            
	        	receievePacket();
	           
	            System.out.println("[Node]PacketRecived");
	            //System.out.println(receivedPacket.getAddress());
	            //System.out.println(nextAddress);
	            byteResponse = receivedPacket.getData();
	            
	            if((char)byteResponse[0] != '#') {
	            	if(receivedPacket.getAddress().toString().equals(nextAddress.toString())) { // sprawcz czy wiadomo?????? wraca po w???z???ach 
	            		sendResponse(byteResponse,receivedPacket.getLength() , sendBackAddress, previousPort, "[Node]error'NO_TAG' (sending back)");	            	
	            		System.out.println("[Node]Sending Back On Address: " + sendBackAddress);
	            		System.out.println("[Node]Sending Back On Port: " + ((Integer)previousPort).toString());
	            		previousAddress = null;
	            		previousPort = null;
	            	}
	            	else System.out.println("[Node]Unknown packet format");
	            }
	            else {
	            	recodePacket();
	            	System.out.println("[Node]"+recivedMessage);
	            	
	            	previousAddress = receivedPacket.getAddress(); // Port i host kt???ry wys???a??? nam zapytanie
	            	previousPort = receivedPacket.getPort();
	            	
	            	//System.out.println(recivedTokens[]);
		            if(recivedTokens.length >= 1) {
		        	   switch (recivedTokens[0]){
		            		case "#SEND#":
		            			response = "";
		            			sendBackAddress = previousAddress;
		            			sendingPort = config.SendingPort;
		            			try {
		            				nextAddress = InetAddress.getByName(recivedTokens[1].split("/")[1]);
		            				if(nodeVector.contains(nextAddress) == true) {
			            				response = "#SEND# ";
			            				sendingPort = config.NodePort;
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
			    	 	        		sendResponse(response, nodeVector.get(n), config.NodePort, "error#ADD_NODE#");
			    	 	        }
			            		if(nodeVector.contains(previousAddress) == false) {
			            			nodeVector.add(previousAddress);
			            		}
			            			response = "#REQUEST_ACCEPTED# "; // Tag dla udpThread
			            			response += mergeVector(nodeVector, 0, nodeVector.size()-1, " ");
			            			System.out.println("[Node]Sending request reply:"+response);
			            			sendResponse(response, nextAddress, previousPort, "error#JOIN_REQUEST#");
			            			System.out.println("[Node]Nodes' adresses in network");
			        	        	System.out.println("[Node]"+nodeVector);
			            			nextAddress = resetIP();
			    	 	            previousAddress = null;
		            			}
		            			else break;
		            		case "#ADD_NODE#":
		            			if(recivedTokens.length == 2) {
		            				try {
										tmpAddress = InetAddress.getByName(recivedTokens[1].split("/")[1]);
										if(nodeVector.contains(tmpAddress) == false) {
										nodeVector.add(tmpAddress);
										} else {
											//System.out.println("[Node]Address Rejected (Warning)");
										}
		            				} catch (UnknownHostException e) {
										System.out.println("[Node]Wrong IP Format");
									}
		            				System.out.println("[Node]Nodes' adresses in network");
		            	        	System.out.println("[Node]"+nodeVector);
		            				nextAddress = resetIP();
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
		            				System.out.println("[Node]Nodes' adresses in network");
		            	        	System.out.println("[Node]"+nodeVector);
		            				nextAddress = resetIP();
		            				previousAddress = null;
		            			}
		            			break;
		        	   }	
		           }
		           else {
		        	   System.out.println("[Node]Message is empty");
		           }
	            }
	        //System.out.println("[Node]");    
	        }
	    }
}
