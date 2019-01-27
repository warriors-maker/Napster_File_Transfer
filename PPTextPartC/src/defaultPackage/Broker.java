package defaultPackage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Broker {
	//Key: textFile
	//Value: List of Addresses that have register this file;
	static HashMap<String, List<BrokerObject>> map = new HashMap<>();
	
	static HashMap<BrokerObject, Integer> countMap = new HashMap<>();
	
	private static void printBrokerObject(List<BrokerObject> l) {
		for (int i = 0; i < l.size(); i++) {
			BrokerObject obj = l.get(i);
			System.out.println(obj.getAddress() + ": " + obj.getPortNum());
		}
	}
	
	private static void printBrokerWindow() {
		System.out.println("CurrentBroker Window looks like following: ");
		for (String fileName : map.keySet()) {
			System.out.println(fileName);
			printBrokerObject(map.get(fileName));
		}
	}
	
	//check if the client has already registered the file
	private static boolean checkRegistered(List<BrokerObject> l, BrokerObject obj) {
		for (BrokerObject o : l) {
			if (o.getAddress().equals(obj.getAddress()) 
					&& o.getPortNum() == obj.getPortNum()) {
				return true;
			}
		}
		return false;
	}
	
	private static void delete (List<BrokerObject>l, BrokerObject obj) {
		BrokerObject remove = null;
		for (BrokerObject o : l) {
			if (o.getAddress().equals(obj.getAddress()) && o.getPortNum() == obj.getPortNum()) {
				remove = o;
			}
		}
		if (remove == null) {
			return;
		} else {
			l.remove(remove);	
			countMap.put(obj, countMap.get(obj) - 1);
		}
	}
	
	private static void register(EchoDataToBroker dataToBroker, EchoDataToClient dataToClient) {
		InetAddress address = dataToBroker.getTcpAddress();
		int tcpPort = dataToBroker.getTcpPortNum();
		String fileName = dataToBroker.getTextName();
		BrokerObject obj = new BrokerObject(address, tcpPort);
		
		printBrokerWindow();
		if (!map.containsKey(fileName)) {
			map.put(fileName, new ArrayList<>());
		}
		
		//only register if not added before
		if (!checkRegistered(map.get(fileName), obj)) {
			map.get(fileName).add(obj);
			dataToClient.setText("Successfully register the file");
			System.out.println(dataToBroker.getTcpAddress() + "on Port:" + dataToBroker.getTcpPortNum() +
			"successfully register the file: " + dataToBroker.getTextName());
			
			countMap.put(obj, countMap.getOrDefault(obj, 0) + 1);
			
		} else {
			dataToClient.setText("You have registered this file before");
			System.out.println(dataToBroker.getTcpAddress() + "on Port:" + dataToBroker.getTcpPortNum() +
					"fail to register the file: " + dataToBroker.getTextName());
		}
	}
	
	private static void unRegister(EchoDataToBroker dataToBroker, EchoDataToClient dataToClient) {
		InetAddress address = dataToBroker.getTcpAddress();
		int tcpPort = dataToBroker.getTcpPortNum();
		String fileName = dataToBroker.getTextName();
		BrokerObject obj = new BrokerObject(address, tcpPort);
		
		//noOne register the file yet;
		if (!map.containsKey(fileName)) {
			dataToClient.setText("You have not register the file");
			System.out.println(dataToBroker.getTcpAddress() + "on Port: " + dataToBroker.getTcpPortNum() +
					"fail to unregister the file:" + dataToBroker.getTextName());
			return;
		}
		
		List<BrokerObject> l = map.get(fileName);
		
		if (l == null || l.size() == 0) {
			dataToClient.setText("You have not register the file");
			System.out.println(dataToBroker.getTcpAddress() + "on Port: " + dataToBroker.getTcpPortNum() +
					"fail to unregister the file: " + dataToBroker.getTextName());
			return;
		}
		
		//delete iff the client register for this file;
		if (checkRegistered(l, obj)) {
			delete(l, obj);
			dataToClient.setText("Successfully unregister the file");
			System.out.println(dataToBroker.getTcpAddress() + "on Port: " + dataToBroker.getTcpPortNum() +
					"successfully unregister the file:" + dataToBroker.getTextName());
		} else {
			dataToClient.setText("You have not register the file2");
			System.out.println(dataToBroker.getTcpAddress() + "on Port: " + dataToBroker.getTcpPortNum() +
					"fail to unregister the file: " + dataToBroker.getTextName());
		}
	}
	
	//when sending query, remove the BrokerObject that is ourself.
	//note that we remove on our copy but not on the original list
	
	private static boolean checkExists (List<BrokerObject> l, BrokerObject target) {
		for (BrokerObject o : l) {
			if (o.equals(target)) {
				return true;
			}
		}
		System.out.println("Current client never register this file");
		return false;
		
	}
	private static List<BrokerObject> removeBrokerItSelf(List<BrokerObject> l, BrokerObject target) {
		List<BrokerObject> returnList = new ArrayList<>(l);
		if (checkExists(l,target)) {
			returnList.remove(target);
		}
		return returnList;
	}
	
	private static void printList(List<BrokerObject> l) {
		System.out.println("Sending the available obj to client");
		if (l == null) {
			System.out.println("Null");
		} else {
			for (BrokerObject o : l) {
				System.out.println(o.getAddress() + ": " + o.getPortNum());
			}
		}
	}
	
	public static void main(String[] args) throws SocketException {
		// TODO Auto-generated method stub
		//portNumber for the Broker
		int servPort = 8000;
		
		BrokerNetWorkUtil nu = new BrokerNetWorkUtil(servPort);
		nu.createServerSocket();
		nu.setReceivePacket();
		
		System.out.println("I am an Broker Server and I am waiting on port # " + servPort);

		while (true) { // Run forever, receiving and echoing datagrams from any client
			
			try {
				//get the object from the clients
				EchoDataToBroker dataToBroker = nu.getSerializedEchoData();
				EchoDataToClient dataToClient = new EchoDataToClient();
				
				//Query the List;
				if (dataToBroker.isQuery()) {
					String fileName = dataToBroker.getTextName();
					dataToClient.setReAndUnRe(false);
					dataToClient.setFileName(fileName);
					
					InetAddress address = dataToBroker.getTcpAddress();
					int portNum = dataToBroker.getTcpPortNum();
					BrokerObject target = new BrokerObject(address, portNum);
					
					if (!map.containsKey(fileName)) {
						System.out.println("The file does not Exists required by the Client");
						dataToClient.setExists(false);
						if (!countMap.containsKey(target) || countMap.get(target) < 2) {
							dataToClient.setLargerThanTwo(false);
						} else {
							dataToClient.setLargerThanTwo(true);
						}
						dataToClient.setList(new ArrayList<>());
					} else if (!countMap.containsKey(target) || countMap.get(target) < 2) { 
						System.out.println(target.getAddress() + ":" + target.getPortNum() + "does not register more than two");
						dataToClient.setLargerThanTwo(false);
					}	
					else {
						dataToClient.setLargerThanTwo(true);
						List<BrokerObject> list = map.get(fileName);
						//donot let the client to query itself again
						List<BrokerObject> sendList = removeBrokerItSelf(list, target);
						printList(sendList);
						dataToClient.setList(sendList);
					}
					nu.sendSerializedEchoData(dataToClient);
					
				} else {
					//registering
					dataToClient.setReAndUnRe(true);
					if (dataToBroker.isRegister()) {
						register(dataToBroker,dataToClient);	
					} 
					//unregistering
					else {
						unRegister(dataToBroker, dataToClient);
					}
					nu.sendSerializedEchoData(dataToClient);
				}
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
