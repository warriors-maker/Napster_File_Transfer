package defaultPackage;
import java.io.Serializable;
import java.net.InetAddress;

public class FileNameRequest implements Serializable {
	private String fileName;
	InetAddress address;
	int portNum;
	
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	public int getPortNum() {
		return portNum;
	}
	public void setPortNum(int portNum) {
		this.portNum = portNum;
	}
	public FileNameRequest(String fileName) {
		this.fileName = fileName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
