package defaultPackage;
import java.io.Serializable;
import java.net.InetAddress;

//object that contains all the client information
public class BrokerObject implements Serializable{
	//TCP address 
	InetAddress address;
	//TCP portNum
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

	
	
	public BrokerObject (InetAddress address, int portNum) {
		this.address = address;
		this.portNum = portNum;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + this.address.hashCode();
		hash = 31 * hash + this.portNum;
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
	    if (o == this) {
	      return true;
	    }
	    if (!(o instanceof BrokerObject)) {
	      return false;
	    }
	    BrokerObject cc = (BrokerObject)o;
	    return cc.getAddress().equals(this.address) && cc.getPortNum() == this.getPortNum();
	  }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
