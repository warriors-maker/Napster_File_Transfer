package defaultPackage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

public class Client {
	
	//One Thread for acting as TCP server
	//Another thread for communicating with the Broker
	public static void main(String[] args) throws UnknownHostException {
		// TODO Auto-generated method stub
		InetAddress tcpServerAddress = InetAddress.getByName("127.0.0.1");
		int tcpPortNumber = 8802;
		
		//The portNum in which it acts as a Server;
		Thread clientListener = new Thread(new ClientListenThread(tcpPortNumber));
		clientListener.start();
		
		//pass in port and address so the action thread knows whcih tcp it should connect with
		Thread clientActioner = new Thread(new ClientActionThread(tcpPortNumber, tcpServerAddress));
		clientActioner.start();
	}

}
