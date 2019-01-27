import java.io.Serializable;
import java.net.InetAddress;

//this is the data that is passed from Client to the Broker
public class EchoDataToBroker implements Serializable{
	private InetAddress tcpAddress;
	private int tcpPortNum;
	private String textName;
	private boolean query;// true indicates that it wants to query, false means either register or unregister
	private boolean register;// true indicates that it wants to register, false indicates that it wants to unregister
	
	public EchoDataToBroker() {
		
	}
	public EchoDataToBroker(InetAddress tcpAddress, int tcpPortNum, String textName, boolean query, boolean register) {
		this.tcpAddress = tcpAddress;
		this.tcpPortNum = tcpPortNum;
		this.textName = textName;
		this.query = query;
		this.register = register;
	}
	
	public boolean isQuery() {
		return query;
	}

	public void setQuery(boolean query) {
		this.query = query;
	}

	public boolean isRegister() {
		return register;
	}

	public void setRegister(boolean register) {
		this.register = register;
	}

	public InetAddress getTcpAddress() {
		return tcpAddress;
	}

	public void setTcpAddress(InetAddress tcpAddress) {
		this.tcpAddress = tcpAddress;
	}

	public int getTcpPortNum() {
		return tcpPortNum;
	}

	public void setTcpPortNum(int tcpPortNum) {
		this.tcpPortNum = tcpPortNum;
	}

	public String getTextName() {
		return textName;
	}

	public void setTextName(String textName) {
		this.textName = textName;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
