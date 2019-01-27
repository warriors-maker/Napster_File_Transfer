package defaultPackage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class ClientActionThread implements Runnable {
	//this is our own tcp information for others to listen
	int tcpPortNum;
	InetAddress tcpAddress;
	final String path = "/Users/yingjianwu/Documents/Java/LeetcodePractice/linkedList/PPTextPartB/src/";
	
	//Maintain a set of file that have downloaded; so no need to download again.
	private HashSet<String> container = new HashSet<>(); //need to restrict action 4;
	
	
	//maintain the fileName and the corresponding Clients you query from the broker
	HashMap<String, List<BrokerObject>> map = new HashMap<>();
	
	public ClientActionThread(int tcpPortNum, InetAddress tcpAddress) {
		this.tcpPortNum = tcpPortNum;
		this.tcpAddress = tcpAddress;
	}
	
	//donot want to query from myself...Pretty Wierd
//	private void removeMyself(HashMap<String, List<BrokerObject>> map, String fileName) {
//		List<BrokerObject> l = map.get(fileName);
//		if (l == null || l.size() == 0) {
//			return;
//		}
//		
//		BrokerObject obj = null;
//		for (BrokerObject o : l) {
//			if (o.getAddress().equals(tcpAddress) && o.getPortNum() == tcpPortNum) {
//				obj = o;
//			}
//		}
//		if (obj != null) {
//			l.remove(obj);
//		}
//		map.put(fileName, l);
//	}
	
	//Input the fileName
	private String inputFileName() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the fileName you want to take action on: ");

		String name;
		try {
			name = reader.readLine();
			while (name.length() == 0) {
				System.out.println("Invalid Input, Please input Again");
				name = reader.readLine();
			}
			return name;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static void printList(List<BrokerObject> l) {
		System.out.println("Here are the avialable Server that you can send to:");
		if (l == null) {
			System.out.println("Null");
		} else if (l.size() == 0) {
			System.out.println("There is noOne you can Query");
		}
		else {
			for (BrokerObject o : l) {
				System.out.println(o.getAddress() + ": " + o.getPortNum());
			}
		}
	}
	
	private void printOption() {
		System.out.println("Here are the choices you can hava:");
		System.out.println("1.Register a file");
		System.out.println("2.Unregister a file");
		System.out.println("3.Query Broker");
		System.out.println("4.Query Clients");
		System.out.println("5.PrintOut all file and address you can communicate with");
	}
	
	//Print All available files and its corresponding clients that you can query
	private void printAvail() {
		if (map.keySet().isEmpty()) {
			System.out.println("Sorry, There is no available server for this client");
		}
		for (String fileName : map.keySet()) {
			System.out.println("FileName: " + fileName);
			List<BrokerObject> l = map.get(fileName);
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (BrokerObject o : l) {
				InetAddress address = o.getAddress();
				int portNum = o.getPortNum();
				sb.append("(" + address +":"+portNum +")");
			}
			sb.append("]");
			System.out.println("Here are the List of clients you can pick");
			System.out.println(sb.toString());
		}
	}
	
	//take the choices input
	private int takeInput() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter your choice: ");

		String choice;
		try {
			choice = reader.readLine();
			while (choice.length() != 1 
					|| (choice.charAt(0) - '0'<= 0 || choice.charAt(0) - '0' > 5 )) {
				System.out.println("Invalid Input, Please input Again");
				choice = reader.readLine();
			}
			
			int num = choice.charAt(0) - '0';
			System.out.println("Your choice is: " + num);
			return num;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	
	//random select a server to connect to;
	private BrokerObject randomSelect(List<BrokerObject> l) {
	
		int index = new Random().nextInt(l.size());
		//if index is the size, return the first one;
		if (index == l.size()) {
			return l.get(0);
		}
		return l.get(index);
	}
	
	//Send Back Ack to the Server
	private void sendBackAck(Socket socket, Sentence sentence) {
		OutputStream os;
		try {
			os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(sentence);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Get the sentence from the Server
	private Sentence getLine(Socket socket, String fileName) throws IOException, ClassNotFoundException {

			InputStream is = socket.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

			Sentence sentence = (Sentence)ois.readObject();
			if (sentence.isEnd()) {
				System.out.println("Successfully get all the text");
				return sentence;
			}
			
			if (!sentence.isFileExist()) {
				System.out.println("The client does not have the file");
				return null;
			}
			String line = sentence.getSentence();
			System.out.println(line);
			
			writeToFile(fileName, line);
			return sentence;
	}
	
	
	//General Function
	private String getTextFromServer(Socket socket, String fileName) throws ClassNotFoundException, IOException {
		boolean end = false;
		//StringBuilder sb = new StringBuilder();
	    
		//First always send the required text you want to the client;
		try {
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			FileNameRequest f = new FileNameRequest(fileName);
			oos.writeObject(f);
		} catch (IOException e) {
			System.out.println("Possible that the port you try to connect dies or too congested");
			e.printStackTrace();
			end = true;
		}

		//sendBack sentence
	    while (!end) {
	    	//read from Client Sender;
	    	Sentence s = getLine(socket, fileName);
	    	if (s != null) {
	    		sendBackAck(socket,s);
	    	}
	    	if (s != null && s.isEnd()) {
	    		return "End";
	    	}
	    }
	    return null;
	}
	
	private void writeToFile(String fileName, String line) {
		String fileOutputName = path + fileName + tcpPortNum + ".txt";
		try {
			FileWriter fw = new FileWriter(fileOutputName, true);
			fw.write(line);
			fw.write("\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void cleanFile(String fileName) {
		System.out.println("CleaningUp the file");
		String fileOutputName = path + fileName + tcpPortNum + ".txt";
		File fout = new File(fileOutputName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//TCP connection with the destined Clients
	private void communicateServer(BrokerObject obj, String fileName) {
		InetAddress serverAddress = obj.getAddress();
		int portNum = obj.getPortNum();

		
		//Connect with assigned client
		Socket socket;
		try {
			socket = new Socket(serverAddress, portNum);
			System.out.println("Try to get " + fileName + " from " + serverAddress + " and " + portNum);
			cleanFile(fileName);
			//send to server
			String result = getTextFromServer(socket,fileName);
			if (result != null) {
				container.add(fileName);
			} else {
				//select another One and make connection;
				cleanFile(fileName);
				System.out.println("I will pick anohter one for you");
				BrokerObject newOne = pickAnotherOne(obj, fileName);
				if (newOne != null) {
					communicateServer(newOne, fileName);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("The Client is busy or not alived");
			System.out.println("I will pick another One for you");
			
			cleanFile(fileName);
			//start a new connection;
			BrokerObject newOne = pickAnotherOne(obj, fileName);
			if (newOne != null) {
				communicateServer(newOne, fileName);
			}
			return;
		} catch(Exception e) {
			
		}
		
		//System.out.println("Current Local Port is" + socket.getLocalPort());
		//no longer have packets (for udp) but stream for tcp;
		
	}
	
	
	private BrokerObject pickAnotherOne(BrokerObject dead, String fileName) {
		List<BrokerObject> l = map.get(fileName);
		l.remove(dead);
		if (l.size() == 0) {
			System.out.println("You may want to Query again since there is no one available");
			return null;
		}
		
		BrokerObject obj = randomSelect(l);
		return obj;
	}

	
	private BrokerObject handleQueryClient(String fileName) {
//		if (container.contains(fileName)) {
//			System.out.println("You already got this file, Stop losing data plan");
//			return null;
//		}
		if (!map.containsKey(fileName) || map.get(fileName) == null || map.get(fileName).size() == 0) {
			return null;
		} else {
			List<BrokerObject> l = map.get(fileName);
			BrokerObject selected = randomSelect(l);
			return selected;
		}
	}
	
	private void handleRegister(EchoDataToBroker data) {
		data.setQuery(false);
		data.setRegister(true);
	}
	
	private void handleUnRegister(EchoDataToBroker data) {
		data.setQuery(false);
		data.setRegister(false);
	}
	
	private void handleQueryBroker(EchoDataToBroker data) {
		data.setQuery(true);
	}
	
	
	private void handleGeneral(EchoDataToBroker data, String fileName) {
		//Common fields that need to be set;
		data.setTextName(fileName);
		data.setTcpAddress(tcpAddress);	
		data.setTcpPortNum(tcpPortNum);
	}
	
	
	//UTP Methods
	private void sendDataToBroker(clientNetworkUtils nu, EchoDataToBroker data) {
		try {
			nu.sendSerializedEchoData(data);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void receiveDataFromBroker(clientNetworkUtils nu) {
		try {
			EchoDataToClient toReceive = nu.getSerializedEchoData();
			if (toReceive == null) {
				System.out.println("The Broker is not responding, you may consider to connect it later");
			} else {
				if (!toReceive.isReAndUnRe()) {
					if (!toReceive.isExists()) {
						System.out.println("The file you want is not registerd by anyone yet");
						if (!toReceive.isLargerThanTwo()) {
							System.out.println("Also, You need to register two files before you can query others");
						}
						return;
					}
					if (!toReceive.isLargerThanTwo()) {
						System.out.println("You need to register two files before you can query others");
						return;
					}
					List<BrokerObject> l = toReceive.getList();
					printList(l);
					String filekey = toReceive.getFileName();
					map.put(filekey, l);
//					removeMyself(map,filekey);
				} else {
					System.out.println(toReceive.getText());
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean fileExist(String fileName) {
		System.out.println("Checking: " + fileName);
		String filePath = "/Users/yingjianwu/Documents/Java/LeetcodePractice/linkedList/PPTextPartA/src/"+fileName;
		File file = new File(filePath);
		boolean exists = false;
		try {
			exists = file.exists() && file.getCanonicalPath().equals(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return exists;
	}
	
	@Override
	public void run() {
		try {
			//Broker Server that we want to send to;
			InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
			int BrokerServerPort = 8000;
			
			//make a UDPconnection with the server port;
			clientNetworkUtils nu = new clientNetworkUtils(serverAddress, BrokerServerPort); //create a connection to a server
			try {
				nu.createClientSocket();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// TODO Auto-generated method stub
			while (true) {
				System.out.println("---------------------------------------");
				printOption();
				int choice = takeInput();
				if (choice == -1) {
					System.out.println("Something wrong with the input choice Number");
				} 
				else if (choice == 5) {
					printAvail();
				}
				//need to build connection
				else {
					String fileName = inputFileName();
					EchoDataToBroker data = new EchoDataToBroker();
					handleGeneral(data, fileName);
					
					//this makes a TCP connection with others
					if (choice == 4){
						//need to make that mostUpToDate before querying
						if (container.contains(fileName)) {
							System.out.println("I already got the file");
						} else {
							System.out.println("Query the Broker Again to get the Latest information");
							handleQueryBroker(data);
							sendDataToBroker(nu,data);
							receiveDataFromBroker(nu);
							
							//then chooses the client you will connect with
							BrokerObject obj = handleQueryClient(fileName);
							//will first send the fileName to the Server
							//and then get line by line from the server
							if (obj != null) {
								communicateServer(obj,fileName);
							}
						}
					} 
					//this makes a UDP connection with the Broker
					else {
						//register
						boolean checkExists = true; // this is for your first choice, the other choices will not be changed
						if (choice == 1) {
							String checkName = data.getTextName();
							//check whether you have this file in your local before registering
							boolean fileExist = fileExist(checkName);
							if (fileExist) {
								System.out.println("The file exists in your local directory!");
								handleRegister(data);
							} else {
								checkExists = false;
								System.out.println("Fail to Register because you do not have this file");
							}
						} 
						//unregister
						else if (choice == 2) {
							handleUnRegister(data);
						} 
						//query
						else if (choice == 3) {
							handleQueryBroker(data);
						}
						
						//sendData to Broker
						if (checkExists) {
							sendDataToBroker(nu,data);
							//receive the data we want from the broker
							receiveDataFromBroker(nu);
						}
					}
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
