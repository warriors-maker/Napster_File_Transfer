
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
	
	private static void printBrokerObject(List<BrokerObject> l) {
		for (int i = 0; i < l.size(); i++) {
			BrokerObject obj = l.get(i);
			System.out.println(obj.getAddress() + ": " + obj.getPortNum());
		}
	}
	
	private static List<BrokerObject> removeBrokerItSelf(List<BrokerObject> l, BrokerObject target) {
		List<BrokerObject> returnList = new ArrayList<>(l);
		returnList.remove(target);
		return returnList;
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
		}
	}
	
	private static void register(EchoDataToBroker dataToBroker, EchoDataToClient dataToClient) {
		InetAddress address = dataToBroker.getTcpAddress();
		int tcpPort = dataToBroker.getTcpPortNum();
		String fileName = dataToBroker.getTextName();
		BrokerObject obj = new BrokerObject(address, tcpPort);
		
		if (!map.containsKey(fileName)) {
			map.put(fileName, new ArrayList<>());
		}
		//only register if not added before
		if (!checkRegistered(map.get(fileName), obj)) {
			map.get(fileName).add(obj);
			dataToClient.setText("Successfully register the file");
			System.out.println(dataToBroker.getTcpAddress() + "on Port:" + dataToBroker.getTcpPortNum() +
			"successfully register the file: " + dataToBroker.getTextName());
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
			dataToClient.setText("You have not register the file");
			System.out.println(dataToBroker.getTcpAddress() + "on Port: " + dataToBroker.getTcpPortNum() +
					"fail to unregister the file: " + dataToBroker.getTextName());
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
				
				InetAddress address = dataToBroker.getTcpAddress();
				int portNum = dataToBroker.getTcpPortNum();
				BrokerObject target = new BrokerObject(address, portNum);
				
				//Query the List;
				if (dataToBroker.isQuery()) {
					String fileName = dataToBroker.getTextName();
					dataToClient.setReAndUnRe(false);
					dataToClient.setFileName(fileName);
					if (!map.containsKey(fileName)) {
						dataToClient.setList(new ArrayList<>());
					} else {
						List<BrokerObject> list = map.get(fileName);
						List<BrokerObject> returnList = removeBrokerItSelf(list, target);
						dataToClient.setList(returnList);
						System.out.println("Actual Broker in the storage:");
						printBrokerObject(list);
						System.out.println("Broker that donot include the current receiver:");
						printBrokerObject(returnList);
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
