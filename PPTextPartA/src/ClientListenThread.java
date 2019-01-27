import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;




public class ClientListenThread implements Runnable{
	
	int tcpPortNum;
	public ClientListenThread(int portNum) {
		this.tcpPortNum = portNum;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//this is the first handshake connection
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(tcpPortNum);
			System.out.println("I am listening at port:" + tcpPortNum);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + tcpPortNum);
			System.exit(-1);
		}
        
        //this will make the server able to sponsed off different threads and port number for new conversation
		while (true)
		{
			//accept the incoming call, and pass the NEW socket to the thread for each conversation
			Socket clntSock = null;
			try {
				//this clntsocket is the socket after we finish first handshake
				clntSock = serverSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
			System.out.println("New Socket from new users");
			ClientListenChildThread t = new ClientListenChildThread(clntSock);
			Thread T = new Thread(t);
			T.start();
		}
		//serverSocket.close();
    }
	
	
}


