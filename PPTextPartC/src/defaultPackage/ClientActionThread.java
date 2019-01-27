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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class ClientActionThread implements Runnable {
	//this is our own tcp information for others to listen
	int tcpPortNum;
	InetAddress tcpAddress;
	final String path = "/Users/yingjianwu/Documents/Java/LeetcodePractice/linkedList/PPTextPartC/src/";
	
	//Maintain a set of file that have downloaded; so no need to download again.
	private HashSet<String> container = new HashSet<>(); //need to restrict action 4;
	
	
	//maintain the fileName and the corresponding Clients you query from the broker
	HashMap<String, List<BrokerObject>> map = new HashMap<>();
	
	//Contains the rank number of individual broker
	//-1 dead
	//0 Not send
	//1 sent in average 3000s
	//2 sent in average 2000s
	//3 sent in average 1000s
	
	HashMap<BrokerObject, Integer> rankMap = new HashMap<>();
	
	public ClientActionThread(int tcpPortNum, InetAddress tcpAddress) {
		this.tcpPortNum = tcpPortNum;
		this.tcpAddress = tcpAddress;
	}
	
	private void printScoreInfo() {
		System.out.println("-------------------------------------------------");
		System.out.println("The ranking is like the following:");
		System.out.println("-1. The IP is too congested and even dies");
		System.out.println("0 . Do not really know, may be give a try");
		System.out.println("1. In average gets each line more than 2 seconds");
		System.out.println("2. In average gets each line between 1 second - 2 second");
		System.out.println("3. In average gets each line less than 1 second");
	}
	
	
	private BrokerObject pickServer(String fileName) {
		printScoreInfo();
		printfileScore(fileName);
		List<BrokerObject> l = map.get(fileName);
		int userChoice = userInputTargetServer(l.size());
		//No one for you to choose;
		if (userChoice == -1) {
			return null;
		}
		return selectedServer(userChoice, fileName);
	}
	
	private boolean atLeastOneServerAlive(String fileName) {
		List<BrokerObject> l = map.get(fileName);
		for (BrokerObject o : l) {
			InetAddress address = o.getAddress();
			int portNum = o.getPortNum();
			Integer score = rankMap.get(o);
			if (score == null || score != -1) {
				return true;
			}
		}
		return false;
	}
	
	private void printScore() {
		if (rankMap.size() == 0) {
			System.out.println("You donot have any record with other clients");
			return;
		}
		int i = 1;
		for (BrokerObject o : rankMap.keySet()) {
			InetAddress address = o.getAddress();
			int portNum = o.getPortNum();
			Integer score = rankMap.get(o);
			if (score == null) {
				System.out.println(i + " | " + address + ":" + portNum + "Score: " + 0);
			} else {
				score = rankMap.get(o);
				System.out.println(i + " | " + address + ":" + portNum + "Score: " + score);
			}
			i++;
		}
	}
	private void printfileScore(String fileName) {
		System.out.println("Choose the broker you want to send to");
		System.out.println("Here are the information for each Broker:");
		
		//get all the Server you can send to;
		List<BrokerObject> l = map.get(fileName);
		
		if (l.size() == 0 ) {
			System.out.println("There is no Server currently Available");
			return;
		}
		
		int i = 1;
		for (BrokerObject o : l) {
			InetAddress address = o.getAddress();
			int portNum = o.getPortNum();
			Integer score = rankMap.get(o);
			if (score == null) {
				System.out.println(i + " | " + address + ":" + portNum + "Score: " + 0);
			} else {
				score = rankMap.get(o);
				System.out.println(i + " | " + address + ":" + portNum + "Score: " + score);
			}
			
			i++;
		}
	}
	
	private int userInputTargetServer(int max) {
		if (max == 0) {
			System.out.println("There is nothing for you to choose");
			return -1;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter your choice: ");

		String choice;
		try {
			choice = reader.readLine();
			while ((choice.charAt(0) - '0'<= 0 || choice.charAt(0) - '0' > max )) {
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
	
	private BrokerObject selectedServer(int selectedIndex, String fileName) {
		List<BrokerObject> l = map.get(fileName);
		return l.get(selectedIndex - 1);
	}
	
	

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
		System.out.println("6.PrintOut all the server score you know");
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
					|| (choice.charAt(0) - '0'<= 0 || choice.charAt(0) - '0' > 6 )) {
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
//	private BrokerObject randomSelect(List<BrokerObject> l) {
//	
//		int index = new Random().nextInt(l.size());
//		//if index is the size, return the first one;
//		if (index == l.size()) {
//			return l.get(0);
//		}
//		return l.get(index);
//	}
	
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
	private String getTextFromServer(Socket socket, String fileName, BrokerObject obj) throws ClassNotFoundException, IOException {
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
		int time = 0;
		int count = 0;
	    while (!end) {
	    	//read from Client Sender;
	    	Instant startTime = Instant.now();
	    	Sentence s = getLine(socket, fileName);
	    	Instant endTime = Instant.now();
	    	Duration result = Duration.between(startTime, endTime);
	    	long stopTime = result.toMillis();
//	    	System.out.println(stopTime);
	    	
	    	time += (int) (stopTime / 1000);
//	    	System.out.println(time);
	    	count += 1;
//	    	System.out.println(count);
	    	
	    	if (s != null) {
	    		sendBackAck(socket,s);
	    	}
	    	
	    	if (s != null && s.isEnd()) {
	    		count -=1;
	    		time = time / count;
	    		System.out.println("The average time for getting each line of this file is: " + time);
	    		assignScore(time,obj);
	    		return "End";
	    	}
	    }
	    
	    return null;//will return null because even you can not send your fileName to the Server
	}
	
	private void assignScore(int time, BrokerObject obj) {

		int timeScore;
		if (time <= 1) {
			timeScore = 3;
		} else if (time <= 2) {
			timeScore = 2;
		}  else {
			timeScore = 1;
		}
		
		Integer oldScore = rankMap.get(obj);
		if (oldScore == null) {
			rankMap.put(obj,timeScore);
			return;
		}
		if (oldScore == -1) {
			rankMap.put(obj, timeScore);
			return;
		}
		int newScore = (oldScore + timeScore + 1) / 2;
		rankMap.put(obj,newScore);
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
		//the tcp server information that you are connecting to;
		InetAddress serverAddress = obj.getAddress();
		int portNum = obj.getPortNum();

		
		//Connect with assigned client
		Socket socket;
		try {
			socket = new Socket(serverAddress, portNum);
			System.out.println("Try to get " + fileName + " from " + serverAddress + " and " + portNum);
			cleanFile(fileName);
			//send to server
			String result = getTextFromServer(socket,fileName, obj);
			if (result != null) {
				container.add(fileName);
			} else {
				//select another One and make connection;
				cleanFile(fileName);
				rankMap.put(obj, -1);
				System.out.println("Something goes wrong");
				if (!atLeastOneServerAlive(fileName)) {
					System.out.println("Since noOne is really available, you may want to take another action");
					return;
				}
				BrokerObject newOne = pickServer(fileName);
				if (newOne != null) {
					communicateServer(newOne, fileName);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("The Client is busy or not alived");
			cleanFile(fileName);
			rankMap.put(obj, -1);
			//start a new connection;
			if (!atLeastOneServerAlive(fileName)) {
				System.out.println("Since noOne is really available, you may want to take another action");
				return;
			}
			BrokerObject newOne = pickServer(fileName);
			//check if there is at leastOne alive
			if (newOne != null) {
				communicateServer(newOne, fileName);
			}
			return;
		} catch(Exception e) {
			
		}
		
		//System.out.println("Current Local Port is" + socket.getLocalPort());
		//no longer have packets (for udp) but stream for tcp;
		
	}
	
	
//	private BrokerObject pickAnotherOne(BrokerObject dead, String fileName) {
//		List<BrokerObject> l = map.get(fileName);
//		l.remove(dead);
//		if (l.size() == 0) {
//			System.out.println("You may want to Query again since there is no one available");
//			return null;
//		}
//		
//		BrokerObject obj = pickServer(fileName);
//		return obj;
//	}

	
	private BrokerObject handleQueryClient(String fileName) {
//		if (container.contains(fileName)) {
//			System.out.println("You already got this file, Stop losing data plan");
//			return null;
//		}
		if (!map.containsKey(fileName) || map.get(fileName) == null || map.get(fileName).size() == 0) {
			//System.out.println("NoOne is registering the current file right Now, Come back Later");
			return null;
		} else {
			//List<BrokerObject> l = map.get(fileName);
			BrokerObject selected = pickServer(fileName);
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
			} else if (!toReceive.isExists()){
				System.out.println("The file you want does not exists");
				if (!toReceive.isLargerThanTwo()) {
					System.out.println("Also, You need to register two files before you can query others");
				}
			}else {
				if (!toReceive.isReAndUnRe()) {
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
				} else if (choice == 5) {
//					System.out.println("Query the Broker Again to get the Latest information");
//					handleQueryBroker(data);
//					sendDataToBroker(nu,data);
//					receiveDataFromBroker(nu);
					printAvail();
				} else if (choice == 6) {
					printScore();
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
