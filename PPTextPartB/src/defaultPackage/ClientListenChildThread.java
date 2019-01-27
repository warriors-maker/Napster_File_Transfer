package defaultPackage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;


public class ClientListenChildThread implements Runnable{
	private Socket socket;
	
	public ClientListenChildThread (Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//first check the required file
		FileNameRequest request = null;
		try {
			request = getFileNameFromClient();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong with the client...");
			return;
		}
		
		if (request != null) {
			String fileName = request.getFileName();
			System.out.println("The client request: " + fileName);
			System.out.println("The client that is listening is + ");
			File file = new File("/Users/yingjianwu/Documents/Java/LeetcodePractice/linkedList/PPTextPartB/src/"+fileName);
			boolean exists = file.exists();
			if (!exists) {
				System.out.println("I donot have the file" + fileName);
				sendNackToClient();
				return;
			}
			
			//start sending lines to Client
			try {
				sendToClient(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//	public void getFromClient() throws IOException, ClassNotFoundException {
//		InputStream is = socket.getInputStream();  
//	    ObjectInputStream ois = new ObjectInputStream(is);  
//		board = (GameBoard)ois.readObject();
//	}
	
	//if the fileName does not exist.
	private void sendNackToClient() {
		try {
			OutputStream os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			Sentence fileFail = new Sentence();
			fileFail.setFileExist(false);
			oos.writeObject(fileFail);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	private void readAckFromClient(Socket socket) throws IOException {
		InputStream is;
		is = socket.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(is);
		try {
			Sentence sentence = (Sentence)ois.readObject();
//			System.out.println("The client successfully gets" + sentence.getSentence());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendLine(String line) {
		OutputStream os;
		try {
			os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			//System.out.println("Sent: " + line);
			Sentence sent = new Sentence(line);
			sent.setFileExist(true);
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			oos.writeObject(sent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}  
	}
	
	private void sendEnd() {
		OutputStream os;
		try {
			os = socket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			Sentence end = new Sentence();
			end.setEnd(true);
			oos.writeObject(end);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	
	//send each line to the registered client;
	public void sendToClient(File fin) throws IOException {
	    FileInputStream fis = new FileInputStream(fin);
	    
		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		boolean clientFail = false;
		while ((line = br.readLine()) != null) {
			//System.out.println("I am here.");
			//send to Client
			try {
				Thread.sleep(4500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendLine(line);
			//Read from Client:
			try {
				readAckFromClient(socket);
			} catch (IOException e) {
				System.out.println("The Client is dead, so I am not going to send to it anymore");
				clientFail = true;
				br.close(); 
				break;
			}
		}
		
		if (!clientFail) {
			sendEnd();
		} else {
			//Do nothing since the client dies.
		}
		br.close();    
	}
	
	//read the object from the clients request
	public FileNameRequest getFileNameFromClient() throws IOException, ClassNotFoundException {
		InputStream is = socket.getInputStream();  
		ObjectInputStream ois = new ObjectInputStream(is);  
		return (FileNameRequest)ois.readObject();
	}

}
